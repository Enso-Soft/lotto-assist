package com.enso.qrscan

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import com.enso.qrscan.parser.LottoTicketInfo

data class QrCodeBounds(
    val cornerPoints: List<PointF>,
    val sourceImageWidth: Int,
    val sourceImageHeight: Int,
    val rotationDegrees: Int
)

data class SavedTicketSummary(
    val round: Int,
    val gameCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class QrScanUiState(
    val isScanning: Boolean = false,
    val scannedResult: LottoTicketInfo? = null,
    val detectedBounds: QrCodeBounds? = null,
    val isCurrentlyDetected: Boolean = false,
    val isSuccess: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val error: String? = null,
    val focusPoint: Offset? = null,
    val isFocusing: Boolean = false,
    val savedTickets: List<SavedTicketSummary> = emptyList(),
    val isListExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val lastSavedTicket: SavedTicketSummary? = null
)

sealed class QrScanEvent {
    data object StartScan : QrScanEvent()
    data object StopScan : QrScanEvent()
    data object ResetAfterSuccess : QrScanEvent()
    data object ToggleFlash : QrScanEvent()
    data class ProcessQrCode(val content: String, val bounds: QrCodeBounds) : QrScanEvent()
    data class UpdateDetectedBounds(val bounds: QrCodeBounds?) : QrScanEvent()
    data class RequestFocus(val x: Float, val y: Float) : QrScanEvent()
    data object ToggleListExpansion : QrScanEvent()
    data class SaveScannedTicket(val qrUrl: String) : QrScanEvent()
}

sealed class QrScanEffect {
    data class ShowError(val message: String) : QrScanEffect()
    data object ShowDuplicateMessage : QrScanEffect()
}
