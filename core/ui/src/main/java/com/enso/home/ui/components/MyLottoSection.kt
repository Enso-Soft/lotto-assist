package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.home.ui.theme.CardLight
import com.enso.home.ui.theme.Primary
import com.enso.home.ui.theme.TextMainLight
import com.enso.home.ui.theme.TextSubLight
import com.enso.ui.R
import com.enso.util.format.formatDrawDate
import java.text.NumberFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyLottoSection(
    tickets: List<LottoTicket>,
    lottoResults: List<LottoResult>,
    currentRound: Int,
    onCheckWinning: (Long) -> Unit,
    onDeleteTicket: (Long) -> Unit,
    onViewAll: () -> Unit = {},
    showViewAll: Boolean = true,
    enableDelete: Boolean = true
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.my_lotto_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextMainLight
            )
            if (showViewAll) {
                Text(
                    stringResource(R.string.my_lotto_view_all),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSubLight,
                    modifier = Modifier.clickable(onClick = onViewAll)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tickets.isEmpty()) {
            EmptyTicketCard()
        } else {
            val pagerState = rememberPagerState(pageCount = { tickets.size })

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 40.dp),
                pageSpacing = 12.dp,
                modifier = Modifier
            ) { page ->
                val ticket = tickets[page]
                TicketCard(
                    ticket = ticket,
                    lottoResult = lottoResults.find { it.round == ticket.round },
                    currentRound = currentRound,
                    onCheckWinning = { onCheckWinning(ticket.ticketId) },
                    onDelete = { onDeleteTicket(ticket.ticketId) },
                    enableDelete = enableDelete
                )
            }
        }
    }
}

@Composable
private fun EmptyTicketCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                stringResource(R.string.my_lotto_empty),
                textAlign = TextAlign.Center,
                color = TextSubLight,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TicketCard(
    ticket: LottoTicket,
    lottoResult: LottoResult?,
    currentRound: Int,
    onCheckWinning: () -> Unit,
    onDelete: () -> Unit,
    enableDelete: Boolean
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "제 ${ticket.round}회",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextMainLight
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${formatDrawDate(ticket.registeredDate)} 등록",
                        fontSize = 11.sp,
                        color = TextSubLight
                    )
                }
                if (enableDelete) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "삭제",
                            tint = TextSubLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

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
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        lottoResult.numbers.forEach { number ->
                            SmallLottoBall(number = number)
                            Spacer(modifier = Modifier.width(3.dp))
                        }

                        Text(
                            text = "+",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSubLight,
                            modifier = Modifier.padding(horizontal = 3.dp)
                        )

                        SmallLottoBall(number = lottoResult.bonusNumber)
                    }

                    Text(
                        text = formatPrizeAmount(lottoResult.firstPrize.winAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Primary
                    )
                }
            }

            ticket.games.forEach { game ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        game.gameLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Primary,
                        modifier = Modifier.width(16.dp)
                    )

                    Column(
                        modifier = Modifier.width(25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (isDrawComplete && game.winningRank > 0) {
                            WinningBadge(rank = game.winningRank)
                        }
                        Text(
                            game.gameType.displayName,
                            fontSize = 12.sp,
                            color = TextSubLight
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

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

    if (enableDelete && showDeleteDialog) {
        ConfirmDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            }
        )
    }
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
