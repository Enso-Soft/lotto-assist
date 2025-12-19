package com.enso.qrscan

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.exception.DuplicateQrException
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
import com.enso.domain.usecase.CheckTicketWinningUseCase
import com.enso.domain.usecase.DeleteLottoTicketUseCase
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLottoTicketsUseCase
import com.enso.domain.usecase.SaveLottoTicketUseCase
import com.enso.qrscan.parser.LottoQrParser
import com.enso.util.lotto_date.LottoDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import com.enso.qrscan.R

@HiltViewModel
class QrScanViewModel @Inject constructor(
    private val saveLottoTicketUseCase: SaveLottoTicketUseCase,
    private val getLottoTicketsUseCase: GetLottoTicketsUseCase,
    private val getAllLottoResultsUseCase: GetAllLottoResultsUseCase,
    private val deleteLottoTicketUseCase: DeleteLottoTicketUseCase,
    private val checkTicketWinningUseCase: CheckTicketWinningUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QrScanUiState())
    val state: StateFlow<QrScanUiState> = _state.asStateFlow()

    private val _effect = Channel<QrScanEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        _state.update { it.copy(currentRound = LottoDate.getCurrentDrawNumber()) }
        observeTickets()
        observeLottoResults()
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
            is QrScanEvent.DeleteTicket -> deleteTicket(event.ticketId)
            is QrScanEvent.CheckWinning -> checkWinning(event.ticketId)
        }
    }

    private fun startScan() {
        _state.update {
            it.copy(
                isScanning = true,
                error = null,
                detectedBounds = null,
                isCurrentlyDetected = false,
                lastScanResult = null,
                isSaving = false
            )
        }
    }

    private fun stopScan() {
        _state.update {
            it.copy(
                isScanning = false,
                detectedBounds = null,
                isCurrentlyDetected = false,
                isSaving = false
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
                error = null,
                lastScanResult = null,
                isSaving = false
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
            if (_state.value.isSaving || _state.value.isSuccess) return@launch
            _state.update { it.copy(isSaving = true) }
            val ticketInfo = LottoQrParser.parse(content)
            Log.d("whk__", "ticketInfo : $ticketInfo")
            if (ticketInfo != null) {
                val games = ticketInfo.games.mapIndexed { index, gameInfo ->
                    val gameLabel = ('A' + index).toString()
                    LottoGame(
                        gameLabel = gameLabel,
                        numbers = gameInfo.numbers.sorted(),
                        gameType = if (gameInfo.isAuto) GameType.AUTO else GameType.MANUAL,
                        winningRank = 0
                    )
                }
                val ticket = LottoTicket(
                    round = ticketInfo.round,
                    registeredDate = Date(),
                    isChecked = false,
                    games = games,
                    qrUrl = ticketInfo.qrUrl
                )

                saveLottoTicketUseCase(ticket)
                    .onSuccess {
                        _state.update {
                            it.copy(
                                scannedResult = ticketInfo,
                                isScanning = false,
                                detectedBounds = bounds,
                                isSuccess = true,
                                error = null,
                                lastScanResult = QrScanResult.Saved,
                                isSaving = false
                            )
                        }
                    }
                    .onFailure { e ->
                        when (e) {
                            is DuplicateQrException -> {
                                _state.update {
                                    it.copy(
                                        scannedResult = ticketInfo,
                                        isScanning = false,
                                        detectedBounds = bounds,
                                        isSuccess = true,
                                        error = null,
                                        lastScanResult = QrScanResult.Duplicate,
                                        isSaving = false
                                    )
                                }
                                _effect.send(QrScanEffect.ShowMessage(R.string.qr_scan_duplicate))
                            }
                            else -> {
                                _state.update {
                                    it.copy(
                                        error = "SAVE_FAILED",
                                        isScanning = true,
                                        isSuccess = false,
                                        lastScanResult = null,
                                        isSaving = false
                                    )
                                }
                                _effect.send(QrScanEffect.ShowMessage(R.string.qr_scan_save_failed))
                            }
                        }
                    }
            } else {
                _state.update {
                    it.copy(
                        error = "INVALID_QR",
                        isScanning = true,
                        // detectedBounds는 유지 (박스가 사라지지 않도록)
                        isSuccess = false,
                        lastScanResult = null,
                        isSaving = false
                    )
                }
                _effect.send(QrScanEffect.ShowMessage(R.string.qr_scan_invalid))
            }
        }
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

    private fun observeTickets() {
        viewModelScope.launch {
            getLottoTicketsUseCase(TicketSortType.DEFAULT)
                .collectLatest { tickets ->
                    _state.update { it.copy(tickets = tickets) }
                }
        }
    }

    private fun observeLottoResults() {
        viewModelScope.launch {
            getAllLottoResultsUseCase()
                .collectLatest { results ->
                    _state.update { it.copy(lottoResults = results) }
                }
        }
    }

    private fun deleteTicket(ticketId: Long) {
        viewModelScope.launch {
            deleteLottoTicketUseCase(ticketId)
                .onFailure { e ->
                    _effect.send(QrScanEffect.ShowMessage(R.string.qr_scan_delete_failed))
                }
        }
    }

    private fun checkWinning(ticketId: Long) {
        viewModelScope.launch {
            val ticket = _state.value.tickets.find { it.ticketId == ticketId } ?: return@launch
            checkTicketWinningUseCase(ticket)
                .onFailure { e ->
                    _effect.send(QrScanEffect.ShowMessage(R.string.qr_scan_check_failed))
                }
        }
    }
}
