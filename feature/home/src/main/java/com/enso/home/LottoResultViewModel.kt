package com.enso.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
import com.enso.domain.usecase.CheckTicketWinningUseCase
import com.enso.domain.usecase.DeleteLottoTicketUseCase
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLottoResultUseCase
import com.enso.domain.usecase.GetLottoTicketsUseCase
import com.enso.domain.usecase.SaveLottoTicketUseCase
import com.enso.domain.usecase.SyncLottoResultsUseCase
import com.enso.util.lotto_date.LottoDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class LottoResultViewModel @Inject constructor(
    private val getLottoResultUseCase: GetLottoResultUseCase,
    private val getAllLottoResultsUseCase: GetAllLottoResultsUseCase,
    private val syncLottoResultsUseCase: SyncLottoResultsUseCase,
    private val getLottoTicketsUseCase: GetLottoTicketsUseCase,
    private val saveLottoTicketUseCase: SaveLottoTicketUseCase,
    private val deleteLottoTicketUseCase: DeleteLottoTicketUseCase,
    private val checkTicketWinningUseCase: CheckTicketWinningUseCase,
    private val lottoRepository: com.enso.domain.repository.LottoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LottoResultUiState())
    val state: StateFlow<LottoResultUiState> = _state.asStateFlow()

    private val _effect = Channel<LottoResultEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeAllResults()
        observeUserTickets()
        checkAndLoadInitialData()
    }

    private fun checkAndLoadInitialData() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        _state.update { it.copy(currentRound = currentRound) }

        viewModelScope.launch {
            val localCount = lottoRepository.getLocalCount()
            if (localCount == 0) {
                startInitialSync()
            }
        }
    }

    private fun observeAllResults() {
        viewModelScope.launch {
            getAllLottoResultsUseCase()
                .collect { results ->
                    _state.update {
                        it.copy(
                            lottoResults = results,
                            selectedResult = it.selectedResult ?: results.firstOrNull()
                        )
                    }
                }
        }
    }

    private fun observeUserTickets() {
        viewModelScope.launch {
            _state
                .flatMapLatest { state ->
                    getLottoTicketsUseCase(state.ticketSortType)
                }
                .collect { tickets ->
                    _state.update { it.copy(tickets = tickets) }
                }
        }
    }

    private fun startInitialSync() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        _state.update { it.copy(currentRound = currentRound, isSyncing = true) }

        viewModelScope.launch {
            syncLottoResultsUseCase(currentRound)
                .onSuccess {
                    _state.update { it.copy(isSyncing = false) }
                    _effect.send(LottoResultEffect.SyncCompleted)
                }
                .onFailure { e ->
                    val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                    _state.update { it.copy(isSyncing = false, error = errorMessage) }
                    _effect.send(LottoResultEffect.ShowError(errorMessage))
                }
        }
    }

    fun onEvent(event: LottoResultEvent) {
        when (event) {
            is LottoResultEvent.LoadResult -> loadResult(event.round)
            is LottoResultEvent.LoadLatestResult -> loadLatestResult()
            is LottoResultEvent.Refresh -> refresh()
            is LottoResultEvent.SelectResult -> selectResult(event.result)
            is LottoResultEvent.StartSync -> startSync()
            is LottoResultEvent.OpenQrScan -> openQrScan()
            is LottoResultEvent.OpenManualInput -> openManualInput()
            is LottoResultEvent.SaveQrTickets -> saveQrTickets(event.round, event.games, event.gameTypes)
            is LottoResultEvent.SaveManualTicket -> saveManualTicket(event.round, event.numbers, event.isAuto)
            is LottoResultEvent.DeleteTicket -> deleteTicket(event.ticketId)
            is LottoResultEvent.CheckWinning -> checkWinning(event.ticketId)
            is LottoResultEvent.ToggleBottomSheet -> toggleBottomSheet()
            is LottoResultEvent.ChangeSortType -> changeSortType(event.sortType)
        }
    }

    private fun loadLatestResult() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        _state.update { it.copy(currentRound = currentRound) }
        loadResult(currentRound)
    }

    private fun loadResult(round: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            getLottoResultUseCase(round)
                .onSuccess { result ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            selectedResult = result,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = errorMessage
                        )
                    }
                    _effect.send(LottoResultEffect.ShowError(errorMessage))
                }
        }
    }

    private fun refresh() {
        startSync()
    }

    private fun selectResult(result: LottoResult) {
        _state.update { it.copy(selectedResult = result) }
    }

    private fun startSync() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        _state.update { it.copy(currentRound = currentRound, isSyncing = true) }

        viewModelScope.launch {
            syncLottoResultsUseCase(currentRound)
                .onSuccess {
                    _state.update { it.copy(isSyncing = false) }
                    _effect.send(LottoResultEffect.SyncCompleted)
                }
                .onFailure { e ->
                    val errorMessage = e.message ?: "알 수 없는 오류가 발생했습니다."
                    _state.update { it.copy(isSyncing = false, error = errorMessage) }
                    _effect.send(LottoResultEffect.ShowError(errorMessage))
                }
        }
    }

    private fun openQrScan() {
        viewModelScope.launch {
            _effect.send(LottoResultEffect.NavigateToQrScan)
        }
    }

    private fun openManualInput() {
        viewModelScope.launch {
            _effect.send(LottoResultEffect.NavigateToManualInput)
        }
    }

    private fun saveQrTickets(round: Int, games: List<List<Int>>, gameTypes: List<Boolean>) {
        viewModelScope.launch {
            val gameLabels = listOf("A", "B", "C", "D", "E")
            val lottoGames = games.mapIndexed { index, numbers ->
                val isAuto = gameTypes.getOrNull(index) ?: true
                LottoGame(
                    gameLabel = gameLabels.getOrNull(index) ?: "A",
                    numbers = numbers.sorted(),
                    gameType = if (isAuto) GameType.AUTO else GameType.MANUAL,
                    winningRank = 0
                )
            }

            val ticket = LottoTicket(
                round = round,
                registeredDate = Date(),
                isChecked = false,
                games = lottoGames
            )

            saveLottoTicketUseCase(ticket)
                .onSuccess {
                    _effect.send(LottoResultEffect.ShowTicketSaved(games.size))
                }
                .onFailure { e ->
                    _effect.send(LottoResultEffect.ShowError(e.message ?: "티켓 저장 실패"))
                }
        }
    }

    private fun saveManualTicket(round: Int, numbers: List<Int>, isAuto: Boolean) {
        viewModelScope.launch {
            val game = LottoGame(
                gameLabel = "A",
                numbers = numbers.sorted(),
                gameType = if (isAuto) GameType.AUTO else GameType.MANUAL,
                winningRank = 0
            )

            val ticket = LottoTicket(
                round = round,
                registeredDate = Date(),
                isChecked = false,
                games = listOf(game)
            )

            saveLottoTicketUseCase(ticket)
                .onSuccess {
                    _effect.send(LottoResultEffect.ShowTicketSaved(1))
                }
                .onFailure { e ->
                    _effect.send(LottoResultEffect.ShowError(e.message ?: "티켓 저장 실패"))
                }
        }
    }

    private fun deleteTicket(ticketId: Long) {
        viewModelScope.launch {
            deleteLottoTicketUseCase(ticketId)
                .onFailure { e ->
                    _effect.send(LottoResultEffect.ShowError(e.message ?: "티켓 삭제 실패"))
                }
        }
    }

    private fun checkWinning(ticketId: Long) {
        viewModelScope.launch {
            val ticket = _state.value.tickets.find { it.ticketId == ticketId } ?: return@launch

            checkTicketWinningUseCase(ticket)
                .onSuccess {
                    // 티켓 내 게임들의 최고 당첨 등수 표시
                    val highestRank = ticket.games.minOfOrNull { it.winningRank } ?: 0
                    if (highestRank > 0) {
                        _effect.send(LottoResultEffect.ShowWinningResult(highestRank))
                    }
                }
                .onFailure { e ->
                    _effect.send(LottoResultEffect.ShowError(e.message ?: "당첨 확인 실패"))
                }
        }
    }

    private fun toggleBottomSheet() {
        _state.update { it.copy(isBottomSheetOpen = !it.isBottomSheetOpen) }
    }

    private fun changeSortType(sortType: TicketSortType) {
        _state.update { it.copy(ticketSortType = sortType) }
    }
}
