package com.enso.qrscan

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.qrscan.parser.LottoQrParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScanViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(QrScanUiState())
    val state: StateFlow<QrScanUiState> = _state.asStateFlow()

    private val _effect = Channel<QrScanEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: QrScanEvent) {
        when (event) {
            is QrScanEvent.StartScan -> startScan()
            is QrScanEvent.StopScan -> stopScan()
            is QrScanEvent.ProcessQrCode -> processQrCode(event.content)
        }
    }

    private fun startScan() {
        _state.update { it.copy(isScanning = true, error = null) }
    }

    private fun stopScan() {
        _state.update { it.copy(isScanning = false) }
    }

    private fun processQrCode(content: String) {
        viewModelScope.launch {
            val ticketInfo = LottoQrParser.parse(content)
            Log.d("whk__", "ticketInfo : $ticketInfo")
            if (ticketInfo != null) {
                _state.update {
                    it.copy(
                        scannedResult = ticketInfo,
                        isScanning = false
                    )
                }
                _effect.send(QrScanEffect.ScanSuccess(ticketInfo))
            } else {
                _state.update {
                    it.copy(
                        error = "유효하지 않은 로또 QR 코드입니다",
                        isScanning = true
                    )
                }
                _effect.send(QrScanEffect.ShowError("유효하지 않은 로또 QR 코드입니다"))
            }
        }
    }
}
