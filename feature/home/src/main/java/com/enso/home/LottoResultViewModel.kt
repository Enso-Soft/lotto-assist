package com.enso.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoResult
import com.enso.domain.model.UserLottoTicket
import com.enso.domain.usecase.CheckWinningUseCase
import com.enso.domain.usecase.DeleteUserTicketUseCase
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLottoResultUseCase
import com.enso.domain.usecase.GetUserTicketsUseCase
import com.enso.domain.usecase.SaveUserTicketUseCase
import com.enso.domain.usecase.SyncLottoResultsUseCase
import com.enso.domain.usecase.UpdateUserTicketUseCase
import com.enso.util.lotto_date.LottoDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getUserTicketsUseCase: GetUserTicketsUseCase,
    private val saveUserTicketUseCase: SaveUserTicketUseCase,
    private val updateUserTicketUseCase: UpdateUserTicketUseCase,
    private val deleteUserTicketUseCase: DeleteUserTicketUseCase,
    private val checkWinningUseCase: CheckWinningUseCase,
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
            getUserTicketsUseCase()
                .collect { tickets ->
                    _state.update { it.copy(userTickets = tickets) }
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
            is LottoResultEvent.SaveQrTickets -> saveQrTickets(event.round, event.games)
            is LottoResultEvent.SaveManualTicket -> saveManualTicket(event.round, event.numbers, event.isAuto)
            is LottoResultEvent.DeleteTicket -> deleteTicket(event.ticketId)
            is LottoResultEvent.CheckWinning -> checkWinning(event.ticketId)
            is LottoResultEvent.ToggleBottomSheet -> toggleBottomSheet()
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

    private fun saveQrTickets(round: Int, games: List<List<Int>>) {
        viewModelScope.launch {
            val tickets = games.mapIndexed { index, numbers ->
                UserLottoTicket(
                    round = round,
                    numbers = numbers.sorted(),
                    gameType = GameType.AUTO,
                    registeredDate = Date()
                )
            }

            saveUserTicketUseCase.saveMultiple(tickets)
                .onSuccess {
                    _effect.send(LottoResultEffect.ShowTicketSaved(tickets.size))
                }
                .onFailure { e ->
                    _effect.send(LottoResultEffect.ShowError(e.message ?: "티켓 저장 실패"))
                }
        }
    }

    private fun saveManualTicket(round: Int, numbers: List<Int>, isAuto: Boolean) {
        viewModelScope.launch {
            val ticket = UserLottoTicket(
                round = round,
                numbers = numbers.sorted(),
                gameType = if (isAuto) GameType.AUTO else GameType.MANUAL,
                registeredDate = Date()
            )

            saveUserTicketUseCase(ticket)
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
            deleteUserTicketUseCase(ticketId)
                .onFailure { e ->
                    _effect.send(LottoResultEffect.ShowError(e.message ?: "티켓 삭제 실패"))
                }
        }
    }

    private fun checkWinning(ticketId: Long) {
        viewModelScope.launch {
            val ticket = _state.value.userTickets.find { it.id == ticketId } ?: return@launch

            checkWinningUseCase(ticket.round, ticket.numbers)
                .onSuccess { result ->
                    val updatedTicket = ticket.copy(
                        winningRank = result.rank,
                        isChecked = true
                    )
                    updateUserTicketUseCase(updatedTicket)
                    _effect.send(LottoResultEffect.ShowWinningResult(result.rank))
                }
                .onFailure { e ->
                    _effect.send(LottoResultEffect.ShowError(e.message ?: "당첨 확인 실패"))
                }
        }
    }

    private fun toggleBottomSheet() {
        _state.update { it.copy(isBottomSheetOpen = !it.isBottomSheetOpen) }
    }
}
