package com.enso.qrscan

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.annotation.StringRes
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
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
    val isFlashEnabled: Boolean = false,
    val error: String? = null,
    val focusPoint: Offset? = null,
    val isFocusing: Boolean = false,
    val tickets: List<LottoTicket> = emptyList(),
    val lottoResults: List<LottoResult> = emptyList(),
    val currentRound: Int = 0,
    val lastScanResult: QrScanResult? = null,
    val isSaving: Boolean = false
)

enum class QrScanResult {
    Saved,
    Duplicate
}

sealed class QrScanEvent {
    data object StartScan : QrScanEvent()
    data object StopScan : QrScanEvent()
    data object ResetAfterSuccess : QrScanEvent()
    data object ToggleFlash : QrScanEvent()
    data class ProcessQrCode(val content: String, val bounds: QrCodeBounds) : QrScanEvent()
    data class UpdateDetectedBounds(val bounds: QrCodeBounds?) : QrScanEvent()
    data class RequestFocus(val x: Float, val y: Float) : QrScanEvent()
    data class DeleteTicket(val ticketId: Long) : QrScanEvent()
    data class CheckWinning(val ticketId: Long) : QrScanEvent()
}

sealed class QrScanEffect {
    data class ShowMessage(@StringRes val messageRes: Int) : QrScanEffect()
}
