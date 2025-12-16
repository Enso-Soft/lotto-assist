package com.enso.qrscan

import android.graphics.PointF
import com.enso.qrscan.parser.LottoTicketInfo

data class QrCodeBounds(
    val cornerPoints: List<PointF>,
    val sourceImageWidth: Int,
    val sourceImageHeight: Int,
    val rotationDegrees: Int
)

data class QrScanUiState(
    val isScanning: Boolean = false,
    val scannedResult: LottoTicketInfo? = null,
    val detectedBounds: QrCodeBounds? = null,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class QrScanEvent {
    data object StartScan : QrScanEvent()
    data object StopScan : QrScanEvent()
    data class ProcessQrCode(val content: String, val bounds: QrCodeBounds) : QrScanEvent()
    data class UpdateDetectedBounds(val bounds: QrCodeBounds?) : QrScanEvent()
}

sealed class QrScanEffect {
    data class ShowError(val message: String) : QrScanEffect()
    data class ScanSuccess(val ticketInfo: LottoTicketInfo) : QrScanEffect()
}
