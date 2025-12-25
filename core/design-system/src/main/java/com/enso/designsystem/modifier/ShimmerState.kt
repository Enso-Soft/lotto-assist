package com.enso.designsystem.modifier

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

/**
 * Shimmer 애니메이션의 공유 상태를 관리하는 클래스
 *
 * 모든 스켈레톤 요소가 동일한 애니메이션 타이밍을 공유하도록 합니다.
 * CompositionLocal을 통해 하위 컴포저블에 전달됩니다.
 *
 * @property translateAnim 현재 애니메이션 진행값 (0f ~ 1000f)
 * @property baseColor 스켈레톤 기본 색상
 * @property highlightColor 하이라이트 색상 (shimmer 효과)
 */
@Stable
class ShimmerState(
    val translateAnim: Float,
    val baseColor: Color,
    val highlightColor: Color
)

/**
 * Shimmer 상태를 제공하는 CompositionLocal
 *
 * null이면 shimmerEffect()가 자체 애니메이션을 생성합니다 (하위 호환성).
 * ShimmerContainer 내부에서는 공유 상태가 제공됩니다.
 */
val LocalShimmerState = compositionLocalOf<ShimmerState?> { null }

/**
 * ShimmerState를 생성하고 기억하는 함수
 *
 * 단일 InfiniteTransition을 생성하여 모든 하위 shimmerEffect()가 공유합니다.
 *
 * @param baseColor 스켈레톤 기본 색상
 * @param highlightColor 하이라이트 색상
 * @param durationMillis 애니메이션 한 사이클 시간 (ms)
 * @return 공유 ShimmerState 인스턴스
 */
@Composable
fun rememberShimmerState(
    baseColor: Color = Color.LightGray.copy(alpha = 0.3f),
    highlightColor: Color = Color.LightGray.copy(alpha = 0.7f),
    durationMillis: Int = 1200
): ShimmerState {
    val transition = rememberInfiniteTransition(label = "shared_shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shared_shimmer_translate"
    )

    return ShimmerState(
        translateAnim = translateAnim,
        baseColor = baseColor,
        highlightColor = highlightColor
    )
}
