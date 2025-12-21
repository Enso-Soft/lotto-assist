package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.home.R
import com.enso.home.ui.theme.CardLight
import com.enso.home.ui.theme.Primary
import com.enso.home.ui.theme.TextMainLight
import com.enso.home.ui.theme.TextSubLight
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TicketCard(
    ticket: LottoTicket,
    lottoResult: LottoResult?,
    currentRound: Int,
    onCheckWinning: () -> Unit,
    onDelete: () -> Unit
) {
    val isDrawComplete = ticket.round <= currentRound
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(ticket.ticketId, ticket.isChecked, isDrawComplete) {
        if (isDrawComplete && !ticket.isChecked) {
            onCheckWinning()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 티켓 헤더: 회차 + 등록일 + 삭제 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.home_round_prefix_format, ticket.round),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextMainLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.home_registered_format, formatDrawDate(ticket.registeredDate)),
                        fontSize = 11.sp,
                        color = TextSubLight
                    )
                }
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.home_delete),
                        tint = TextSubLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // 당첨번호 및 당첨금 정보
            if (lottoResult != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    // 당첨 번호 + 보너스 (가운데 정렬)
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 당첨 번호 6개
                        lottoResult.numbers.forEach { number ->
                            SmallLottoBall(number = number)
                            Spacer(modifier = Modifier.width(3.dp))
                        }

                        // 구분선
                        Text(
                            text = "+",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubLight,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        )

                        // 보너스 번호
                        SmallLottoBall(number = lottoResult.bonusNumber)
                    }

                    // 당첨금 (가운데 정렬, 문구 없음)
                    Text(
                        text = formatPrizeAmount(lottoResult.firstPrize.winAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )
                }
            }

            // 게임 목록
            ticket.games.forEach { game ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 게임 레이블 (A, B, C, D, E)
                    Text(
                        game.gameLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Primary,
                        modifier = Modifier.width(16.dp)
                    )

                    // 당첨 배지 + 게임 타입 (세로로 배치)
                    Column(
                        modifier = Modifier.width(25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // 당첨/낙첨 배지
                        if (isDrawComplete && game.winningRank > 0) {
                            WinningBadge(rank = game.winningRank)
                        }
                        // 자동/수동
                        Text(
                            game.gameType.displayName,
                            fontSize = 12.sp,
                            color = TextSubLight
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 번호들 (당첨번호가 있으면 매칭된 번호만 하이라이트)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        if (lottoResult != null && isDrawComplete) {
                            game.numbers.forEach { number ->
                                HighlightedSmallLottoBall(
                                    number = number,
                                    isMatched = number in lottoResult.numbers
                                )
                            }
                        } else {
                            game.numbers.forEach { number ->
                                SmallLottoBall(number = number)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            }
        )
    }
}

private fun formatDrawDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    return formatter.format(date)
}

private fun formatPrizeAmount(amount: Long): String {
    val billions = amount / 100_000_000
    val remainder = amount % 100_000_000
    val tenThousands = remainder / 10_000

    return if (billions > 0) {
        if (tenThousands > 0) {
            val formatter = NumberFormat.getInstance(Locale.KOREA)
            "${billions}억 ${formatter.format(tenThousands)}만원"
        } else {
            "${billions}억원"
        }
    } else if (tenThousands > 0) {
        val formatter = NumberFormat.getInstance(Locale.KOREA)
        "${formatter.format(tenThousands)}만원"
    } else {
        "${amount}원"
    }
}
