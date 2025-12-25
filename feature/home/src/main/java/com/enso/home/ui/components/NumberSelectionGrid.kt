package com.enso.home.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enso.designsystem.modifier.scaleOnPressEffect
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.designsystem.theme.getLottoBallColor

/**
 * 번호 선택 그리드 (7열 로또 용지 스타일)
 *
 * 1-45 번호를 실제 로또 용지와 동일하게 7열 7행으로 배치합니다.
 * 1-7, 8-14, 15-21, 22-28, 29-35, 36-42, 43-45
 *
 * @param selectedNumbers 현재 선택된 번호 목록
 * @param onNumberClick 번호 클릭 콜백
 * @param maxSelection 최대 선택 가능 개수 (기본: 6)
 * @param modifier Modifier
 */
@Composable
fun NumberSelectionGrid(
    selectedNumbers: List<Int>,
    onNumberClick: (Int) -> Unit,
    maxSelection: Int = 6,
    modifier: Modifier = Modifier
) {
    val lottoColors = LocalLottoColors.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 7행으로 나누기 (로또 용지 스타일: 1-7, 8-14, ..., 43-45)
        for (row in 0 until 7) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val startNumber = row * 7 + 1
                val endNumber = minOf(startNumber + 6, 45)

                for (number in startNumber..endNumber) {
                    val isSelected = selectedNumbers.contains(number)
                    val canSelect = selectedNumbers.size < maxSelection || isSelected

                    NumberButton(
                        number = number,
                        isSelected = isSelected,
                        enabled = canSelect,
                        onClick = { onNumberClick(number) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 마지막 행은 7개가 안 될 수 있으므로 빈 공간 채우기
                val emptySlots = 7 - (endNumber - startNumber + 1)
                repeat(emptySlots) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 개별 번호 버튼
 */
@Composable
private fun NumberButton(
    number: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lottoColors = LocalLottoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val ballColor = getLottoBallColor(number, lottoColors)

    // 애니메이션된 배경색
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> ballColor
            else -> lottoColors.chipBackground
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "numberBackgroundColor"
    )

    // 애니메이션된 텍스트색
    val textColor by animateColorAsState(
        targetValue = when {
            !enabled && !isSelected -> lottoColors.textDisabled
            isSelected -> Color.White
            else -> ballColor
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "numberTextColor"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scaleOnPressEffect(interactionSource, targetScale = 0.92f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .semantics {
                role = Role.Checkbox
                selected = isSelected
                contentDescription = "번호 $number${if (isSelected) ", 선택됨" else ""}"
            },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // 선택됨: 번호 + 체크 아이콘
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = with(LocalDensity.current) { 16.dp.toSp() }
                    ),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                // 우상단 작은 체크 표시
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(12.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )
            }
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = with(LocalDensity.current) { 16.dp.toSp() }
                ),
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}
