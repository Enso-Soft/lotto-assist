package com.enso.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLocalLottoResultCountUseCase
import com.enso.domain.usecase.GetLottoResultUseCase
import com.enso.domain.usecase.GetWinningStatisticsUseCase
import com.enso.domain.usecase.SaveLottoTicketUseCase
import com.enso.domain.usecase.SyncLottoResultsUseCase
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
    private val saveLottoTicketUseCase: SaveLottoTicketUseCase,
    private val getLocalLottoResultCountUseCase: GetLocalLottoResultCountUseCase,
    private val getWinningStatisticsUseCase: GetWinningStatisticsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LottoResultUiState())
    val state: StateFlow<LottoResultUiState> = _state.asStateFlow()

    private val _effect = Channel<LottoResultEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeAllResults()
        observeWinningStatistics()
        checkAndLoadInitialData()
    }

    private fun checkAndLoadInitialData() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        val upcomingRound = LottoDate.getUpcomingDrawRound()
        _state.update { it.copy(currentRound = currentRound, upcomingRound = upcomingRound) }

        viewModelScope.launch {
            try {
                val localCount = getLocalLottoResultCountUseCase()
                if (localCount == 0) {
                    startInitialSync()
                }
            } catch (e: Exception) {
                _effect.send(LottoResultEffect.ShowError("데이터 확인 실패: ${e.message}"))
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

    private fun observeWinningStatistics() {
        viewModelScope.launch {
            getWinningStatisticsUseCase()
                .collect { statistics ->
                    _state.update { it.copy(winningStatistics = statistics) }
                }
        }
    }

    private fun startInitialSync() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        val upcomingRound = LottoDate.getUpcomingDrawRound()
        _state.update { it.copy(currentRound = currentRound, upcomingRound = upcomingRound, isSyncing = true) }

        viewModelScope.launch {
            syncLottoResultsUseCase(currentRound)
                .onSuccess { syncResult ->
                    _state.update { it.copy(isSyncing = false) }
                    when {
                        syncResult.isFullSuccess || syncResult.totalCount == 0 -> {
                            _effect.send(LottoResultEffect.SyncCompleted)
                        }
                        syncResult.isPartialSuccess -> {
                            _effect.send(LottoResultEffect.PartialSyncCompleted(
                                successCount = syncResult.successCount,
                                failedCount = syncResult.failedCount
                            ))
                        }
                        syncResult.isFullFailure -> {
                            _effect.send(LottoResultEffect.ShowError("동기화 실패: 모든 회차를 가져오지 못했습니다"))
                        }
                    }
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
        }
    }

    private fun loadLatestResult() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        val upcomingRound = LottoDate.getUpcomingDrawRound()
        _state.update { it.copy(currentRound = currentRound, upcomingRound = upcomingRound) }
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
        val upcomingRound = LottoDate.getUpcomingDrawRound()
        _state.update { it.copy(currentRound = currentRound, upcomingRound = upcomingRound, isSyncing = true) }

        viewModelScope.launch {
            syncLottoResultsUseCase(currentRound)
                .onSuccess { syncResult ->
                    _state.update { it.copy(isSyncing = false) }
                    when {
                        syncResult.isFullSuccess || syncResult.totalCount == 0 -> {
                            _effect.send(LottoResultEffect.SyncCompleted)
                        }
                        syncResult.isPartialSuccess -> {
                            _effect.send(LottoResultEffect.PartialSyncCompleted(
                                successCount = syncResult.successCount,
                                failedCount = syncResult.failedCount
                            ))
                        }
                        syncResult.isFullFailure -> {
                            _effect.send(LottoResultEffect.ShowError("동기화 실패: 모든 회차를 가져오지 못했습니다"))
                        }
                    }
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

}
