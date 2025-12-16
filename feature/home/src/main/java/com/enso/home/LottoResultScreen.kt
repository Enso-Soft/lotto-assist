package com.enso.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.domain.model.FirstPrizeInfo
import com.enso.domain.model.LottoResult
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview
@Composable
fun LottoResultScreen(
    viewModel: LottoResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LottoResultEffect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is LottoResultEffect.SyncCompleted -> {
                    // 동기화 완료 처리
                }
            }
        }
    }

    LottoResultContent(
        state = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = { viewModel.onEvent(LottoResultEvent.Refresh) },
        onSelectResult = { result -> viewModel.onEvent(LottoResultEvent.SelectResult(result)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LottoResultContent(
    state: LottoResultUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onSelectResult: (LottoResult) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("로또 당첨 결과") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            // TODO: QR 스캔 화면으로 이동
                        }
                    ) {
                        Text(
                            text = "QR 스캔",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isSyncing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isSyncing && state.lottoResults.isEmpty() -> {
                    LoadingContent()
                }
                state.error != null && state.lottoResults.isEmpty() -> {
                    ErrorContent(errorMessage = state.error)
                }
                else -> {
                    LottoResultsContent(
                        state = state,
                        onSelectResult = onSelectResult
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun LottoResultsContent(
    state: LottoResultUiState,
    onSelectResult: (LottoResult) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (state.isSyncing) {
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        state.selectedResult?.let { result ->
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                LottoResultDetail(lottoResult = result)
            }
        }

        if (state.lottoResults.isNotEmpty()) {
            Text(
                text = "전체 회차 (${state.lottoResults.size}개)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(
                    count = state.lottoResults.size,
                    key = { index -> state.lottoResults[index].round }
                ) { index ->
                    val result = state.lottoResults[index]
                    LottoResultListItem(
                        result = result,
                        isSelected = result.round == state.selectedResult?.round,
                        onClick = { onSelectResult(result) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LottoResultDetail(lottoResult: LottoResult) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RoundInfoCard(
            round = lottoResult.round,
            drawDate = lottoResult.drawDate
        )

        WinningNumbersCard(
            numbers = lottoResult.numbers,
            bonusNumber = lottoResult.bonusNumber
        )

        PrizeInfoCard(
            firstPrize = lottoResult.firstPrize
        )
    }
}

@Composable
private fun LottoResultListItem(
    result: LottoResult,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${result.round}회",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                result.numbers.forEach { number ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(getLottoBallColor(number, false)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = number.toString(),
                            style = MaterialTheme.typography.bodySmall,
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
private fun RoundInfoCard(round: Int, drawDate: Date) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${round}회 추첨 결과",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatDate(drawDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun WinningNumbersCard(numbers: List<Int>, bonusNumber: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "당첨 번호",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                numbers.forEach { number ->
                    LottoBall(
                        number = number,
                        modifier = Modifier.weight(1f)
                    )
                }

                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                LottoBall(
                    number = bonusNumber,
                    isBonus = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LottoBall(
    number: Int,
    isBonus: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(getLottoBallColor(number, isBonus)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun PrizeInfoCard(firstPrize: FirstPrizeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "당첨 정보",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PrizeInfoRow(
                label = "1등 당첨금",
                value = formatCurrency(firstPrize.winAmount)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            PrizeInfoRow(
                label = "1등 당첨자 수",
                value = "${firstPrize.winnerCount}명"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            PrizeInfoRow(
                label = "총 판매액",
                value = formatCurrency(firstPrize.totalSalesAmount)
            )
        }
    }
}

@Composable
private fun PrizeInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

private fun getLottoBallColor(number: Int, isBonus: Boolean): Color {
    return when {
        isBonus -> Color(0xFF9C27B0)
        number in 1..10 -> Color(0xFFFFC107)
        number in 11..20 -> Color(0xFF2196F3)
        number in 21..30 -> Color(0xFFF44336)
        number in 31..40 -> Color(0xFF9E9E9E)
        else -> Color(0xFF4CAF50)
    }
}

private fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.KOREA)
    return formatter.format(amount)
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
    return formatter.format(date)
}

@Preview(showBackground = true)
@Composable
private fun LottoResultDetailPreview() {
    MaterialTheme {
        LottoResultDetail(
            lottoResult = LottoResult(
                round = 1145,
                drawDate = Date(),
                numbers = listOf(3, 12, 17, 23, 28, 41),
                bonusNumber = 35,
                firstPrize = FirstPrizeInfo(
                    winAmount = 2_500_000_000,
                    winnerCount = 12,
                    totalSalesAmount = 125_000_000_000
                )
            )
        )
    }
}
