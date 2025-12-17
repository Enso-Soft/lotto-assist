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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
import com.enso.home.ui.components.HighlightedSmallLottoBall
import com.enso.home.ui.components.LottoBall
import com.enso.home.ui.components.ManualInputDialog
import com.enso.home.ui.components.MediumLottoBall
import com.enso.home.ui.components.SmallLottoBall
import com.enso.home.ui.components.TinyLottoBall
import com.enso.home.ui.components.WinningBadge
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
    onQrScanClick: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showManualInputDialog by remember { mutableStateOf(false) }
    var showRoundBottomSheet by remember { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var showAllTickets by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LottoResultEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LottoResultEffect.SyncCompleted -> {
                    snackbarHostState.showSnackbar("동기화 완료")
                }
                is LottoResultEffect.NavigateToQrScan -> {
                    onQrScanClick()
                }
                is LottoResultEffect.NavigateToManualInput -> {
                    showManualInputDialog = true
                }
                is LottoResultEffect.ShowTicketSaved -> {
                    snackbarHostState.showSnackbar("${effect.count}개의 번호가 저장되었습니다")
                }
                is LottoResultEffect.ShowWinningResult -> {
                    // 당첨/낙첨 스낵바 표시 안 함 (배지로 충분)
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
                        if (showAllTickets) "내 로또 전체보기" else "로또 홈",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundLight,
                    titleContentColor = TextMainLight
                ),
                navigationIcon = {
                    if (showAllTickets) {
                        IconButton(onClick = { showAllTickets = false }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "닫기",
                                tint = TextMainLight
                            )
                        }
                    }
                },
                actions = {
                    if (!showAllTickets) {
                        IconButton(onClick = { /* 설정 */ }) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "설정",
                                tint = TextMainLight
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!showAllTickets) {
                BottomNavigationBar()
            }
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showAllTickets) {
                // 전체보기 화면
                AllTicketsContent(
                    tickets = uiState.tickets,
                    lottoResults = uiState.lottoResults,
                    currentRound = uiState.currentRound,
                    currentSortType = uiState.ticketSortType,
                    onCheckWinning = { ticketId ->
                        viewModel.onEvent(LottoResultEvent.CheckWinning(ticketId))
                    },
                    onDeleteTicket = { ticketId ->
                        viewModel.onEvent(LottoResultEvent.DeleteTicket(ticketId))
                    },
                    onSortTypeChange = { sortType ->
                        viewModel.onEvent(LottoResultEvent.ChangeSortType(sortType))
                    }
                )
            } else {
                // 홈 화면
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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

                    // 내 로또 섹션
                    MyLottoSection(
                        tickets = uiState.tickets,
                        lottoResults = uiState.lottoResults,
                        currentRound = uiState.currentRound,
                        onCheckWinning = { ticketId ->
                            viewModel.onEvent(LottoResultEvent.CheckWinning(ticketId))
                        },
                        onDeleteTicket = { ticketId ->
                            viewModel.onEvent(LottoResultEvent.DeleteTicket(ticketId))
                        },
                        onViewAll = { showAllTickets = true }
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
                    text = "당첨 축하드려요!",
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
                text = "${selectedResult?.round ?: 0}회",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = TextMainLight
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = "회차 선택",
                modifier = Modifier.size(32.dp),
                tint = TextSubLight
            )
            Text(
                text = "당첨결과",
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
                        text = "보너스",
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
                    text = "1등 당첨금 (${result.firstPrize.winnerCount}명)",
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
                    text = "총 판매금액 ${formatCurrencyShort(result.firstPrize.totalSalesAmount)}",
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
                    "QR 당첨 확인",
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
                    "번호 직접 입력",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextMainLight
                )
            }
        }
    }
}

@Composable
private fun MyLottoSection(
    tickets: List<LottoTicket>,
    lottoResults: List<LottoResult>,
    currentRound: Int,
    onCheckWinning: (Long) -> Unit,
    onDeleteTicket: (Long) -> Unit,
    onViewAll: () -> Unit = {}
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
                "내 로또",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextMainLight
            )
            Text(
                "전체보기",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSubLight,
                modifier = Modifier.clickable(onClick = onViewAll)
            )
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
                    onDelete = { onDeleteTicket(ticket.ticketId) }
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
                "저장된 로또 번호가 없습니다\nQR 스캔 또는 직접 입력으로 추가해보세요",
                textAlign = TextAlign.Center,
                color = TextSubLight,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TicketCard(
    ticket: LottoTicket,
    lottoResult: LottoResult?,
    currentRound: Int,
    onCheckWinning: () -> Unit,
    onDelete: () -> Unit
) {
    val isDrawComplete = ticket.round <= currentRound

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
                Column {
                    Text(
                        "제 ${ticket.round}회",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextMainLight
                    )
                    Text(
                        formatTicketDate(ticket.registeredDate),
                        fontSize = 11.sp,
                        color = TextSubLight,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
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

            // 당첨번호 및 당첨금 정보
            if (lottoResult != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Primary.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 당첨번호
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "당첨번호",
                            fontSize = 11.sp,
                            color = TextSubLight,
                            modifier = Modifier.width(48.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            lottoResult.numbers.forEach { number ->
                                TinyLottoBall(number = number)
                            }
                            Text(
                                text = "+",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSubLight,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                            TinyLottoBall(number = lottoResult.bonusNumber)
                        }
                    }

                    // 1등 당첨금
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "1등 당첨금",
                            fontSize = 11.sp,
                            color = TextSubLight,
                            modifier = Modifier.width(48.dp)
                        )
                        Text(
                            formatPrizeAmount(lottoResult.firstPrize.winAmount),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
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
                "지난 회차 정보",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextMainLight
            )
            Text(
                "더보기",
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
                    "${result.round}회",
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
            "회차 선택",
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
                            "${result.round}회",
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

@Composable
private fun BottomNavigationBar() {
    NavigationBar(
        containerColor = CardLight
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { },
            icon = { Icon(Icons.Default.Home, "홈") },
            label = { Text("홈", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.List, "당첨내역") },
            label = { Text("당첨내역", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.List, "내 번호") },
            label = { Text("내 번호", fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = { Icon(Icons.Default.List, "판매점") },
            label = { Text("판매점", fontSize = 10.sp) }
        )
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

@Composable
private fun AllTicketsContent(
    tickets: List<LottoTicket>,
    lottoResults: List<LottoResult>,
    currentRound: Int,
    currentSortType: TicketSortType,
    onCheckWinning: (Long) -> Unit,
    onDeleteTicket: (Long) -> Unit,
    onSortTypeChange: (TicketSortType) -> Unit
) {
    var showSortBottomSheet by remember { mutableStateOf(false) }

    if (tickets.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "저장된 로또 번호가 없습니다\nQR 스캔 또는 직접 입력으로 추가해보세요",
                textAlign = TextAlign.Center,
                color = TextSubLight,
                fontSize = 14.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 통계 헤더
            item {
                AllTicketsHeader(
                    totalCount = tickets.size,
                    winningCount = tickets.sumOf { ticket ->
                        ticket.games.count { it.winningRank in 1..5 }
                    }
                )
            }

            // 정렬 선택 버튼
            item {
                SortButton(
                    currentSortType = currentSortType,
                    onClick = { showSortBottomSheet = true }
                )
            }

            // 전체 티켓 목록
            items(tickets.size) { index ->
                val ticket = tickets[index]
                TicketCard(
                    ticket = ticket,
                    lottoResult = lottoResults.find { it.round == ticket.round },
                    currentRound = currentRound,
                    onCheckWinning = { onCheckWinning(ticket.ticketId) },
                    onDelete = { onDeleteTicket(ticket.ticketId) }
                )
            }
        }
    }

    if (showSortBottomSheet) {
        SortSelectionBottomSheet(
            currentSortType = currentSortType,
            onSelectSortType = { sortType ->
                onSortTypeChange(sortType)
                showSortBottomSheet = false
            },
            onDismiss = { showSortBottomSheet = false }
        )
    }
}

@Composable
private fun AllTicketsHeader(
    totalCount: Int,
    winningCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalCount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
                Text(
                    text = "전체 번호",
                    fontSize = 12.sp,
                    color = TextSubLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(TextSubLight.copy(alpha = 0.2f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$winningCount",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = com.enso.home.ui.theme.WinningGreen
                )
                Text(
                    text = "당첨 (1-5등)",
                    fontSize = 12.sp,
                    color = TextSubLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SortButton(
    currentSortType: TicketSortType,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardLight),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "정렬",
                    fontSize = 14.sp,
                    color = TextSubLight
                )
                Text(
                    text = currentSortType.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }
            Icon(
                Icons.Default.ArrowDropDown,
                contentDescription = "정렬 선택",
                tint = TextSubLight,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSelectionBottomSheet(
    currentSortType: TicketSortType,
    onSelectSortType: (TicketSortType) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                "정렬 기준",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextMainLight,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TicketSortType.values().forEach { sortType ->
                Card(
                    onClick = { onSelectSortType(sortType) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (sortType == currentSortType) {
                            Primary.copy(alpha = 0.1f)
                        } else {
                            CardLight
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            sortType.displayName,
                            fontWeight = if (sortType == currentSortType) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 16.sp,
                            color = if (sortType == currentSortType) Primary else TextMainLight
                        )
                        if (sortType == currentSortType) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "선택됨",
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
