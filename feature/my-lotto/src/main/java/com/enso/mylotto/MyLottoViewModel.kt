package com.enso.mylotto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.usecase.CheckTicketWinningUseCase
import com.enso.domain.usecase.DeleteLottoTicketUseCase
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLottoTicketsUseCase
import com.enso.util.lotto_date.LottoDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MyLottoViewModel @Inject constructor(
    private val getLottoTicketsUseCase: GetLottoTicketsUseCase,
    private val deleteLottoTicketUseCase: DeleteLottoTicketUseCase,
    private val checkTicketWinningUseCase: CheckTicketWinningUseCase,
    private val getAllLottoResultsUseCase: GetAllLottoResultsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MyLottoUiState())
    val state: StateFlow<MyLottoUiState> = _state.asStateFlow()

    private val _effect = Channel<MyLottoEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        initializeData()
        observeTickets()
        observeLottoResults()
    }

    private fun initializeData() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        _state.update { it.copy(currentRound = currentRound) }
    }

    private fun observeTickets() {
        viewModelScope.launch {
            _state
                .map { it.sortType }
                .distinctUntilChanged()
                .flatMapLatest { sortType ->
                    getLottoTicketsUseCase(sortType)
                }
                .catch { e ->
                    val errorMessage = "티켓 로드 실패: ${e.message}"
                    _state.update { it.copy(error = errorMessage, isLoading = false) }
                    _effect.send(MyLottoEffect.ShowError(errorMessage))
                }
                .collect { tickets ->
                    _state.update { it.copy(tickets = tickets, isLoading = false, isRefreshing = false) }
                }
        }
    }

    private fun observeLottoResults() {
        viewModelScope.launch {
            getAllLottoResultsUseCase()
                .catch { e ->
                    val errorMessage = "결과 로드 실패: ${e.message}"
                    _state.update { it.copy(error = errorMessage) }
                    _effect.send(MyLottoEffect.ShowError(errorMessage))
                }
                .collect { results ->
                    _state.update { it.copy(lottoResults = results) }
                }
        }
    }

    fun onEvent(event: MyLottoEvent) {
        when (event) {
            is MyLottoEvent.Refresh -> refresh()
            is MyLottoEvent.ChangeSortType -> changeSortType(event.sortType)
            is MyLottoEvent.DeleteTicket -> deleteTicket(event.ticketId)
            is MyLottoEvent.CheckWinning -> checkWinning(event.ticketId)
            is MyLottoEvent.ClearError -> clearError()
        }
    }

    private fun refresh() {
        _state.update { it.copy(isRefreshing = true) }

        viewModelScope.launch {
            try {
                // Re-check all unchecked tickets
                val uncheckedTickets = _state.value.tickets.filter { !it.isChecked }
                uncheckedTickets.forEach { ticket ->
                    if (ticket.round <= _state.value.currentRound) {
                        // await each check to ensure proper sequencing
                        checkTicketWinningUseCase(ticket)
                    }
                }
                _effect.send(MyLottoEffect.ShowSnackbar("새로고침 완료"))
            } catch (e: Exception) {
                _effect.send(MyLottoEffect.ShowError("새로고침 실패: ${e.message}"))
            } finally {
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun changeSortType(sortType: com.enso.domain.model.TicketSortType) {
        _state.update { it.copy(sortType = sortType) }
    }

    private fun deleteTicket(ticketId: Long) {
        viewModelScope.launch {
            deleteLottoTicketUseCase(ticketId)
                .onSuccess {
                    _effect.send(MyLottoEffect.TicketDeleted(ticketId))
                    _effect.send(MyLottoEffect.ShowSnackbar("티켓이 삭제되었습니다"))
                }
                .onFailure { e ->
                    _effect.send(MyLottoEffect.ShowError(e.message ?: "티켓 삭제 실패"))
                }
        }
    }

    private fun checkWinning(ticketId: Long) {
        viewModelScope.launch {
            val ticket = _state.value.tickets.find { it.ticketId == ticketId } ?: return@launch

            checkTicketWinningUseCase(ticket)
                .onSuccess {
                    // 티켓 내 게임들의 최고 당첨 등수 확인
                    val highestRank = ticket.games.minOfOrNull { it.winningRank } ?: 0
                    if (highestRank > 0) {
                        _effect.send(MyLottoEffect.ShowSnackbar("${highestRank}등 당첨!"))
                    } else {
                        _effect.send(MyLottoEffect.ShowSnackbar("당첨 확인 완료"))
                    }
                }
                .onFailure { e ->
                    _effect.send(MyLottoEffect.ShowError(e.message ?: "당첨 확인 실패"))
                }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
