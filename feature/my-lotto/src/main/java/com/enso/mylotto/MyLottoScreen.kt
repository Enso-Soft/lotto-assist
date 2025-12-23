package com.enso.mylotto

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.home.ui.components.SortButton
import com.enso.home.ui.components.SortSelectionBottomSheet
import com.enso.home.ui.components.TicketCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLottoScreen(
    viewModel: MyLottoViewModel = hiltViewModel()
) {
    val lottoColors = LocalLottoColors.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSortBottomSheet by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MyLottoEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MyLottoEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is MyLottoEffect.TicketDeleted -> {
                    // Optional: Handle ticket deleted
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.my_lotto_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = lottoColors.backgroundLight,
                    titleContentColor = lottoColors.textMainLight
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = lottoColors.backgroundLight
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.onEvent(MyLottoEvent.Refresh) },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = lottoColors.primary)
                }
            } else if (uiState.tickets.isEmpty()) {
                EmptyState()
            } else {
                MyLottoContent(
                    uiState = uiState,
                    onSortClick = { showSortBottomSheet = true },
                    onDeleteTicket = { ticketId ->
                        viewModel.onEvent(MyLottoEvent.DeleteTicket(ticketId))
                    },
                    onCheckWinning = { ticketId ->
                        viewModel.onEvent(MyLottoEvent.CheckWinning(ticketId))
                    }
                )
            }
        }
    }

    if (showSortBottomSheet) {
        SortSelectionBottomSheet(
            currentSortType = uiState.sortType,
            onSelectSortType = { sortType ->
                viewModel.onEvent(MyLottoEvent.ChangeSortType(sortType))
                showSortBottomSheet = false
            },
            onDismiss = { showSortBottomSheet = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MyLottoContent(
    uiState: MyLottoUiState,
    onSortClick: () -> Unit,
    onDeleteTicket: (Long) -> Unit,
    onCheckWinning: (Long) -> Unit
) {
    val lottoColors = LocalLottoColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 섹션 헤더
        item(key = "header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.my_lotto_registered_tickets, uiState.tickets.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = lottoColors.textMainLight
                )
            }
        }

        // 정렬 버튼 (Sticky Header)
        stickyHeader(key = "sort_button") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(lottoColors.backgroundLight)
                    .padding(bottom = 16.dp)
            ) {
                SortButton(
                    currentSortType = uiState.sortType,
                    onClick = onSortClick
                )
            }
        }

        // 티켓 목록
        items(
            items = uiState.tickets,
            key = { ticket -> ticket.ticketId }
        ) { ticket ->
            Box(modifier = Modifier.animateItem()) {
                TicketCard(
                    ticket = ticket,
                    lottoResult = uiState.lottoResults.find { it.round == ticket.round },
                    currentRound = uiState.currentRound,
                    onCheckWinning = { onCheckWinning(ticket.ticketId) },
                    onDelete = { onDeleteTicket(ticket.ticketId) }
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    val lottoColors = LocalLottoColors.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "📋",
                fontSize = 48.sp
            )
            Text(
                stringResource(R.string.my_lotto_empty_message),
                textAlign = TextAlign.Center,
                color = lottoColors.textSubLight,
                fontSize = 14.sp
            )
        }
    }
}
