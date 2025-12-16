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
    val isCurrentlyDetected: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class QrScanEvent {
    data object StartScan : QrScanEvent()
    data object StopScan : QrScanEvent()
    data object ResetAfterSuccess : QrScanEvent()
    data class ProcessQrCode(val content: String, val bounds: QrCodeBounds) : QrScanEvent()
    data class UpdateDetectedBounds(val bounds: QrCodeBounds?) : QrScanEvent()
}

sealed class QrScanEffect {
    data class ShowError(val message: String) : QrScanEffect()
}
