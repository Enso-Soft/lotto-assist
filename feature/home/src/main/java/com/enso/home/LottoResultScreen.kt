package com.enso.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.domain.model.LottoResult
import com.enso.domain.model.UserLottoTicket
import com.enso.home.ui.components.LottoBall
import com.enso.home.ui.components.ManualInputDialog
import com.enso.home.ui.components.SmallLottoBall
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
                    val message = when (effect.rank) {
                        1 -> "축하합니다! 1등 당첨!"
                        2 -> "축하합니다! 2등 당첨!"
                        3 -> "3등 당첨!"
                        4 -> "4등 당첨!"
                        5 -> "5등 당첨!"
                        else -> "낙첨입니다"
                    }
                    snackbarHostState.showSnackbar(message)
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
                        "로또 홈",
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
                            contentDescription = "설정",
                            tint = TextMainLight
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            BottomNavigationBar()
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                    tickets = uiState.userTickets,
                    selectedRound = uiState.selectedResult?.round,
                    onCheckWinning = { ticketId ->
                        viewModel.onEvent(LottoResultEvent.CheckWinning(ticketId))
                    },
                    onDeleteTicket = { ticketId ->
                        viewModel.onEvent(LottoResultEvent.DeleteTicket(ticketId))
                    }
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
            // 당첨 번호
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                result.numbers.forEach { number ->
                    LottoBall(number = number)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 보너스 번호
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+",
                    fontSize = 20.sp,
                    color = TextSubLight,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LottoBall(number = result.bonusNumber)
                    Text(
                        text = "보너스",
                        fontSize = 10.sp,
                        color = TextSubLight,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

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
    tickets: List<UserLottoTicket>,
    selectedRound: Int?,
    onCheckWinning: (Long) -> Unit,
    onDeleteTicket: (Long) -> Unit
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
                color = TextSubLight
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (tickets.isEmpty()) {
            EmptyTicketCard()
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardLight),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    tickets.take(3).forEach { ticket ->
                        TicketItem(
                            ticket = ticket,
                            onCheckWinning = { onCheckWinning(ticket.id) },
                            onDelete = { onDeleteTicket(ticket.id) }
                        )
                    }
                }
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
private fun TicketItem(
    ticket: UserLottoTicket,
    onCheckWinning: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "제 ${ticket.round}회",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextMainLight
                    )
                    if (ticket.isChecked) {
                        WinningBadge(rank = ticket.winningRank)
                    }
                }
                Text(
                    formatTicketDate(ticket.registeredDate),
                    fontSize = 12.sp,
                    color = TextSubLight,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ticket.numbers.forEach { number ->
                        SmallLottoBall(number = number)
                    }
                }
                Text(
                    "${ticket.gameType.displayName}",
                    fontSize = 10.sp,
                    color = TextSubLight,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        if (!ticket.isChecked) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onCheckWinning,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("당첨 확인")
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
