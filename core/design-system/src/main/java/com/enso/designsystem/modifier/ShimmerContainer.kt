package com.enso.designsystem.modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

/**
 * Shimmer 애니메이션을 공유하는 컨테이너 컴포저블
 *
 * 이 컨테이너 내부의 모든 shimmerEffect() 호출은 동일한 애니메이션 상태를 공유합니다.
 * 이를 통해 스켈레톤 요소들이 동기화된 shimmer 효과를 가지며,
 * InfiniteTransition 인스턴스 수를 대폭 줄여 성능을 개선합니다.
 *
 * 사용 예시:
 * ```
 * ShimmerContainer {
 *     Column {
 *         SkeletonBox()  // 공유 shimmer 사용
 *         SkeletonBox()  // 공유 shimmer 사용
 *         SkeletonBox()  // 공유 shimmer 사용
 *     }
 * }
 * ```
 *
 * @param baseColor 스켈레톤 기본 색상
 * @param highlightColor 하이라이트 색상 (shimmer 효과)
 * @param durationMillis 애니메이션 한 사이클 시간 (ms)
 * @param content 스켈레톤 UI를 포함하는 컴포저블 콘텐츠
 */
@Composable
fun ShimmerContainer(
    baseColor: Color = Color.LightGray.copy(alpha = 0.3f),
    highlightColor: Color = Color.LightGray.copy(alpha = 0.7f),
    durationMillis: Int = 1200,
    content: @Composable () -> Unit
) {
    val shimmerState = rememberShimmerState(
        baseColor = baseColor,
        highlightColor = highlightColor,
        durationMillis = durationMillis
    )

    CompositionLocalProvider(LocalShimmerState provides shimmerState) {
        content()
    }
}
