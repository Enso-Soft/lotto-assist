package com.enso.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.enso.designsystem.modifier.scaleOnPressEffect
import com.enso.designsystem.theme.LocalLottoColors

/**
 * 토스 스타일 PillButtonGroup
 *
 * 회색 배경 컨테이너 안에 선택 가능한 pill 버튼들을 배치합니다.
 * 선택된 항목은 흰색 배경 + elevation으로 강조됩니다.
 *
 * @param items 표시할 항목 목록
 * @param selectedIndex 현재 선택된 항목의 인덱스
 * @param onSelectionChange 선택이 변경될 때 호출되는 콜백
 * @param modifier Modifier
 * @param enabled 활성화 여부
 */
@Composable
fun PillButtonGroup(
    items: List<String>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val lottoColors = LocalLottoColors.current

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(lottoColors.chipBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            PillButton(
                text = item,
                isSelected = index == selectedIndex,
                onClick = { if (enabled) onSelectionChange(index) },
                enabled = enabled
            )
        }
    }
}

/**
 * 개별 Pill 버튼
 */
@Composable
private fun PillButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val lottoColors = LocalLottoColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) lottoColors.chipSelectedBackground else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pillBackgroundColor"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            !enabled -> lottoColors.textDisabled
            isSelected -> lottoColors.textPrimary
            else -> lottoColors.textSecondary
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pillTextColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pillElevation"
    )

    Box(
        modifier = Modifier
            .scaleOnPressEffect(interactionSource, targetScale = 0.97f)
            .shadow(elevation = elevation, shape = CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics {
                role = Role.Tab
                selected = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

/**
 * 게임 탭용 PillButtonGroup
 *
 * 게임 상태(완료/진행중/미입력)를 시각적으로 표시합니다.
 *
 * @param games 게임 ID와 상태 목록 (Pair<id, completionStatus>)
 * @param selectedIndex 현재 선택된 게임 인덱스
 * @param onSelectionChange 선택 변경 콜백
 * @param canAddGame 게임 추가 가능 여부
 * @param onAddGame 게임 추가 콜백
 * @param onGameLongPress 게임 롱프레스 콜백 (삭제용)
 * @param modifier Modifier
 */
@Composable
fun GameTabPillGroup(
    games: List<Pair<String, GameCompletionStatus>>,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    canAddGame: Boolean,
    onAddGame: () -> Unit,
    onGameLongPress: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lottoColors = LocalLottoColors.current

    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(lottoColors.chipBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        games.forEachIndexed { index, (id, status) ->
            GameTabPill(
                gameId = id,
                status = status,
                isSelected = index == selectedIndex,
                onClick = { onSelectionChange(index) },
                onLongClick = { onGameLongPress(index) }
            )
        }

        // 추가 버튼
        if (canAddGame) {
            AddGamePill(onClick = onAddGame)
        }
    }
}

/**
 * 게임 완료 상태
 */
enum class GameCompletionStatus {
    COMPLETE,    // 6개 선택 완료
    IN_PROGRESS, // 1~5개 선택
    EMPTY        // 0개 선택
}

/**
 * 개별 게임 탭 Pill
 */
@Composable
private fun GameTabPill(
    gameId: String,
    status: GameCompletionStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val lottoColors = LocalLottoColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val statusIcon = when (status) {
        GameCompletionStatus.COMPLETE -> "✓"
        GameCompletionStatus.IN_PROGRESS -> "●"
        GameCompletionStatus.EMPTY -> "○"
    }

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) lottoColors.chipSelectedBackground else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "gameTabBackgroundColor"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected -> lottoColors.textPrimary
            status == GameCompletionStatus.COMPLETE -> lottoColors.success
            else -> lottoColors.textSecondary
        },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "gameTabTextColor"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "gameTabElevation"
    )

    Box(
        modifier = Modifier
            .scaleOnPressEffect(interactionSource, targetScale = 0.97f)
            .shadow(elevation = elevation, shape = CircleShape)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onClickLabel = "게임 $gameId 선택"
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .semantics {
                role = Role.Tab
                selected = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$gameId$statusIcon",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = textColor
        )
    }
}

/**
 * 게임 추가 버튼 Pill
 */
@Composable
private fun AddGamePill(
    onClick: () -> Unit
) {
    val lottoColors = LocalLottoColors.current
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .scaleOnPressEffect(interactionSource, targetScale = 0.97f)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onClickLabel = "게임 추가"
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = lottoColors.textSecondary
        )
    }
}
