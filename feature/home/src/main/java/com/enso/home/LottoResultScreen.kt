package com.enso.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
import com.enso.home.ui.components.AllTicketsHeader
import com.enso.home.ui.components.ConfirmDeleteDialog
import com.enso.home.ui.components.HighlightedSmallLottoBall
import com.enso.home.ui.components.LottoBall
import com.enso.home.ui.components.ManualInputDialog
import com.enso.home.ui.components.MediumLottoBall
import com.enso.home.ui.components.SmallLottoBall
import com.enso.home.ui.components.SortButton
import com.enso.home.ui.components.SortSelectionBottomSheet
import com.enso.home.ui.components.TicketCard
import com.enso.home.ui.components.TinyLottoBall
import com.enso.home.ui.components.WinningBadge
import com.enso.home.ui.components.WinningStatisticsWidget
import com.enso.home.ui.theme.BackgroundLight
import com.enso.home.ui.theme.CardLight
import com.enso.home.ui.theme.Primary
import com.enso.home.ui.theme.TextMainLight
import com.enso.home.ui.theme.TextSubLight
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun LottoResultScreen(
    viewModel: LottoResultViewModel = hiltViewModel(),
    onQrScanClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var showRoundBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LottoResultEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LottoResultEffect.SyncCompleted -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.home_sync_completed))
                }
                is LottoResultEffect.PartialSyncCompleted -> {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.home_partial_sync_message, effect.failedCount, effect.successCount)
                    )
                }
                is LottoResultEffect.NavigateToQrScan -> {
                    onQrScanClick()
                }
                is LottoResultEffect.NavigateToManualInput -> {
                    showManualInputDialog = true
                }
                is LottoResultEffect.ShowTicketSaved -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.home_ticket_saved_message, effect.count))
                }
            }
        }
    }

    if (showManualInputDialog) {
        ManualInputDialog(
            currentRound = uiState.currentRound,
            onDismiss = { showManualInputDialog = false },
            onConfirm = { round, numbers, isAuto ->
                viewModel.onEvent(LottoResultEvent.SaveManualTicket(round, numbers, isAuto))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextMainLight
                ),
                actions = {
                    IconButton(onClick = { /* 설정 */ }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = stringResource(R.string.home_settings),
                            tint = TextMainLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 당첨 결과 섹션
            WinningResultSection(
                selectedResult = uiState.selectedResult,
                isLoading = uiState.isSyncing,
                onRoundClick = { showRoundBottomSheet = true }
            )

            // QR 스캔 / 번호 입력 버튼
            ActionButtonsSection(
                onQrScanClick = { viewModel.onEvent(LottoResultEvent.OpenQrScan) },
                onManualInputClick = { viewModel.onEvent(LottoResultEvent.OpenManualInput) }
            )

            // 나의 당첨 통계
            WinningStatisticsWidget(
                statistics = uiState.winningStatistics,
                modifier = Modifier.fillMaxWidth()
            )

            // 지난 회차 정보
            PastDrawsSection(
                results = uiState.lottoResults.take(3),
                onSelectResult = { result ->
                    viewModel.onEvent(LottoResultEvent.SelectResult(result))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showRoundBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRoundBottomSheet = false },
            sheetState = bottomSheetState,
            containerColor = CardLight
        ) {
            RoundSelectionBottomSheet(
                results = uiState.lottoResults,
                selectedRound = uiState.selectedResult?.round,
                onSelectRound = { result ->
                    viewModel.onEvent(LottoResultEvent.SelectResult(result))
                    scope.launch {
                        bottomSheetState.hide()
                        showRoundBottomSheet = false
                    }
                }
            )
        }
    }
}

@Composable
private fun WinningResultSection(
    selectedResult: LottoResult?,
    isLoading: Boolean,
    onRoundClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 축하 배지
        Surface(
            modifier = Modifier.padding(bottom = 8.dp),
            color = Primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎉",
                    fontSize = 14.sp
                )
                Text(
                    text = stringResource(R.string.home_winning_congrats),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
        }

        // 회차 선택
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onRoundClick)
        ) {
            Text(
                text = stringResource(R.string.home_round_format, selectedResult?.round ?: 0),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextMainLight
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.home_select_round),
                modifier = Modifier.size(32.dp),
                tint = TextSubLight
            )
            Text(
                text = stringResource(R.string.home_winning_result),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextMainLight
            )
        }

        // 추첨 날짜
        selectedResult?.let {
            Text(
                text = formatDrawDate(it.drawDate),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSubLight,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 당첨 번호 카드
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            selectedResult?.let { result ->
                WinningNumbersCard(result)
            }
        }
    }
}

@Composable
private fun WinningNumbersCard(result: LottoResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 당첨 번호 + 보너스 (한 줄에 표시)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                // 당첨 번호 6개
                result.numbers.forEach { number ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        MediumLottoBall(number = number)
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                // 구분선
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSubLight,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(15.dp))
                }

                // 보너스 번호 + 텍스트
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MediumLottoBall(number = result.bonusNumber)
                    Text(
                        text = stringResource(R.string.home_bonus),
                        fontSize = 9.sp,
                        color = TextSubLight,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1등 당첨 정보
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.home_first_prize_format, result.firstPrize.winnerCount),
                    fontSize = 14.sp,
                    color = TextSubLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(result.firstPrize.winAmount),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.home_total_sales_format, formatCurrencyShort(result.firstPrize.totalSalesAmount)),
                    fontSize = 12.sp,
                    color = TextSubLight
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onQrScanClick: () -> Unit,
    onManualInputClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // QR 스캔 버튼
        Button(
            onClick = onQrScanClick,
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Primary
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📱",
                    fontSize = 28.sp
                )
                Text(
                    stringResource(R.string.home_qr_check),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // 번호 직접 입력 버튼
        OutlinedButton(
            onClick = onManualInputClick,
            modifier = Modifier
                .weight(1f)
                .height(100.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = Primary
                )
                Text(
                    stringResource(R.string.home_manual_input),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextMainLight
                )
            }
        }
    }
}

@Composable
private fun PastDrawsSection(
    results: List<LottoResult>,
    onSelectResult: (LottoResult) -> Unit
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
                stringResource(R.string.home_past_draws),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextMainLight
            )
            Text(
                stringResource(R.string.home_view_more),
                style = MaterialTheme.typography.bodyMedium,
                color = TextSubLight
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            results.forEach { result ->
                PastDrawItem(
                    result = result,
                    onClick = { onSelectResult(result) }
                )
            }
        }
    }
}

@Composable
private fun PastDrawItem(
    result: LottoResult,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    stringResource(R.string.home_round_format, result.round),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextMainLight
                )
                Text(
                    formatDrawDate(result.drawDate),
                    fontSize = 12.sp,
                    color = TextSubLight
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                result.numbers.forEach { number ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(com.enso.home.ui.theme.getLottoBallColor(number)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundSelectionBottomSheet(
    results: List<LottoResult>,
    selectedRound: Int?,
    onSelectRound: (LottoResult) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            stringResource(R.string.home_select_round),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.height(400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results) { result ->
                Card(
                    onClick = { onSelectRound(result) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.round == selectedRound) {
                            Primary.copy(alpha = 0.1f)
                        } else {
                            CardLight
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.home_round_format, result.round),
                            fontWeight = if (result.round == selectedRound) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 18.sp,
                            color = if (result.round == selectedRound) Primary else TextMainLight
                        )
                        Text(
                            formatDrawDate(result.drawDate),
                            fontSize = 14.sp,
                            color = TextSubLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatCurrency(amount: Long): String {
    val billions = amount / 100000000
    val millions = (amount % 100000000) / 10000
    return if (billions > 0) {
        "${billions}억 ${millions.toString().replace("0+$".toRegex(), "")}만원"
    } else {
        "${millions}만원"
    }
}

private fun formatCurrencyShort(amount: Long): String {
    val billions = amount / 100000000
    return "${billions}억 원"
}

private fun formatDrawDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
    return formatter.format(date)
}

private fun formatTicketDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy.MM.dd 등록", Locale.KOREA)
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


