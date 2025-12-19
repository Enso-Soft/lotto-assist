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
                error = null,
                detectedBounds = null,
                isCurrentlyDetected = false
            )
        }
    }

    private fun stopScan() {
        _state.update {
            it.copy(
                isScanning = false,
                detectedBounds = null,
                isCurrentlyDetected = false
            )
        }
    }

    private fun resetAfterSuccess() {
        _state.update {
            it.copy(
                isScanning = true,
                scannedResult = null,
                detectedBounds = null,
                isCurrentlyDetected = false,
                isSuccess = false,
                error = null
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
            if (bounds == null) {
                // 더 이상 인식되지 않더라도 마지막 박스 위치는 유지
                it.copy(isCurrentlyDetected = false)
            } else {
                it.copy(detectedBounds = bounds, isCurrentlyDetected = true)
            }
        }
    }

    private fun processQrCode(content: String, bounds: QrCodeBounds) {
        // 마지막으로 처리한 QR과 같으면 무시
        if (content == lastProcessedQrUrl) {
            Log.d("whk__", "Same QR as last processed, ignoring")
            return
        }

        viewModelScope.launch {
            val ticketInfo = LottoQrParser.parse(content)
            Log.d("whk__", "ticketInfo : $ticketInfo")
            if (ticketInfo != null) {
                // 마지막 처리한 QR URL 저장
                lastProcessedQrUrl = content

                // scannedResult를 먼저 설정
                _state.update {
                    it.copy(
                        scannedResult = ticketInfo,
                        detectedBounds = bounds,
                        isScanning = false,
                        error = null,
                        isCheckingWinning = true
                    )
                }

                // 당첨 확인 (비동기로 수행하되, 실패해도 저장은 진행)
                val winningResults = checkWinning(ticketInfo)

                _state.update {
                    it.copy(
                        isCheckingWinning = false,
                        currentWinningResults = winningResults
                    )
                }

                // 즉시 저장 시도 (당첨 정보와 함께)
                onEvent(QrScanEvent.SaveScannedTicket(content))
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
            }
        }
    }

    private suspend fun checkWinning(ticketInfo: com.enso.qrscan.parser.LottoTicketInfo): List<GameWinningInfo>? {
        return try {
            val lottoResult = getLottoResultUseCase(ticketInfo.round).getOrNull() ?: return null

            ticketInfo.games.mapIndexed { index, gameInfo ->
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
        } catch (e: Exception) {
            Log.e("whk__", "당첨 확인 실패: ${e.message}")
            null
        }
    }

    private fun saveScannedTicket(qrUrl: String, forceOverwrite: Boolean = false) {
        val ticketInfo = _state.value.scannedResult ?: return
        val winningResults = _state.value.currentWinningResults

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

            saveLottoTicketUseCase(ticket).fold(
                onSuccess = {
                    val summary = SavedTicketSummary(
                        round = ticketInfo.round,
                        gameCount = ticketInfo.games.size,
                        winningResults = winningResults,
                        winningCheckFailed = winningResults == null
                    )
                    _state.update {
                        it.copy(
                            isSaving = false,
                            savedTickets = it.savedTickets + summary,
                            lastSavedTicket = summary,
                            scannedResult = null,
                            detectedBounds = null,
                            isSuccess = false,
                            isScanning = true,
                            currentWinningResults = null,
                            duplicateConfirmation = null
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isSaving = false) }
                    if (error is DuplicateQrException && !forceOverwrite) {
                        // 중복 확인 다이얼로그 표시
                        _state.update {
                            it.copy(
                                duplicateConfirmation = DuplicateConfirmation(
                                    qrUrl = qrUrl,
                                    existingRound = ticketInfo.round
                                )
                            )
                        }
                    } else if (error is DuplicateQrException && forceOverwrite) {
                        // forceOverwrite인데도 중복이면, 삭제가 실패했거나 다른 문제
                        _effect.send(QrScanEffect.ShowError("저장 실패: 중복 제거 후에도 저장할 수 없습니다"))
                        resumeScanning()
                    } else {
                        _effect.send(QrScanEffect.ShowError(error.message ?: "저장 실패"))
                        resumeScanning()
                    }
                }
            )
        }
    }

    private fun confirmDuplicateSave() {
        val confirmation = _state.value.duplicateConfirmation ?: return
        // 강제 저장 수행
        onEvent(QrScanEvent.SaveScannedTicket(confirmation.qrUrl, forceOverwrite = true))
    }

    private fun cancelDuplicateSave() {
        _state.update {
            it.copy(duplicateConfirmation = null)
        }
        resumeScanning()
    }

    private fun resumeScanning() {
        _state.update {
            it.copy(
                scannedResult = null,
                detectedBounds = null,
                isSuccess = false,
                isScanning = true,
                currentWinningResults = null
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
