package com.enso.designsystem.modifier

import android.provider.Settings
import android.util.Log
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * 터치 시 스케일이 줄어들었다가 바운스 효과와 함께 복원되는 Modifier 확장 함수
 *
 * 버튼, 카드 등 클릭 가능한 요소에 적용하여 생동감 있는 인터랙션을 제공합니다.
 * 시스템의 "애니메이션 줄이기" 설정을 존중하여 접근성을 보장합니다.
 *
 * 빠른 탭에서도 시각적 피드백을 보장하기 위해 최소 press 시간을 유지합니다.
 * 클릭 이벤트는 즉시 발생하므로 앱 반응성에 영향을 주지 않습니다.
 *
 * 사용 예시:
 * ```
 * Box(
 *     modifier = Modifier.scaleOnPress { viewModel.onClick() }
 * )
 *
 * // Ripple 효과 없이 사용
 * Card(
 *     modifier = Modifier.scaleOnPress(enableRipple = false) { navigateToDetail() }
 * )
 * ```
 *
 * @param targetScale 눌렀을 때의 스케일 값 (기본값: 0.95f)
 * @param minPressDisplayMillis 최소 press 표시 시간 (기본값: 100ms)
 * @param enableRipple Ripple 효과 활성화 여부 (기본값: true)
 * @param enabled 클릭 가능 여부 (기본값: true)
 * @param shape Ripple 효과의 모양 (기본값: null - 사각형)
 * @param interactionSource 외부에서 제공하는 InteractionSource (선택적)
 * @param onClick 클릭 시 실행할 콜백
 */
fun Modifier.scaleOnPress(
    targetScale: Float = 0.95f,
    minPressDisplayMillis: Long = 250L,
    enableRipple: Boolean = true,
    enabled: Boolean = true,
    shape: Shape? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit
): Modifier = composed {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    // 시각적 press 상태 (최소 시간 보장)
    var showPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableLongStateOf(0L) }

    // 접근성: 시스템 애니메이션 설정 확인
    val reduceMotion = isReduceMotionEnabled()

    // Flow를 직접 수집하여 빠른 탭에서도 Press 이벤트를 놓치지 않음
    LaunchedEffect(actualInteractionSource) {
        actualInteractionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    // Press 시작: 즉시 스케일 다운
                    pressStartTime = System.currentTimeMillis()
                    showPressed = true
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    // Release: 최소 시간 보장 후 복원
                    val elapsed = System.currentTimeMillis() - pressStartTime
                    val remaining = minPressDisplayMillis - elapsed
                    if (remaining > 0 && !reduceMotion) {
                        delay(remaining)
                    }
                    showPressed = false
                }
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (showPressed) targetScale else 1f,
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "scaleOnPress"
    )

    val indication = if (enableRipple) LocalIndication.current else null

    val baseModifier = this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

    val clippedModifier = if (shape != null) {
        baseModifier.clip(shape)
    } else {
        baseModifier
    }

    clippedModifier.clickable(
        interactionSource = actualInteractionSource,
        indication = indication,
        enabled = enabled,
        onClick = onClick
    )
}

/**
 * 클릭 핸들러 없이 스케일 효과만 적용하는 Modifier
 *
 * 외부에서 clickable이나 다른 제스처 처리를 하는 경우에 사용합니다.
 * InteractionSource를 공유하여 press 상태를 감지합니다.
 *
 * 빠른 탭에서도 시각적 피드백을 보장하기 위해 최소 press 시간을 유지합니다.
 * 클릭 이벤트는 즉시 발생하므로 앱 반응성에 영향을 주지 않습니다.
 *
 * 사용 예시:
 * ```
 * val interactionSource = remember { MutableInteractionSource() }
 *
 * Button(
 *     onClick = { ... },
 *     interactionSource = interactionSource,
 *     modifier = Modifier.scaleOnPressEffect(interactionSource)
 * )
 * ```
 *
 * @param interactionSource press 상태를 감지할 InteractionSource
 * @param targetScale 눌렀을 때의 스케일 값 (기본값: 0.95f)
 * @param minPressDisplayMillis 최소 press 표시 시간 (기본값: 100ms)
 */
fun Modifier.scaleOnPressEffect(
    interactionSource: MutableInteractionSource,
    targetScale: Float = 0.95f,
    minPressDisplayMillis: Long = 150L
): Modifier = composed {
    // 시각적 press 상태 (최소 시간 보장)
    var showPressed by remember { mutableStateOf(false) }
    var pressStartTime by remember { mutableLongStateOf(0L) }

    // 접근성: 시스템 애니메이션 설정 확인
    val reduceMotion = isReduceMotionEnabled()

    // Flow를 직접 수집하여 빠른 탭에서도 Press 이벤트를 놓치지 않음
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    // Press 시작: 즉시 스케일 다운
                    pressStartTime = System.currentTimeMillis()
                    showPressed = true
                }
                is PressInteraction.Release, is PressInteraction.Cancel -> {
                    // Release: 최소 시간 보장 후 복원
                    val elapsed = System.currentTimeMillis() - pressStartTime
                    val remaining = minPressDisplayMillis - elapsed
                    if (remaining > 0 && !reduceMotion) {
                        delay(remaining)
                    }
                    showPressed = false
                }
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (showPressed) targetScale else 1f,
        animationSpec = if (reduceMotion) {
            snap()
        } else {
            spring(
                dampingRatio = 0.5f,
                stiffness = Spring.StiffnessLow
            )
        },
        label = "scaleOnPressEffect"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * 시스템의 애니메이션 줄이기 설정이 활성화되어 있는지 확인합니다.
 *
 * ANIMATOR_DURATION_SCALE이 0이면 애니메이션을 비활성화해야 합니다.
 * 이는 접근성 설정에서 "애니메이션 제거" 또는 "애니메이션 줄이기"를 활성화한 경우입니다.
 *
 * @return 애니메이션을 줄여야 하면 true, 그렇지 않으면 false
 */
@Composable
private fun isReduceMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        try {
            val scale = Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            )
            scale == 0f
        } catch (e: Exception) {
            false
        }
    }
}

// ==================== Previews ====================

@Preview(showBackground = true, name = "ScaleOnPress - Card")
@Composable
private fun ScaleOnPressCardPreview() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "터치하여 효과 확인",
                    style = MaterialTheme.typography.titleMedium
                )

                // With Ripple
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .scaleOnPress { /* onClick */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ripple ON")
                    }
                }

                // Without Ripple
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .scaleOnPress(enableRipple = false) { /* onClick */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ripple OFF")
                    }
                }

                // Custom Scale
                Card(
                    modifier = Modifier
                        .size(150.dp)
                        .scaleOnPress(targetScale = 0.85f) { /* onClick */ },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Scale 0.85")
                    }
                }
            }
        }
    }
}
