package com.enso.qrscan

import com.enso.qrscan.parser.LottoTicketInfo

data class QrScanUiState(
    val isScanning: Boolean = false,
    val scannedResult: LottoTicketInfo? = null,
    val error: String? = null
)

sealed class QrScanEvent {
    data object StartScan : QrScanEvent()
    data object StopScan : QrScanEvent()
    data class ProcessQrCode(val content: String) : QrScanEvent()
}

sealed class QrScanEffect {
    data class ShowError(val message: String) : QrScanEffect()
    data class ScanSuccess(val ticketInfo: LottoTicketInfo) : QrScanEffect()
}
