package com.enso.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lotto Spacing
 * 4dp 기반 그리드 시스템
 */
@Immutable
data class LottoSpacing(
    // Base spacing scale
    val none: Dp = 0.dp,
    val xxs: Dp = 4.dp,
    val xs: Dp = 8.dp,
    val sm: Dp = 12.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 20.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,
    val xxxxl: Dp = 64.dp,

    // Semantic spacing aliases
    val screenHorizontalPadding: Dp = 20.dp,
    val screenVerticalPadding: Dp = 24.dp,
    val cardPadding: Dp = 16.dp,
    val sectionGap: Dp = 32.dp,
    val listItemSpacing: Dp = 12.dp,
    val ballSpacing: Dp = 3.dp,
)
