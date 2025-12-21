package com.enso.qrscan

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import com.enso.qrscan.parser.LottoTicketInfo
import java.util.Date

data class QrCodeBounds(
    val cornerPoints: List<PointF>,
    val sourceImageWidth: Int,
    val sourceImageHeight: Int,
    val rotationDegrees: Int
)

data class GameWinningInfo(
    val gameLabel: String,
    val rank: Int,  // 0: 낙첨, 1~5: 등수
    val matchedCount: Int,
    val bonusMatched: Boolean = false
)

data class ScannedGameSummary(
    val gameLabel: String,
    val numbers: List<Int>,
    val isAuto: Boolean
)

data class TicketWinningDetail(
    val winningNumbers: List<Int>,
    val bonusNumber: Int,
    val gameResults: List<GameWinningInfo>,
    val firstPrizeAmount: Long,
    val drawDate: Date
)

data class SavedTicketSummary(
    val round: Int,
    val gameCount: Int,
    val games: List<ScannedGameSummary>,
    val timestamp: Long = System.currentTimeMillis(),
    val winningNumbers: List<Int>? = null,
    val bonusNumber: Int? = null,
    val winningResults: List<GameWinningInfo>? = null,  // null이면 당첨 확인 불가
    val winningCheckFailed: Boolean = false,  // 당첨 확인 실패 여부
    val firstPrizeAmount: Long? = null,
    val drawDate: Date? = null
)

data class DuplicateConfirmation(
    val qrUrl: String,
    val existingRound: Int
)

data class QrScanUiState(
    val isScanning: Boolean = false,
    val scannedResult: LottoTicketInfo? = null,
    // detectedBounds는 리컴포지션 최적화를 위해 별도 StateFlow로 분리됨
    // ViewModel.detectedBounds를 직접 수집하여 사용
    val isSuccess: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val error: String? = null,
    val focusPoint: Offset? = null,
    val isFocusing: Boolean = false,
    val savedTickets: List<SavedTicketSummary> = emptyList(),
    val currentRound: Int = 0,
    val isListExpanded: Boolean = false,
    val isSaving: Boolean = false,
    val isCheckingWinning: Boolean = false,  // 당첨 확인 중
    val currentWinningDetail: TicketWinningDetail? = null,  // 현재 스캔된 티켓의 당첨 결과
    val lastSavedTicket: SavedTicketSummary? = null,
    val duplicateConfirmation: DuplicateConfirmation? = null  // 중복 확인 다이얼로그 상태
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
    data class SaveScannedTicket(val qrUrl: String, val forceOverwrite: Boolean = false) : QrScanEvent()
    data object ConfirmDuplicateSave : QrScanEvent()  // 중복 저장 확인
    data object CancelDuplicateSave : QrScanEvent()  // 중복 저장 취소
}

sealed class QrScanEffect {
    data class ShowError(val message: String) : QrScanEffect()
    data object ShowDuplicateMessage : QrScanEffect()
    data object VibrateScan : QrScanEffect()
    data object VibrateWinning : QrScanEffect()
}
