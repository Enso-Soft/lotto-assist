package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.enso.designsystem.theme.AppShapes
import com.enso.designsystem.theme.LottoTheme
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.home.R

@Composable
fun WinningBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val lottoColors = LocalLottoColors.current
    val badgeText = if (rank in 1..5) {
        stringResource(R.string.home_statistics_rank_format, rank)
    } else {
        stringResource(R.string.home_winning_badge_lose)
    }
    val (backgroundColor, textColor) = if (rank in 1..5) {
        val alpha = when (rank) {
            1, 2, 3 -> 1f
            4 -> 0.85f
            else -> 0.7f
        }
        lottoColors.success.copy(alpha = alpha) to lottoColors.onSuccess
    } else {
        lottoColors.losingRed.copy(alpha = 0.12f) to lottoColors.losingRed
    }

    Text(
        text = badgeText,
        modifier = modifier
            .background(backgroundColor, AppShapes.Badge)
            .padding(
                horizontal = LottoTheme.spacing.xs,
                vertical = LottoTheme.spacing.xxs
            ),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
    )
}
