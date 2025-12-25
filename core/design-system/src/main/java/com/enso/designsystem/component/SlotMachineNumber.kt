package com.enso.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import com.enso.designsystem.theme.LocalLottoColors
import kotlin.math.abs
import kotlinx.coroutines.delay

/**
 * 슬롯머신 스타일 숫자 롤링 애니메이션
 * 각 자릿수가 드럼처럼 회전하며 목표 숫자에서 멈춤
 */
@Composable
fun SlotMachineNumber(
    targetNumber: Int,
    modifier: Modifier = Modifier,
    totalDurationMs: Int = 300,
    textStyle: TextStyle = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
    textColor: Color = LocalLottoColors.current.textMainLight
) {
    val digits = targetNumber.toString()
    var previousNumber by remember { mutableIntStateOf(targetNumber) }

    data class DigitAnimation(
        val targetDigit: Int,
        val fromDigit: Int,
        val stepCount: Int,
        val direction: Int,
        val delayMs: Int
    )

    val digitAnimations = remember(targetNumber) {
        val direction = when {
            targetNumber > previousNumber -> 1
            targetNumber < previousNumber -> -1
            else -> 0
        }
        val digitCount = digits.length
        digits.mapIndexed { index, char ->
            val position = digitCount - 1 - index
            val divisor = pow10(position)
            val prevChunk = previousNumber / divisor
            val targetChunk = targetNumber / divisor
            val stepCount = abs(targetChunk - prevChunk)
            val fromDigit = ((prevChunk % 10) + 10) % 10
            DigitAnimation(
                targetDigit = char.digitToInt(),
                fromDigit = fromDigit,
                stepCount = if (direction == 0) 0 else stepCount,
                direction = direction,
                delayMs = index * 40
            )
        }
    }

    SideEffect {
        previousNumber = targetNumber
    }

    Row(
        modifier = modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        digitAnimations.forEach { animation ->
            SingleDigitDrum(
                targetDigit = animation.targetDigit,
                fromDigit = animation.fromDigit,
                stepCount = animation.stepCount,
                direction = animation.direction,
                durationMs = totalDurationMs,
                delayMs = animation.delayMs,
                textStyle = textStyle,
                textColor = textColor
            )
        }
    }
}

/**
 * 드럼 롤 스타일 단일 자릿수 애니메이션
 * 자체적으로 이전 값과 방향을 관리하여 깜빡임/타이밍 문제 방지
 * 전체 숫자 카운트 방향을 따라 회전
 */
@Composable
private fun SingleDigitDrum(
    targetDigit: Int,
    fromDigit: Int,
    stepCount: Int,
    direction: Int,
    durationMs: Int = 300,
    delayMs: Int = 0,
    textStyle: TextStyle,
    textColor: Color
) {
    val density = LocalDensity.current
    val resolvedLineHeight = if (textStyle.lineHeight != TextUnit.Unspecified) {
        textStyle.lineHeight
    } else {
        textStyle.fontSize
    }
    val digitHeight = with(density) { resolvedLineHeight.toPx() }

    // 자체 상태 관리
    var displayedDigit by remember { mutableIntStateOf(targetDigit) }
    var animatingFromDigit by remember { mutableIntStateOf(targetDigit) }
    var animatingToDigit by remember { mutableIntStateOf(targetDigit) }
    var isAnimating by remember { mutableStateOf(false) }
    var animationDirection by remember { mutableIntStateOf(1) }
    val scrollOffset = remember { Animatable(0f) }

    // targetDigit 변경 감지 및 애니메이션
    LaunchedEffect(targetDigit, stepCount, direction, fromDigit) {
        if (stepCount == 0 || direction == 0) {
            displayedDigit = targetDigit
            animatingFromDigit = targetDigit
            animatingToDigit = targetDigit
            isAnimating = false
            scrollOffset.snapTo(0f)
            return@LaunchedEffect
        }

        // 이미 애니메이션 중이면 즉시 중단
        if (isAnimating) {
            scrollOffset.stop()
        }

        // 애니메이션 설정
        animatingFromDigit = fromDigit
        animatingToDigit = targetDigit
        displayedDigit = fromDigit

        animationDirection = if (direction > 0) 1 else -1

        isAnimating = true

        val totalDistance = stepCount * digitHeight

        delay(delayMs.toLong())

        // 스크롤 초기화 및 애니메이션
        scrollOffset.snapTo(0f)
        scrollOffset.animateTo(
            targetValue = totalDistance,
            animationSpec = tween(durationMs, easing = FastOutSlowInEasing)
        )

        // 마무리 바운스
        scrollOffset.animateTo(
            targetValue = totalDistance + digitHeight * 0.12f,
            animationSpec = tween(40, easing = LinearEasing)
        )
        scrollOffset.animateTo(
            targetValue = totalDistance,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMedium)
        )

        // 애니메이션 완료
        displayedDigit = targetDigit
        isAnimating = false
        scrollOffset.snapTo(0f)
    }

    // 현재 프레임 계산 (카운트 기반)
    val steps = if (isAnimating) stepCount else 0

    val totalDistance = steps * digitHeight
    val currentDigitIndex = if (totalDistance > 0) {
        (scrollOffset.value / digitHeight).toInt().coerceIn(0, steps)
    } else 0
    val frameOffset = if (totalDistance > 0) scrollOffset.value % digitHeight else 0f

    // 현재 표시할 숫자
    val currentDigit = if (isAnimating && steps > 0) {
        var digit = animatingFromDigit
        repeat(currentDigitIndex) {
            digit = if (animationDirection > 0) (digit + 1) % 10 else (digit - 1 + 10) % 10
        }
        digit
    } else {
        displayedDigit
    }

    // 다음 숫자
    val nextDigit = if (animationDirection > 0) (currentDigit + 1) % 10 else (currentDigit - 1 + 10) % 10

    // 애니메이션 완료 단계 여부
    val isComplete = isAnimating && totalDistance > 0 && scrollOffset.value >= totalDistance - 1f

    Box(
        modifier = Modifier
            .width(IntrinsicSize.Max)
            .height(with(density) { digitHeight.toDp() })
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 애니메이션 중이 아님
            !isAnimating -> {
                Text(
                    text = displayedDigit.toString(),
                    style = textStyle,
                    color = textColor
                )
            }
            // 애니메이션 완료 단계 (바운스)
            isComplete -> {
                val bounceOffset = scrollOffset.value - totalDistance
                Text(
                    text = animatingToDigit.toString(),
                    style = textStyle,
                    color = textColor,
                    modifier = Modifier.graphicsLayer {
                        translationY = if (animationDirection > 0) bounceOffset else -bounceOffset
                    }
                )
            }
            // 애니메이션 진행 중
            else -> {
                val currentTranslationY = if (animationDirection > 0) frameOffset else -frameOffset
                val nextTranslationY = if (animationDirection > 0) {
                    -digitHeight + frameOffset
                } else {
                    digitHeight - frameOffset
                }

                // 현재 숫자
                Text(
                    text = currentDigit.toString(),
                    style = textStyle,
                    color = textColor,
                    modifier = Modifier.graphicsLayer {
                        translationY = currentTranslationY
                        alpha = 1f - (frameOffset / digitHeight) * 0.3f
                    }
                )

                // 다음 숫자
                if (frameOffset > 0.1f && currentDigitIndex < steps) {
                    Text(
                        text = nextDigit.toString(),
                        style = textStyle,
                        color = textColor,
                        modifier = Modifier.graphicsLayer {
                            translationY = nextTranslationY
                            alpha = (frameOffset / digitHeight) * 0.7f + 0.3f
                        }
                    )
                }
            }
        }
    }
}

private fun pow10(exp: Int): Int {
    var result = 1
    repeat(exp) { result *= 10 }
    return result
}
