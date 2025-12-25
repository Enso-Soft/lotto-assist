package com.enso.home.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enso.designsystem.modifier.scaleOnPressEffect
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.designsystem.theme.getLottoBallColor

/**
 * 선택된 공 슬롯 Row (6칸 고정)
 *
 * 선택된 번호를 공으로 표시하고, 빈 슬롯은 점선 원으로 표시합니다.
 * 선택된 공을 탭하면 해당 번호가 해제됩니다.
 *
 * @param selectedNumbers 선택된 번호 목록 (최대 6개)
 * @param onBallClick 공 클릭 콜백 (해제용)
 * @param modifier Modifier
 */
@Composable
fun SelectedBallsRow(
    selectedNumbers: List<Int>,
    onBallClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 오름차순 정렬하여 표시
    val sortedNumbers = selectedNumbers.sorted()
    val currentSet = sortedNumbers.toSet()

    // 이전에 표시된 번호들을 기억
    var previousNumbers by remember { mutableStateOf(currentSet) }

    // 새로 추가된 번호만 애니메이션 적용 (기존 번호는 제외)
    val newlyAddedNumbers = currentSet - previousNumbers

    // Composition 완료 후 이전 번호 목록 업데이트
    SideEffect {
        previousNumbers = currentSet
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(6) { index ->
            val number = sortedNumbers.getOrNull(index)

            if (number != null) {
                // key를 사용하여 번호가 변경될 때 새로운 컴포저블로 인식
                key(number) {
                    SelectedBall(
                        number = number,
                        onClick = { onBallClick(number) },
                        animateEntry = number in newlyAddedNumbers
                    )
                }
            } else {
                EmptySlot(index = index + 1)
            }
        }
    }
}

/**
 * 선택된 공
 */
@Composable
private fun SelectedBall(
    number: Int,
    onClick: () -> Unit,
    animateEntry: Boolean = false
) {
    val lottoColors = LocalLottoColors.current
    val ballColor = getLottoBallColor(number, lottoColors)
    val interactionSource = remember { MutableInteractionSource() }

    // 진입 애니메이션: 30% → 100% with bounce
    val scale = remember { Animatable(if (animateEntry) 0.3f else 1f) }

    LaunchedEffect(Unit) {
        if (animateEntry) {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.5f,
                    stiffness = 500f
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .scaleOnPressEffect(interactionSource, targetScale = 0.9f)
            .clip(CircleShape)
            .background(ballColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onClickLabel = "번호 $number 해제"
            )
            .semantics {
                contentDescription = "선택된 번호 $number, 탭하여 해제"
            },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = with(density) { 16.dp.toSp() }
            ),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * 빈 슬롯 (점선 원)
 */
@Composable
private fun EmptySlot(
    index: Int
) {
    val lottoColors = LocalLottoColors.current
    val dashColor = lottoColors.divider

    Box(
        modifier = Modifier
            .size(48.dp)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val dashLength = 8.dp.toPx()
                val gapLength = 4.dp.toPx()

                drawCircle(
                    color = dashColor,
                    radius = (size.minDimension - strokeWidth) / 2,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(dashLength, gapLength),
                            0f
                        )
                    )
                )
            }
            .semantics {
                contentDescription = "빈 슬롯 $index"
            },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = with(density) { 14.dp.toSp() }
            ),
            color = lottoColors.textDisabled
        )
    }
}

/**
 * 바텀시트용 선택된 공 Row (정렬됨)
 *
 * GameCompleteBottomSheet에서 사용하는 정렬된 번호 표시용
 */
@Composable
fun SelectedBallsRowForBottomSheet(
    selectedNumbers: List<Int>,
    modifier: Modifier = Modifier
) {
    val sortedNumbers = selectedNumbers.sorted()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        sortedNumbers.forEach { number ->
            DisplayBall(number = number)
        }
    }
}

/**
 * 표시 전용 공 (클릭 불가)
 */
@Composable
private fun DisplayBall(
    number: Int
) {
    val lottoColors = LocalLottoColors.current
    val ballColor = getLottoBallColor(number, lottoColors)

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(ballColor)
            .semantics {
                contentDescription = "번호 $number"
            },
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = with(density) { 14.dp.toSp() }
            ),
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
