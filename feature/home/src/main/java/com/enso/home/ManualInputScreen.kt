package com.enso.home

import android.view.HapticFeedbackConstants
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.enso.designsystem.modifier.scaleOnPress
import com.enso.designsystem.theme.AppShapes
import com.enso.util.lotto_date.LottoDate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.enso.designsystem.component.GameCompletionStatus
import com.enso.designsystem.component.GameTabPillGroup
import com.enso.designsystem.component.LottoPrimaryButtonFull
import com.enso.designsystem.component.LottoTextButton
import com.enso.designsystem.component.PillButtonGroup
import com.enso.designsystem.component.SlotMachineNumber
import com.enso.designsystem.theme.LocalLottoColors
import com.enso.home.ui.components.GameCompleteBottomSheet
import com.enso.home.ui.components.NumberSelectionGrid
import com.enso.home.ui.components.SelectedBallsRow
import kotlinx.coroutines.launch

/**
 * 번호 직접 입력 화면
 *
 * Full Screen으로 다중 게임 입력을 지원합니다.
 *
 * @param currentRound 현재 회차
 * @param viewModel ManualInputViewModel
 * @param onNavigateBack 뒤로 네비게이션 콜백
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    currentRound: Int,
    viewModel: ManualInputViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val completeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val roundSelectionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val view = LocalView.current
    val lottoColors = LocalLottoColors.current

    // 회차 설정
    LaunchedEffect(currentRound) {
        if (currentRound > 0) {
            viewModel.onEvent(ManualInputEvent.ChangeRound(currentRound))
        }
    }

    // 뒤로가기 처리
    BackHandler {
        viewModel.onEvent(ManualInputEvent.OnBackPressed)
    }

    // Effect 처리
    LaunchedEffect(Unit) {
        viewModel.  effect.collect { effect ->
            when (effect) {
                is ManualInputEffect.NavigateBack -> onNavigateBack()
                is ManualInputEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is ManualInputEffect.SaveSuccess -> {
                    snackbarHostState.showSnackbar("${effect.count}게임이 저장되었어요")
                }
                is ManualInputEffect.Haptic -> {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                }
                is ManualInputEffect.HapticSuccess -> {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            ManualInputTopAppBar(
                currentRound = uiState.currentRound,
                onBackClick = { viewModel.onEvent(ManualInputEvent.OnBackPressed) },
                onRoundClick = { viewModel.onEvent(ManualInputEvent.ShowRoundSelection) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            LottoPrimaryButtonFull(
                text = stringResource(R.string.manual_input_save),
                onClick = { viewModel.onEvent(ManualInputEvent.ShowSaveConfirm) },
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            )
        },
        containerColor = lottoColors.backgroundLight
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // 게임 탭
            GameTabPillGroup(
                games = uiState.games.map { game ->
                    game.id to when {
                        game.isComplete -> GameCompletionStatus.COMPLETE
                        game.isInProgress -> GameCompletionStatus.IN_PROGRESS
                        else -> GameCompletionStatus.EMPTY
                    }
                },
                selectedIndex = uiState.selectedGameIndex,
                onSelectionChange = { viewModel.onEvent(ManualInputEvent.SelectGame(it)) },
                canAddGame = uiState.canAddGame,
                onAddGame = { viewModel.onEvent(ManualInputEvent.AddGame) },
                onGameLongPress = { viewModel.onEvent(ManualInputEvent.ShowDeleteGameDialog(it)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 선택된 번호 + 자동/수동 토글
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.manual_input_selected_count_format,
                        uiState.currentGame.numbers.size
                    ),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = lottoColors.textPrimary
                )
                PillButtonGroup(
                    items = listOf(
                        stringResource(R.string.manual_input_auto),
                        stringResource(R.string.manual_input_manual)
                    ),
                    selectedIndex = if (uiState.currentGame.isAuto) 0 else 1,
                    onSelectionChange = { index ->
                        viewModel.onEvent(ManualInputEvent.SetAutoMode(index == 0))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 선택된 공 슬롯
            SelectedBallsRow(
                selectedNumbers = uiState.currentGame.numbers,
                onBallClick = { viewModel.onEvent(ManualInputEvent.DeselectNumber(it)) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 초기화 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                LottoTextButton(
                    text = stringResource(R.string.manual_input_reset),
                    onClick = { viewModel.onEvent(ManualInputEvent.ResetCurrentGame) },
                    enabled = uiState.currentGame.numbers.isNotEmpty()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 번호 그리드
            NumberSelectionGrid(
                selectedNumbers = uiState.currentGame.numbers,
                onNumberClick = { number ->
                    if (uiState.currentGame.numbers.contains(number)) {
                        viewModel.onEvent(ManualInputEvent.DeselectNumber(number))
                    } else {
                        viewModel.onEvent(ManualInputEvent.SelectNumber(number))
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // 완료 바텀시트 (6개 선택 완료 시 또는 저장하기 버튼 클릭 시)
    if (uiState.showCompleteBottomSheet) {
        GameCompleteBottomSheet(
            gameName = uiState.currentGame.id,
            numbers = uiState.currentGame.numbers,
            sheetState = completeBottomSheetState,
            canAddGame = uiState.canAddOrMoveToNextGame,
            onAddGame = { viewModel.onEvent(ManualInputEvent.ConfirmAddGameFromBottomSheet) },
            onSave = { viewModel.onEvent(ManualInputEvent.Save) },
            onDismiss = { viewModel.onEvent(ManualInputEvent.DismissBottomSheet) }
        )
    }

    // 회차 선택 바텀시트
    if (uiState.showRoundSelectionBottomSheet) {
        RoundSelectionBottomSheet(
            upcomingRound = uiState.upcomingRound,
            availableRounds = uiState.availableRounds,
            selectedRound = uiState.currentRound,
            sheetState = roundSelectionSheetState,
            onSelectRound = { round ->
                viewModel.onEvent(ManualInputEvent.ChangeRound(round))
            },
            onDismiss = { viewModel.onEvent(ManualInputEvent.DismissRoundSelection) }
        )
    }

    // 나가기 확인 다이얼로그
    if (uiState.showExitConfirmDialog) {
        ExitConfirmDialog(
            onDismiss = { viewModel.onEvent(ManualInputEvent.DismissExitConfirmDialog) },
            onConfirm = { viewModel.onEvent(ManualInputEvent.ConfirmExit) }
        )
    }

    // 게임 삭제 확인 다이얼로그
    uiState.showDeleteGameDialog?.let { index ->
        val gameName = uiState.games.getOrNull(index)?.id ?: ""
        DeleteGameDialog(
            gameName = gameName,
            onDismiss = { viewModel.onEvent(ManualInputEvent.DismissDeleteGameDialog) },
            onConfirm = { viewModel.onEvent(ManualInputEvent.ConfirmDeleteGame) }
        )
    }
}

/**
 * 상단 앱바
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualInputTopAppBar(
    currentRound: Int,
    onBackClick: () -> Unit,
    onRoundClick: () -> Unit
) {
    val lottoColors = LocalLottoColors.current

    TopAppBar(
        title = {
            Text(
                text = stringResource(R.string.manual_input_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로가기"
                )
            }
        },
        actions = {
            TextButton(onClick = onRoundClick) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SlotMachineNumber(
                        targetNumber = currentRound,
                        totalDurationMs = 200,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        textColor = lottoColors.textPrimary
                    )
                    Text(
                        text = stringResource(R.string.home_round_suffix),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = lottoColors.textPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "회차 선택",
                    tint = lottoColors.textSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = lottoColors.backgroundLight,
            titleContentColor = lottoColors.textPrimary
        )
    )
}

/**
 * 나가기 확인 다이얼로그
 */
@Composable
private fun ExitConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.manual_input_exit_confirm_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = stringResource(R.string.manual_input_exit_confirm_message))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.manual_input_exit_confirm_leave))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.manual_input_exit_confirm_stay))
            }
        }
    )
}

/**
 * 게임 삭제 확인 다이얼로그
 */
@Composable
private fun DeleteGameDialog(
    gameName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.manual_input_delete_game_title, gameName),
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = stringResource(R.string.manual_input_delete_game_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.dialog_cancel))
            }
        }
    )
}


/**
 * 회차 선택 바텀시트
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoundSelectionBottomSheet(
    upcomingRound: Int,
    availableRounds: List<Int>,
    selectedRound: Int,
    sheetState: SheetState,
    onSelectRound: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val lottoColors = LocalLottoColors.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = lottoColors.cardLight
    ) {
        RoundSelectionContent(
            upcomingRound = upcomingRound,
            availableRounds = availableRounds,
            selectedRound = selectedRound,
            onSelectRound = onSelectRound
        )
    }
}


/**
 * 회차 선택 컨텐츠 (홈 화면과 동일 스타일)
 */
@Composable
private fun RoundSelectionContent(
    upcomingRound: Int,
    availableRounds: List<Int>,
    selectedRound: Int,
    onSelectRound: (Int) -> Unit
) {
    val lottoColors = LocalLottoColors.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 첫 번째 보이는 아이템이 20개 이상 스크롤되었는지 확인
    val showScrollToTopButton by remember {
        derivedStateOf { listState.firstVisibleItemIndex >= 20 }
    }

    // 선택된 회차의 인덱스를 찾아서 해당 위치로 스크롤
    LaunchedEffect(selectedRound) {
        val index = if (selectedRound == upcomingRound) {
            0
        } else {
            availableRounds.indexOf(selectedRound) + 1 // +1 for upcoming round
        }
        if (index >= 0) {
            listState.scrollToItem(index)
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.home_select_round),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = lottoColors.textPrimary,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.height(400.dp)
            ) {
                // 미추첨 회차 (맨 위)
                item(key = "upcoming_$upcomingRound") {
                    RoundSelectionRow(
                        round = upcomingRound,
                        isSelected = selectedRound == upcomingRound,
                        isUpcoming = true,
                        onClick = { onSelectRound(upcomingRound) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = lottoColors.divider
                    )
                }

                // 추첨 완료된 회차들
                itemsIndexed(availableRounds, key = { _, round -> round }) { _, round ->
                    RoundSelectionRow(
                        round = round,
                        isSelected = selectedRound == round,
                        isUpcoming = false,
                        onClick = { onSelectRound(round) }
                    )
                }
            }
        }

        // 맨 위로 이동 플로팅 버튼
        AnimatedVisibility(
            visible = showScrollToTopButton,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(lottoColors.primary.copy(alpha = 0.6f))
                    .clickable {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "맨 위로",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * 회차 선택 행
 */
@Composable
private fun RoundSelectionRow(
    round: Int,
    isSelected: Boolean,
    isUpcoming: Boolean,
    onClick: () -> Unit
) {
    val lottoColors = LocalLottoColors.current
    val drawDate = remember(round) {
        LottoDate.getDrawDateByNumber(round)
    }
    val dateText = remember(drawDate) {
        with(LottoDate) { drawDate.formatDate() }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) lottoColors.accentContainer else Color.Transparent
            )
            .scaleOnPress(shape = RoundedCornerShape(12.dp), onClick = onClick)
            .padding(horizontal = 20.dp, vertical = if (isUpcoming) 14.dp else 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isUpcoming) {
            // 미추첨: 세로 배치 (회차 + 날짜)
            Column {
                Text(
                    stringResource(R.string.home_round_format, round),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isSelected) lottoColors.accent else lottoColors.textPrimary
                )
                Text(
                    dateText,
                    fontSize = 12.sp,
                    color = lottoColors.textSecondary
                )
            }
            // 배지
            Box(
                modifier = Modifier
                    .background(
                        color = lottoColors.accent.copy(alpha = 0.15f),
                        shape = AppShapes.Badge
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.manual_input_round_upcoming),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = lottoColors.accent
                )
            }
        } else {
            // 추첨 완료: 가로 배치 (회차 | 날짜)
            Text(
                stringResource(R.string.home_round_format, round),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isSelected) lottoColors.accent else lottoColors.textPrimary
            )
            Text(
                dateText,
                fontSize = 14.sp,
                color = lottoColors.textSecondary
            )
        }
    }
}
