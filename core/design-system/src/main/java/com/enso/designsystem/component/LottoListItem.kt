package com.enso.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.enso.designsystem.theme.AppShapes
import com.enso.designsystem.theme.LocalLottoColors

/**
 * Navigation List Item
 * 아이콘(둥근 배경) + 텍스트 + chevron
 */
@Composable
fun LottoNavigationListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    showChevron: Boolean = true,
) {
    val lottoColors = LocalLottoColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = lottoColors.textPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = lottoColors.textSecondary
                )
            }
        }

        if (trailingContent != null) {
            trailingContent()
            if (showChevron) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = lottoColors.textTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Icon Container
 * 둥근 배경의 아이콘 컨테이너
 */
@Composable
fun LottoIconContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    size: Dp = 48.dp,
    content: @Composable () -> Unit,
) {
    val lottoColors = LocalLottoColors.current

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor ?: lottoColors.chipBackground),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/**
 * Badge List Item
 * 아이콘 + 텍스트 + 뱃지/액션버튼
 */
@Composable
fun LottoBadgeListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    badge: String? = null,
    badgeColor: Color? = null,
) {
    val lottoColors = LocalLottoColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(16.dp))
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = lottoColors.textPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = lottoColors.textSecondary
                )
            }
        }

        if (badge != null) {
            Surface(
                shape = AppShapes.Chip,
                color = badgeColor ?: lottoColors.chipBackground
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = lottoColors.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Mission List Item
 * 미션 리스트 아이템 (아이콘 + 텍스트 + 액션 버튼)
 */
@Composable
fun LottoMissionListItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val lottoColors = LocalLottoColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(16.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = lottoColors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        if (actionText != null && onActionClick != null) {
            LottoPillButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}

/**
 * Section Header
 * 섹션 제목 + "더보기" 버튼
 */
@Composable
fun LottoSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val lottoColors = LocalLottoColors.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = lottoColors.textPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = lottoColors.textSecondary
                )
            }
        }

        if (actionText != null && onActionClick != null) {
            LottoTextButton(
                text = actionText,
                onClick = onActionClick
            )
        }
    }
}
