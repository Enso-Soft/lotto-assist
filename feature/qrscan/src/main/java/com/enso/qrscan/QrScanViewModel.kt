package com.enso.qrscan

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.exception.DuplicateQrException
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoTicket
import com.enso.domain.usecase.SaveLottoTicketUseCase
import com.enso.qrscan.parser.LottoQrParser
import com.enso.util.lotto_date.LottoDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class QrScanViewModel @Inject constructor(
    private val saveLottoTicketUseCase: SaveLottoTicketUseCase,
    private val getLottoResultUseCase: com.enso.domain.usecase.GetLottoResultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QrScanUiState())
    val state: StateFlow<QrScanUiState> = _state.asStateFlow()

    private val _effect = Channel<QrScanEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // 마지막으로 처리한 QR URL (중복 방지용)
    private var lastProcessedQrUrl: String? = null
    
    // QR 처리 중 플래그 (다중 QR 동시 처리 방지)
    private var isProcessingQr = false

    init {
        _state.update { it.copy(currentRound = LottoDate.getCurrentDrawNumber()) }
    }

    fun onEvent(event: QrScanEvent) {
        when (event) {
            is QrScanEvent.StartScan -> startScan()
            is QrScanEvent.StopScan -> stopScan()
            is QrScanEvent.ResetAfterSuccess -> resetAfterSuccess()
            is QrScanEvent.ToggleFlash -> toggleFlash()
            is QrScanEvent.ProcessQrCode -> processQrCode(event.content, event.bounds)
            is QrScanEvent.UpdateDetectedBounds -> updateDetectedBounds(event.bounds)
            is QrScanEvent.RequestFocus -> requestFocus(event.x, event.y)
            is QrScanEvent.ToggleListExpansion -> toggleListExpansion()
            is QrScanEvent.SaveScannedTicket -> saveScannedTicket(event.qrUrl, event.forceOverwrite)
            is QrScanEvent.ConfirmDuplicateSave -> confirmDuplicateSave()
            is QrScanEvent.CancelDuplicateSave -> cancelDuplicateSave()
        }
    }

    private fun startScan() {
        _state.update {
            it.copy(
                isScanning = true,
                error = null
                // detectedBounds는 유지 (새 스캔 시에도 이전 박스 위치 유지)
            )
        }
    }

    private fun stopScan() {
        _state.update {
            it.copy(
                isScanning = false
                // detectedBounds는 유지 (스캔 중지해도 박스는 유지)
            )
        }
    }

    private fun resetAfterSuccess() {
        _state.update {
            it.copy(
                isScanning = true,
                scannedResult = null,
                isSuccess = false,
                error = null
                // detectedBounds는 유지 (성공 후에도 박스 위치 유지)
            )
        }
    }

    private fun toggleFlash() {
        _state.update {
            it.copy(isFlashEnabled = !it.isFlashEnabled)
        }
    }

    private fun updateDetectedBounds(bounds: QrCodeBounds?) {
        _state.update {
            if (bounds != null) {
                // QR이 감지되면 bounds 업데이트
                it.copy(detectedBounds = bounds)
            } else {
                // QR이 일시적으로 감지되지 않아도 기존 bounds 유지
                it
            }
        }
    }

    private fun processQrCode(content: String, bounds: QrCodeBounds) {
        // 이미 스캔된 결과가 있으면 무시 (저장 중이거나 중복 확인 중)
        if (_state.value.scannedResult != null) {
            Log.d("whk__", "Already have scanned result, ignoring")
            return
        }
        
        // 중복 확인 다이얼로그가 떠있으면 무시
        if (_state.value.duplicateConfirmation != null) {
            Log.d("whk__", "Duplicate confirmation in progress, ignoring")
            return
        }
        
        // 이미 처리 중이면 무시 (다중 QR 동시 처리 방지)
        if (isProcessingQr) {
            Log.d("whk__", "Already processing a QR, ignoring")
            return
        }
        
        // 마지막으로 처리한 QR과 같으면 무시
        if (content == lastProcessedQrUrl) {
            Log.d("whk__", "Same QR as last processed, ignoring")
            return
        }

        viewModelScope.launch {
            // 처리 시작
            isProcessingQr = true
            
            try {
                val ticketInfo = LottoQrParser.parse(content)
                Log.d("whk__", "ticketInfo : $ticketInfo")
                if (ticketInfo != null) {
                    // 마지막 처리한 QR URL 저장
                    lastProcessedQrUrl = content

                    // 새로운 QR 처리 시작: 이전 결과 카드 즉시 제거
                    _state.update {
                        it.copy(
                            scannedResult = ticketInfo,
                            detectedBounds = bounds,
                            isScanning = false,
                            error = null,
                            isCheckingWinning = true,
                            lastSavedTicket = null  // 이전 카드 제거
                        )
                    }
                    _effect.send(QrScanEffect.VibrateScan)

                    // 당첨 확인 (비동기로 수행하되, 실패해도 저장은 진행)
                    val winningDetail = checkWinning(ticketInfo)

                    _state.update {
                        it.copy(
                            isCheckingWinning = false,
                            currentWinningDetail = winningDetail
                        )
                    }
                    if (winningDetail?.gameResults?.any { it.rank in 1..5 } == true) {
                        _effect.send(QrScanEffect.VibrateWinning)
                    }

                    // 즉시 저장 시도 (당첨 정보와 함께)
                    // 저장이 완료되면 saveScannedTicket 내부에서 isProcessingQr를 해제
                    saveScannedTicket(content)
                } else {
                    _state.update {
                        it.copy(
                            error = "유효하지 않은 로또 QR 코드입니다",
                            isScanning = true,
                            // detectedBounds는 유지 (박스가 사라지지 않도록)
                            isSuccess = false
                        )
                    }
                    _effect.send(QrScanEffect.ShowError("유효하지 않은 로또 QR 코드입니다"))
                    // 유효하지 않은 QR이면 즉시 플래그 해제
                    isProcessingQr = false
                }
            } catch (e: Exception) {
                Log.e("whk__", "QR 처리 중 에러: ${e.message}")
                isProcessingQr = false
            }
        }
    }

    private suspend fun checkWinning(ticketInfo: com.enso.qrscan.parser.LottoTicketInfo): TicketWinningDetail? {
        return try {
            val lottoResult = getLottoResultUseCase(ticketInfo.round).getOrNull() ?: return null

            val gameResults = ticketInfo.games.mapIndexed { index, gameInfo ->
                val matchedCount = gameInfo.numbers.count { it in lottoResult.numbers }
                val bonusMatched = lottoResult.bonusNumber in gameInfo.numbers

                val rank = when {
                    matchedCount == 6 -> 1               // 1등: 6개 일치
                    matchedCount == 5 && bonusMatched -> 2  // 2등: 5개 + 보너스
                    matchedCount == 5 -> 3               // 3등: 5개 일치
                    matchedCount == 4 -> 4               // 4등: 4개 일치
                    matchedCount == 3 -> 5               // 5등: 3개 일치
                    else -> 0                            // 낙첨
                }

                GameWinningInfo(
                    gameLabel = ('A' + index).toString(),
                    rank = rank,
                    matchedCount = matchedCount,
                    bonusMatched = bonusMatched
                )
            }

            TicketWinningDetail(
                winningNumbers = lottoResult.numbers,
                bonusNumber = lottoResult.bonusNumber,
                gameResults = gameResults,
                firstPrizeAmount = lottoResult.firstPrize.winAmount,
                drawDate = lottoResult.drawDate
            )
        } catch (e: Exception) {
            Log.e("whk__", "당첨 확인 실패: ${e.message}")
            null
        }
    }

    private fun saveScannedTicket(qrUrl: String, forceOverwrite: Boolean = false) {
        val ticketInfo = _state.value.scannedResult ?: return
        val winningDetail = _state.value.currentWinningDetail

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            // 강제 저장인 경우 기존 티켓 삭제
            if (forceOverwrite) {
                saveLottoTicketUseCase.deleteTicketByQrUrl(qrUrl)
            }

            val games = ticketInfo.games.mapIndexed { index, gameInfo ->
                LottoGame(
                    gameLabel = ('A' + index).toString(),
                    numbers = gameInfo.numbers,
                    gameType = if (gameInfo.isAuto) GameType.AUTO else GameType.MANUAL
                )
            }

            val ticket = LottoTicket(
                round = ticketInfo.round,
                registeredDate = Date(),
                games = games,
                qrUrl = qrUrl
            )

            val currentRound = _state.value.currentRound
            val drawDate = winningDetail?.drawDate
                ?: if (ticketInfo.round > currentRound) {
                    LottoDate.getDrawDateTimeByNumber(ticketInfo.round).time
                } else {
                    null
                }

            saveLottoTicketUseCase(ticket).fold(
                onSuccess = {
                    val summary = SavedTicketSummary(
                        round = ticketInfo.round,
                        gameCount = ticketInfo.games.size,
                        games = ticketInfo.games.mapIndexed { index, gameInfo ->
                            ScannedGameSummary(
                                gameLabel = ('A' + index).toString(),
                                numbers = gameInfo.numbers,
                                isAuto = gameInfo.isAuto
                            )
                        },
                        winningNumbers = winningDetail?.winningNumbers,
                        bonusNumber = winningDetail?.bonusNumber,
                        winningResults = winningDetail?.gameResults,
                        winningCheckFailed = winningDetail == null,
                        firstPrizeAmount = winningDetail?.firstPrizeAmount,
                        drawDate = drawDate
                    )
                    _state.update {
                        it.copy(
                            isSaving = false,
                            savedTickets = listOf(summary) + it.savedTickets,
                            lastSavedTicket = summary,
                            scannedResult = null,
                            detectedBounds = null,  // 새 QR 스캔을 위해 박스 초기화
                            isSuccess = false,
                            isScanning = true,
                            currentWinningDetail = null,
                            duplicateConfirmation = null
                        )
                    }
                    // 저장 성공: 다음 QR 처리 가능 (lastProcessedQrUrl은 유지하여 같은 QR 재인식 방지)
                    isProcessingQr = false
                },
                onFailure = { error ->
                    _state.update { it.copy(isSaving = false) }
                    if (error is DuplicateQrException && !forceOverwrite) {
                        // 중복 확인 다이얼로그 표시 - 플래그는 유지 (사용자가 선택할 때까지)
                        _state.update {
                            it.copy(
                                duplicateConfirmation = DuplicateConfirmation(
                                    qrUrl = qrUrl,
                                    existingRound = ticketInfo.round
                                )
                            )
                        }
                        // 중복 다이얼로그 상태에서는 플래그 유지 (다른 QR 인식 방지)
                    } else if (error is DuplicateQrException && forceOverwrite) {
                        // forceOverwrite인데도 중복이면, 삭제가 실패했거나 다른 문제
                        _effect.send(QrScanEffect.ShowError("저장 실패: 중복 제거 후에도 저장할 수 없습니다"))
                        resumeScanning()
                        // 에러 발생: 다음 QR 처리 가능 (lastProcessedQrUrl은 유지)
                        isProcessingQr = false
                    } else {
                        _effect.send(QrScanEffect.ShowError(error.message ?: "저장 실패"))
                        resumeScanning()
                        // 에러 발생: 다음 QR 처리 가능 (lastProcessedQrUrl은 유지)
                        isProcessingQr = false
                    }
                }
            )
        }
    }

    private fun confirmDuplicateSave() {
        val confirmation = _state.value.duplicateConfirmation ?: return
        
        viewModelScope.launch {
            // 중복 배너 먼저 제거 (애니메이션을 위한 시각적 피드백)
            _state.update {
                it.copy(duplicateConfirmation = null)
            }
            
            // 약간의 딜레이 후 저장 (부드러운 전환)
            delay(100)
            
            // 강제 저장 수행 (saveScannedTicket에서 플래그 해제 처리)
            saveScannedTicket(confirmation.qrUrl, forceOverwrite = true)
        }
    }

    private fun cancelDuplicateSave() {
        val ticketInfo = _state.value.scannedResult ?: return
        val winningDetail = _state.value.currentWinningDetail
        val currentRound = _state.value.currentRound
        
        viewModelScope.launch {
            // 중복 배너 먼저 제거 (저장 시와 동일한 흐름)
            _state.update {
                it.copy(duplicateConfirmation = null)
            }
            
            // 약간의 딜레이 후 처리 (저장 시와 동일)
            delay(100)
            
            // 스캔 결과를 표시용 리스트에 추가 (DB에는 저장하지 않음)
            val drawDate = winningDetail?.drawDate
                ?: if (ticketInfo.round > currentRound) {
                    LottoDate.getDrawDateTimeByNumber(ticketInfo.round).time
                } else {
                    null
                }
            
            val summary = SavedTicketSummary(
                round = ticketInfo.round,
                gameCount = ticketInfo.games.size,
                games = ticketInfo.games.mapIndexed { index, gameInfo ->
                    ScannedGameSummary(
                        gameLabel = ('A' + index).toString(),
                        numbers = gameInfo.numbers,
                        isAuto = gameInfo.isAuto
                    )
                },
                winningNumbers = winningDetail?.winningNumbers,
                bonusNumber = winningDetail?.bonusNumber,
                winningResults = winningDetail?.gameResults,
                winningCheckFailed = winningDetail == null,
                firstPrizeAmount = winningDetail?.firstPrizeAmount,
                drawDate = drawDate
            )
            
            // 저장 시와 동��하게 상태 업데이트 (DB 저장 없이 메모리에만)
            _state.update {
                it.copy(
                    savedTickets = listOf(summary) + it.savedTickets,
                    lastSavedTicket = summary,
                    scannedResult = null,  // 임시 스캔 결과 제거
                    detectedBounds = null,  // 박스 초기화
                    isSuccess = false,
                    isScanning = true,
                    currentWinningDetail = null
                )
            }
            
            // 취소: 다음 QR 처리 가능 (lastProcessedQrUrl은 유지하여 같은 QR 재인식 방지)
            isProcessingQr = false
        }
    }

    private fun resumeScanning() {
        _state.update {
            it.copy(
                scannedResult = null,
                detectedBounds = null,  // 에러 후 재시작 시 박스 초기화
                isSuccess = false,
                isScanning = true,
                currentWinningDetail = null
            )
        }
    }

    private fun toggleListExpansion() {
        _state.update { it.copy(isListExpanded = !it.isListExpanded) }
    }

    private fun requestFocus(x: Float, y: Float) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    focusPoint = Offset(x, y),
                    isFocusing = true
                )
            }
            // 0.3초 후 포커스 애니메이션 종료
            delay(300L)
            _state.update {
                it.copy(isFocusing = false)
            }
        }
    }
}
