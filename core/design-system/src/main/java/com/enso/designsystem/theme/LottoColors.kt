package com.enso.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Extended semantic colors for Lotto app
 * These colors are exposed via LottoTheme.colors
 */
@Immutable
data class LottoColors(
    // Lotto Ball Colors (consistent across themes)
    val ballYellow: Color,
    val ballBlue: Color,
    val ballRed: Color,
    val ballGrey: Color,
    val ballGreen: Color,
    val ballTextColor: Color,

    // Success (Winning) Colors
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,

    // Warning Colors
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,

    // Info Colors
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,

    // Text Hierarchy (Semantic aliases)
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val textLink: Color,

    // Legacy colors for backward compatibility (will be removed in future)
    val backgroundLight: Color,
    val backgroundDark: Color,
    val cardLight: Color,
    val cardDark: Color,
    val textMainLight: Color,
    val textMainDark: Color,
    val textSubLight: Color,
    val textSubDark: Color,
    val primary: Color,
    val winningGreen: Color,
    val losingRed: Color,
)

/**
 * Light theme extended colors
 */
internal val LightLottoColors = LottoColors(
    // Ball colors (same for light/dark)
    ballYellow = Color(0xFFFBC400),
    ballBlue = Color(0xFF69C8F2),
    ballRed = Color(0xFFFF7272),
    ballGrey = Color(0xFFB0B0B0),
    ballGreen = Color(0xFFB0D840),
    ballTextColor = Color(0xFFFFFFFF),

    // Success
    success = Color(0xFF4CAF50),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFD4EDDA),
    onSuccessContainer = Color(0xFF0F5323),

    // Warning
    warning = Color(0xFFFFC107),
    onWarning = Color(0xFF000000),
    warningContainer = Color(0xFFFFF8E1),
    onWarningContainer = Color(0xFF3D2E00),

    // Info
    info = Color(0xFF2196F3),
    onInfo = Color(0xFFFFFFFF),
    infoContainer = Color(0xFFE3F2FD),
    onInfoContainer = Color(0xFF0D3C61),

    // Text
    textPrimary = Color(0xFF191F28),
    textSecondary = Color(0xFF8B95A1),
    textDisabled = Color(0xFFB0B5BC),
    textLink = Color(0xFF137FEC),

    // Legacy colors
    backgroundLight = Color(0xFFF2F4F6),
    backgroundDark = Color(0xFF101922),
    cardLight = Color(0xFFFFFFFF),
    cardDark = Color(0xFF1B2631),
    textMainLight = Color(0xFF191F28),
    textMainDark = Color(0xFFFFFFFF),
    textSubLight = Color(0xFF8B95A1),
    textSubDark = Color(0xFF92ADC9),
    primary = Color(0xFF137FEC),
    winningGreen = Color(0xFF4CAF50),
    losingRed = Color(0xFFF44336),
)

/**
 * Dark theme extended colors
 */
internal val DarkLottoColors = LottoColors(
    // Ball colors (same for light/dark)
    ballYellow = Color(0xFFFBC400),
    ballBlue = Color(0xFF69C8F2),
    ballRed = Color(0xFFFF7272),
    ballGrey = Color(0xFFB0B0B0),
    ballGreen = Color(0xFFB0D840),
    ballTextColor = Color(0xFFFFFFFF),

    // Success
    success = Color(0xFF9CCC9C),
    onSuccess = Color(0xFF0F3815),
    successContainer = Color(0xFF1B5E20),
    onSuccessContainer = Color(0xFFC8E6C9),

    // Warning
    warning = Color(0xFFFFCC80),
    onWarning = Color(0xFF3D2E00),
    warningContainer = Color(0xFF5D4200),
    onWarningContainer = Color(0xFFFFECB3),

    // Info
    info = Color(0xFF90CAF9),
    onInfo = Color(0xFF0D3C61),
    infoContainer = Color(0xFF1565C0),
    onInfoContainer = Color(0xFFBBDEFB),

    // Text
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF92ADC9),
    textDisabled = Color(0xFF6B7280),
    textLink = Color(0xFFA4C9FF),

    // Legacy colors
    backgroundLight = Color(0xFFF2F4F6),
    backgroundDark = Color(0xFF101922),
    cardLight = Color(0xFFFFFFFF),
    cardDark = Color(0xFF1B2631),
    textMainLight = Color(0xFF191F28),
    textMainDark = Color(0xFFFFFFFF),
    textSubLight = Color(0xFF8B95A1),
    textSubDark = Color(0xFF92ADC9),
    primary = Color(0xFF137FEC),
    winningGreen = Color(0xFF4CAF50),
    losingRed = Color(0xFFF44336),
)

/**
 * Get lotto ball color by number
 *
 * @param number 로또 번호 (1-45)
 * @param lottoColors 테마별 Lotto 색상
 * @return 해당 번호의 공 색상
 */
fun getLottoBallColor(number: Int, lottoColors: LottoColors): Color {
    return when (number) {
        in 1..10 -> lottoColors.ballYellow
        in 11..20 -> lottoColors.ballBlue
        in 21..30 -> lottoColors.ballRed
        in 31..40 -> lottoColors.ballGrey
        in 41..45 -> lottoColors.ballGreen
        else -> lottoColors.ballGrey // Fallback for invalid numbers
    }
}
