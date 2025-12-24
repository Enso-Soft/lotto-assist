package com.enso.designsystem.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Shimmer 애니메이션 효과를 적용하는 Modifier 확장 함수
 *
 * ShimmerContainer 내부에서 호출되면 공유 애니메이션 상태를 사용합니다 (권장).
 * ShimmerContainer 외부에서 호출되면 자체 애니메이션을 생성합니다 (하위 호환성).
 *
 * 성능 최적화를 위해 ShimmerContainer와 함께 사용하는 것을 권장합니다:
 * ```
 * ShimmerContainer {
 *     Box(modifier = Modifier.shimmerEffect())
 *     Box(modifier = Modifier.shimmerEffect())
 * }
 * ```
 *
 * @param baseColor 스켈레톤 기본 색상 (ShimmerContainer 내부에서는 무시됨)
 * @param highlightColor 하이라이트 색상 (ShimmerContainer 내부에서는 무시됨)
 * @param durationMillis 애니메이션 한 사이클 시간 (ShimmerContainer 내부에서는 무시됨)
 */
fun Modifier.shimmerEffect(
    baseColor: Color = Color.LightGray.copy(alpha = 0.3f),
    highlightColor: Color = Color.LightGray.copy(alpha = 0.7f),
    durationMillis: Int = 1200
): Modifier = composed {
    // ShimmerContainer에서 제공하는 공유 상태가 있으면 사용
    val sharedState = LocalShimmerState.current

    val (translateAnim, actualBaseColor, actualHighlightColor) = if (sharedState != null) {
        // 공유 상태 사용 (성능 최적화)
        Triple(sharedState.translateAnim, sharedState.baseColor, sharedState.highlightColor)
    } else {
        // Fallback: 자체 애니메이션 생성 (하위 호환성)
        val transition = rememberInfiniteTransition(label = "shimmer_fallback")
        val anim by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = durationMillis,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate_fallback"
        )
        Triple(anim, baseColor, highlightColor)
    }

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            actualBaseColor,
            actualHighlightColor,
            actualBaseColor
        ),
        start = Offset(translateAnim - 200f, translateAnim - 200f),
        end = Offset(translateAnim, translateAnim)
    )

    this.background(shimmerBrush)
}
