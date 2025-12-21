package com.enso.home.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enso.domain.model.WinningStatistics
import com.enso.home.R
import com.enso.home.ui.theme.BackgroundDark
import com.enso.home.ui.theme.BackgroundLight
import com.enso.home.ui.theme.BallBlue
import com.enso.home.ui.theme.BallGreen
import com.enso.home.ui.theme.BallGrey
import com.enso.home.ui.theme.BallRed
import com.enso.home.ui.theme.BallYellow
import com.enso.home.ui.theme.CardDark
import com.enso.home.ui.theme.CardLight
import com.enso.home.ui.theme.Primary
import com.enso.home.ui.theme.TextMainDark
import com.enso.home.ui.theme.TextMainLight
import com.enso.home.ui.theme.TextSubDark
import com.enso.home.ui.theme.TextSubLight
import com.enso.home.ui.theme.WinningGreen

/**
 * WinningStatisticsWidget - 나의 당첨 통계 위젯
 *
 * 사용자의 로또 당첨 통계를 카드 형태로 표시하는 컴포저블.
 * 당첨률, 당첨/전체 게임 수, 등수별 당첨 횟수를 보여줍니다.
 *
 * @param statistics 표시할 당첨 통계 데이터
 * @param modifier 외부에서 전달받는 Modifier
 */
@Composable
fun WinningStatisticsWidget(
    statistics: WinningStatistics,
    modifier: Modifier = Modifier
) {
    val isDarkMode = isSystemInDarkTheme()
    val cardColor = if (isDarkMode) CardDark else CardLight

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // 헤더
            StatisticsHeader(isDarkMode = isDarkMode)

            Spacer(modifier = Modifier.height(16.dp))

            if (statistics.hasData) {
                // 메인 통계 섹션 (당첨률 + 당첨/전체)
                MainStatsSection(
                    winningRate = statistics.formattedWinningRate,
                    winningCount = statistics.winningGamesCount,
                    totalCount = statistics.checkedGamesCount,
                    isDarkMode = isDarkMode
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 구분선
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = if (isDarkMode) TextSubDark.copy(alpha = 0.2f) else TextSubLight.copy(alpha = 0.2f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 등수별 당첨 횟수
                RankBreakdownSection(
                    rankBreakdown = statistics.rankBreakdown,
                    isDarkMode = isDarkMode
                )
            } else {
                // 빈 상태
                EmptyStatisticsContent(isDarkMode = isDarkMode)
            }
        }
    }
}

/**
 * 통계 위젯 헤더
 *
 * @param isDarkMode 다크 모드 여부
 */
@Composable
private fun StatisticsHeader(isDarkMode: Boolean) {
    val textColor = if (isDarkMode) TextMainDark else TextMainLight

    Text(
        text = stringResource(R.string.home_statistics_title),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = textColor
    )
}

/**
 * 메인 통계 섹션 - 당첨률과 당첨/전체 게임 수 표시
 *
 * @param winningRate 포맷된 당첨률 문자열
 * @param winningCount 당첨 게임 수
 * @param totalCount 전체 확인된 게임 수
 * @param isDarkMode 다크 모드 여부
 */
@Composable
private fun MainStatsSection(
    winningRate: String,
    winningCount: Int,
    totalCount: Int,
    isDarkMode: Boolean
) {
    val subTextColor = if (isDarkMode) TextSubDark else TextSubLight

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 당첨률
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = winningRate,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_statistics_win_rate),
                fontSize = 12.sp,
                color = subTextColor
            )
        }

        // 수직 구분선
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(50.dp)
                .background(subTextColor.copy(alpha = 0.2f))
        )

        // 당첨 / 전체
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$winningCount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = WinningGreen
                )
                Text(
                    text = " / ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = subTextColor
                )
                Text(
                    text = "$totalCount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkMode) TextMainDark else TextMainLight
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.home_statistics_win_total),
                fontSize = 12.sp,
                color = subTextColor
            )
        }
    }
}

/**
 * 등수별 당첨 횟수 섹션
 *
 * @param rankBreakdown 등수(1-5)별 당첨 횟수 맵
 * @param isDarkMode 다크 모드 여부
 */
@Composable
private fun RankBreakdownSection(
    rankBreakdown: Map<Int, Int>,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..5).forEach { rank ->
            RankBadge(
                rank = rank,
                count = rankBreakdown.getOrDefault(rank, 0),
                isDarkMode = isDarkMode
            )
        }
    }
}

/**
 * 등수별 배지 컴포넌트
 *
 * @param rank 등수 (1-5)
 * @param count 해당 등수 당첨 횟수
 * @param isDarkMode 다크 모드 여부
 */
@Composable
private fun RankBadge(
    rank: Int,
    count: Int,
    isDarkMode: Boolean
) {
    val badgeColor = getRankBadgeColor(rank)
    val subTextColor = if (isDarkMode) TextSubDark else TextSubLight

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 등수 배지 (원형)
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.home_statistics_rank_format, rank),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 당첨 횟수
        Text(
            text = stringResource(R.string.home_statistics_count_format, count),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = subTextColor
        )
    }
}

/**
 * 빈 상태 컨텐츠 - 데이터가 없을 때 표시
 *
 * @param isDarkMode 다크 모드 여부
 */
@Composable
private fun EmptyStatisticsContent(isDarkMode: Boolean) {
    val subTextColor = if (isDarkMode) TextSubDark else TextSubLight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.home_statistics_empty_title),
                fontSize = 14.sp,
                color = subTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.home_statistics_empty_subtitle),
                fontSize = 12.sp,
                color = subTextColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 등수에 따른 배지 색상 반환
 *
 * @param rank 등수 (1-5)
 * @return 해당 등수의 배지 색상
 */
private fun getRankBadgeColor(rank: Int): Color = when (rank) {
    1 -> BallYellow   // 1등 - 노란색
    2 -> BallBlue     // 2등 - 파란색
    3 -> BallRed      // 3등 - 빨간색
    4 -> BallGrey     // 4등 - 회색
    5 -> BallGreen    // 5등 - 녹색
    else -> BallGrey
}

// ============================================================================
// Preview Composables
// ============================================================================

/**
 * 샘플 통계 데이터 - 일반 상태
 */
private val sampleStatistics = WinningStatistics(
    totalGamesPlayed = 180,
    checkedGamesCount = 144,
    winningGamesCount = 18,
    winningRate = 12.5f,
    rankBreakdown = WinningStatistics.VALID_WINNING_RANKS.associateWith { rank ->
        when (rank) {
            1 -> 0
            2 -> 1
            3 -> 2
            4 -> 5
            5 -> 10
            else -> 0
        }
    },
    totalTickets = 36
)

/**
 * 샘플 통계 데이터 - 높은 당첨률
 */
private val highWinStatistics = WinningStatistics(
    totalGamesPlayed = 50,
    checkedGamesCount = 50,
    winningGamesCount = 25,
    winningRate = 50.0f,
    rankBreakdown = WinningStatistics.VALID_WINNING_RANKS.associateWith { rank ->
        when (rank) {
            1 -> 1
            2 -> 2
            3 -> 5
            4 -> 8
            5 -> 9
            else -> 0
        }
    },
    totalTickets = 10
)

@Preview(name = "Default - Light")
@Composable
private fun WinningStatisticsWidgetLightPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = sampleStatistics
        )
    }
}

@Preview(
    name = "Default - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WinningStatisticsWidgetDarkPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = sampleStatistics
        )
    }
}

@Preview(name = "Empty State - Light")
@Composable
private fun WinningStatisticsWidgetEmptyLightPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = WinningStatistics.EMPTY
        )
    }
}

@Preview(
    name = "Empty State - Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun WinningStatisticsWidgetEmptyDarkPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = WinningStatistics.EMPTY
        )
    }
}

@Preview(
    name = "Portrait",
    device = Devices.PIXEL_4
)
@Composable
private fun WinningStatisticsWidgetPortraitPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = sampleStatistics
        )
    }
}

@Preview(
    name = "Landscape",
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 720,
    heightDp = 360
)
@Composable
private fun WinningStatisticsWidgetLandscapePreview() {
    Box(
        modifier = Modifier
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = sampleStatistics
        )
    }
}

@Preview(
    name = "Small Screen",
    widthDp = 320,
    heightDp = 480
)
@Composable
private fun WinningStatisticsWidgetSmallScreenPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundLight)
            .padding(12.dp)
    ) {
        WinningStatisticsWidget(
            statistics = sampleStatistics
        )
    }
}

@Preview(
    name = "Large Text",
    fontScale = 1.5f
)
@Composable
private fun WinningStatisticsWidgetLargeTextPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = sampleStatistics
        )
    }
}

@Preview(name = "High Win Rate")
@Composable
private fun WinningStatisticsWidgetHighWinPreview() {
    Box(
        modifier = Modifier
            .background(BackgroundLight)
            .padding(16.dp)
    ) {
        WinningStatisticsWidget(
            statistics = highWinStatistics
        )
    }
}
