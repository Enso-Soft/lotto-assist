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
    private val saveLottoTicketUseCase: SaveLottoTicketUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QrScanUiState())
    val state: StateFlow<QrScanUiState> = _state.asStateFlow()

    private val _effect = Channel<QrScanEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

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
            is QrScanEvent.SaveScannedTicket -> saveScannedTicket(event.qrUrl)
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
        viewModelScope.launch {
            val ticketInfo = LottoQrParser.parse(content)
            Log.d("whk__", "ticketInfo : $ticketInfo")
            if (ticketInfo != null) {
                // scannedResult를 먼저 설정
                _state.update {
                    it.copy(
                        scannedResult = ticketInfo,
                        detectedBounds = bounds,
                        isScanning = false,
                        error = null
                    )
                }
                // 즉시 저장 시도
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

    private fun saveScannedTicket(qrUrl: String) {
        val ticketInfo = _state.value.scannedResult ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true) }

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
                        gameCount = ticketInfo.games.size
                    )
                    _state.update {
                        it.copy(
                            isSaving = false,
                            savedTickets = it.savedTickets + summary,
                            lastSavedTicket = summary,
                            scannedResult = null,
                            detectedBounds = null,
                            isSuccess = false,
                            isScanning = true
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(isSaving = false) }
                    if (error is DuplicateQrException) {
                        _effect.send(QrScanEffect.ShowDuplicateMessage)
                    } else {
                        _effect.send(QrScanEffect.ShowError(error.message ?: "저장 실패"))
                    }
                    // 스캔 재개
                    _state.update {
                        it.copy(
                            scannedResult = null,
                            detectedBounds = null,
                            isSuccess = false,
                            isScanning = true
                        )
                    }
                }
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
