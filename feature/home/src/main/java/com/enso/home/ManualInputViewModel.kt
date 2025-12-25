package com.enso.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoTicket
import com.enso.domain.repository.UserPreferencesRepository
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.SaveLottoTicketUseCase
import com.enso.util.lotto_date.LottoDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ManualInputViewModel @Inject constructor(
    private val saveLottoTicketUseCase: SaveLottoTicketUseCase,
    private val getAllLottoResultsUseCase: GetAllLottoResultsUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialRound: Int = savedStateHandle["currentRound"] ?: 0
    private val upcomingRound: Int = LottoDate.getUpcomingDrawRound()

    private val _uiState = MutableStateFlow(
        ManualInputUiState(
            currentRound = initialRound,
            upcomingRound = upcomingRound
        )
    )
    val uiState: StateFlow<ManualInputUiState> = _uiState.asStateFlow()

    private val _effect = Channel<ManualInputEffect>(Channel.BUFFERED)
    val effect: Flow<ManualInputEffect> = _effect.receiveAsFlow()

    init {
        loadDefaultIsAuto()
        loadAvailableRounds()
    }

    fun onEvent(event: ManualInputEvent) {
        when (event) {
            is ManualInputEvent.Initialize -> initialize(event.round)
            is ManualInputEvent.SelectNumber -> selectNumber(event.number)
            is ManualInputEvent.DeselectNumber -> deselectNumber(event.number)
            is ManualInputEvent.SelectGame -> selectGame(event.index)
            is ManualInputEvent.AddGame -> addGame()
            is ManualInputEvent.RemoveGame -> removeGame(event.index)
            is ManualInputEvent.SetAutoMode -> setAutoMode(event.isAuto)
            is ManualInputEvent.ResetCurrentGame -> resetCurrentGame()
            is ManualInputEvent.ShowSaveConfirm -> showSaveConfirm()
            is ManualInputEvent.Save -> save()
            is ManualInputEvent.DismissBottomSheet -> dismissBottomSheet()
            is ManualInputEvent.ConfirmAddGameFromBottomSheet -> addGameFromBottomSheet()
            is ManualInputEvent.ChangeRound -> changeRound(event.round)
            is ManualInputEvent.ShowRoundSelection -> showRoundSelection()
            is ManualInputEvent.DismissRoundSelection -> dismissRoundSelection()
            is ManualInputEvent.OnBackPressed -> handleBackPressed()
            is ManualInputEvent.DismissExitConfirmDialog -> dismissExitConfirmDialog()
            is ManualInputEvent.ConfirmExit -> confirmExit()
            is ManualInputEvent.ShowDeleteGameDialog -> showDeleteGameDialog(event.index)
            is ManualInputEvent.DismissDeleteGameDialog -> dismissDeleteGameDialog()
            is ManualInputEvent.ConfirmDeleteGame -> confirmDeleteGame()
        }
    }

    private fun initialize(round: Int) {
        viewModelScope.launch {
            val isAuto = userPreferencesRepository.getManualInputDefaultIsAuto().first()
            _uiState.update {
                ManualInputUiState(
                    currentRound = round,
                    games = listOf(ManualGame.create(0, isAuto)),
                    selectedGameIndex = 0,
                    defaultIsAuto = isAuto,
                    upcomingRound = it.upcomingRound, // 미추첨 회차 유지
                    availableRounds = it.availableRounds // 기존 회차 목록 유지
                )
            }
        }
    }

    private fun loadDefaultIsAuto() {
        viewModelScope.launch {
            val isAuto = userPreferencesRepository.getManualInputDefaultIsAuto().first()
            _uiState.update { state ->
                val updatedGames = state.games.map { game ->
                    if (game.isEmpty) game.copy(isAuto = isAuto) else game
                }
                state.copy(
                    defaultIsAuto = isAuto,
                    games = updatedGames
                )
            }
        }
    }

    private fun loadAvailableRounds() {
        viewModelScope.launch {
            getAllLottoResultsUseCase().collect { results ->
                val rounds = results.map { it.round }.sortedDescending()
                _uiState.update { it.copy(availableRounds = rounds) }
            }
        }
    }

    private fun selectNumber(number: Int) {
        val currentState = _uiState.value
        val currentGame = currentState.currentGame

        if (currentGame.numbers.size >= 6) return
        if (currentGame.numbers.contains(number)) return

        val updatedNumbers = currentGame.numbers + number
        updateCurrentGame(currentGame.copy(numbers = updatedNumbers))

        viewModelScope.launch {
            _effect.send(ManualInputEffect.Haptic)
        }

        // 6개 완료 시 바텀시트 표시
        if (updatedNumbers.size == 6) {
            viewModelScope.launch {
                _effect.send(ManualInputEffect.HapticSuccess)
            }
            _uiState.update { it.copy(showCompleteBottomSheet = true) }
        }
    }

    private fun deselectNumber(number: Int) {
        val currentGame = _uiState.value.currentGame
        val updatedNumbers = currentGame.numbers - number
        updateCurrentGame(currentGame.copy(numbers = updatedNumbers))

        viewModelScope.launch {
            _effect.send(ManualInputEffect.Haptic)
        }
    }

    private fun selectGame(index: Int) {
        if (index in _uiState.value.games.indices) {
            _uiState.update { it.copy(selectedGameIndex = index) }
        }
    }

    private fun addGame() {
        val currentState = _uiState.value
        if (!currentState.canAddGame) return

        val newGameIndex = currentState.games.size
        val newGame = ManualGame.create(newGameIndex, currentState.defaultIsAuto)

        _uiState.update {
            it.copy(
                games = it.games + newGame,
                selectedGameIndex = newGameIndex
            )
        }
    }

    private fun removeGame(index: Int) {
        val currentState = _uiState.value
        if (currentState.games.size <= 1) return
        if (index !in currentState.games.indices) return

        val gameToRemove = currentState.games[index]

        // 데이터가 있으면 삭제 확인 다이얼로그 표시
        if (gameToRemove.numbers.isNotEmpty()) {
            _uiState.update { it.copy(showDeleteGameDialog = index) }
        } else {
            performRemoveGame(index)
        }
    }

    private fun performRemoveGame(index: Int) {
        val currentState = _uiState.value
        val updatedGames = currentState.games
            .filterIndexed { i, _ -> i != index }
            .mapIndexed { i, game -> game.copy(id = ManualGame.getGameIds()[i]) }

        val newSelectedIndex = when {
            currentState.selectedGameIndex >= updatedGames.size -> updatedGames.size - 1
            currentState.selectedGameIndex > index -> currentState.selectedGameIndex - 1
            else -> currentState.selectedGameIndex
        }

        _uiState.update {
            it.copy(
                games = updatedGames,
                selectedGameIndex = newSelectedIndex,
                showDeleteGameDialog = null
            )
        }
    }

    private fun setAutoMode(isAuto: Boolean) {
        updateCurrentGame(_uiState.value.currentGame.copy(isAuto = isAuto))

        viewModelScope.launch {
            userPreferencesRepository.saveManualInputDefaultIsAuto(isAuto)
        }
    }

    private fun resetCurrentGame() {
        val currentGame = _uiState.value.currentGame
        updateCurrentGame(currentGame.copy(numbers = emptyList()))

        viewModelScope.launch {
            _effect.send(ManualInputEffect.Haptic)
        }
    }

    private fun save() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val completedGames = currentState.games.filter { it.isComplete }

            if (completedGames.isEmpty()) return@launch

            _uiState.update {
                it.copy(
                    isLoading = true,
                    showCompleteBottomSheet = false
                )
            }

            val lottoGames = completedGames.map { game ->
                LottoGame(
                    gameLabel = game.id,
                    numbers = game.numbers.sorted(),
                    gameType = if (game.isAuto) GameType.AUTO else GameType.MANUAL
                )
            }

            val ticket = LottoTicket(
                round = currentState.currentRound,
                registeredDate = Date(),
                games = lottoGames
            )

            saveLottoTicketUseCase(ticket)
                .onSuccess {
                    // 나갈 때 상태 초기화
                    val isAuto = userPreferencesRepository.getManualInputDefaultIsAuto().first()
                    _uiState.update {
                        ManualInputUiState(
                            currentRound = it.currentRound,
                            games = listOf(ManualGame.create(0, isAuto)),
                            defaultIsAuto = isAuto,
                            upcomingRound = it.upcomingRound,
                            availableRounds = it.availableRounds
                        )
                    }
                    _effect.send(ManualInputEffect.NavigateBack)
                }
                .onFailure { error ->
                    _effect.send(ManualInputEffect.ShowSnackbar("저장 실패: ${error.message}"))
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun dismissBottomSheet() {
        _uiState.update { it.copy(showCompleteBottomSheet = false) }
    }

    private fun showSaveConfirm() {
        // 저장하기 버튼 클릭 시 GameCompleteBottomSheet 표시 (자동 트리거와 동일)
        _uiState.update { it.copy(showCompleteBottomSheet = true) }
    }

    private fun addGameFromBottomSheet() {
        _uiState.update { it.copy(showCompleteBottomSheet = false) }

        val currentState = _uiState.value
        val nextGameIndex = currentState.selectedGameIndex + 1
        val nextGame = currentState.games.getOrNull(nextGameIndex)

        when {
            // 다음 게임이 없음 → 새 게임 추가
            nextGame == null -> addGame()
            // 다음 게임이 완료됨 → 새 게임 추가
            nextGame.isComplete -> addGame()
            // 다음 게임이 미완료 → 다음 게임으로 이동
            else -> selectGame(nextGameIndex)
        }
    }

    private fun changeRound(round: Int) {
        _uiState.update { it.copy(currentRound = round, showRoundSelectionBottomSheet = false) }
    }

    private fun showRoundSelection() {
        _uiState.update { it.copy(showRoundSelectionBottomSheet = true) }
    }

    private fun dismissRoundSelection() {
        _uiState.update { it.copy(showRoundSelectionBottomSheet = false) }
    }

    private fun handleBackPressed() {
        val currentState = _uiState.value

        if (currentState.hasUnsavedChanges) {
            _uiState.update { it.copy(showExitConfirmDialog = true) }
        } else {
            navigateBackWithReset()
        }
    }

    private fun dismissExitConfirmDialog() {
        _uiState.update { it.copy(showExitConfirmDialog = false) }
    }

    private fun confirmExit() {
        _uiState.update { it.copy(showExitConfirmDialog = false) }
        navigateBackWithReset()
    }

    /**
     * 상태 초기화 후 뒤로가기
     * 다음 진입 시 깨끗한 상태를 보여주기 위해 나갈 때 초기화
     */
    private fun navigateBackWithReset() {
        viewModelScope.launch {
            val isAuto = userPreferencesRepository.getManualInputDefaultIsAuto().first()
            _uiState.update {
                ManualInputUiState(
                    currentRound = it.currentRound,
                    games = listOf(ManualGame.create(0, isAuto)),
                    defaultIsAuto = isAuto,
                    upcomingRound = it.upcomingRound,
                    availableRounds = it.availableRounds
                )
            }
            _effect.send(ManualInputEffect.NavigateBack)
        }
    }

    private fun showDeleteGameDialog(index: Int) {
        _uiState.update { it.copy(showDeleteGameDialog = index) }
    }

    private fun dismissDeleteGameDialog() {
        _uiState.update { it.copy(showDeleteGameDialog = null) }
    }

    private fun confirmDeleteGame() {
        val indexToDelete = _uiState.value.showDeleteGameDialog ?: return
        performRemoveGame(indexToDelete)
    }

    private fun updateCurrentGame(updatedGame: ManualGame) {
        val currentState = _uiState.value
        val updatedGames = currentState.games.toMutableList()
        updatedGames[currentState.selectedGameIndex] = updatedGame

        _uiState.update { it.copy(games = updatedGames) }
    }
}
