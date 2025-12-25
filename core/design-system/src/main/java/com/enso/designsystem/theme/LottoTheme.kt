package com.enso.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * LottoTheme
 * Main theme wrapper providing Material3 theme + extended Lotto tokens
 *
 * @param darkTheme 다크 테마 사용 여부 (기본값: 시스템 설정)
 * @param content 테마를 적용할 컨텐츠
 */
@Composable
fun LottoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val lottoColors = if (darkTheme) DarkLottoColors else LightLottoColors
    val lottoSpacing = LottoSpacing()

    CompositionLocalProvider(
        LocalLottoColors provides lottoColors,
        LocalLottoSpacing provides lottoSpacing,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LottoTypography,
            shapes = LottoShapes,
            content = content
        )
    }
}

/**
 * LottoTheme object
 * Accessor for custom theme tokens
 */
object LottoTheme {
    /**
     * Extended semantic colors for Lotto app
     * 로또 앱 전용 확장 색상 (공 색상, 당첨/실패 색상 등)
     */
    val colors: LottoColors
        @Composable
        get() = LocalLottoColors.current

    /**
     * Spacing tokens
     * 간격 토큰 (4dp 기반 그리드 시스템)
     */
    val spacing: LottoSpacing
        @Composable
        get() = LocalLottoSpacing.current

    /**
     * Number typography for lotto balls
     * 로또 공 전용 타이포그래피
     */
    val numberTypography: LottoNumberTypography
        get() = LottoNumberTypography
}

/**
 * CompositionLocal for LottoColors
 */
val LocalLottoColors = staticCompositionLocalOf {
    LightLottoColors
}

/**
 * CompositionLocal for LottoSpacing
 */
internal val LocalLottoSpacing = staticCompositionLocalOf {
    LottoSpacing()
}
