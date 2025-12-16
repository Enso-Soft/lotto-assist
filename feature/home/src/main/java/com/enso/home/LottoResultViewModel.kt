package com.enso.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enso.domain.model.LottoResult
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLottoResultUseCase
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
import javax.inject.Inject

@HiltViewModel
class LottoResultViewModel @Inject constructor(
    private val getLottoResultUseCase: GetLottoResultUseCase,
    private val getAllLottoResultsUseCase: GetAllLottoResultsUseCase,
    private val syncLottoResultsUseCase: SyncLottoResultsUseCase,
    private val lottoRepository: com.enso.domain.repository.LottoRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LottoResultUiState())
    val state: StateFlow<LottoResultUiState> = _state.asStateFlow()

    private val _effect = Channel<LottoResultEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        observeAllResults()
        checkAndLoadInitialData()
    }

    private fun checkAndLoadInitialData() {
        val currentRound = LottoDate.getCurrentDrawNumber()
        _state.update { it.copy(currentRound = currentRound) }

        viewModelScope.launch {
            val localCount = lottoRepository.getLocalCount()
            if (localCount == 0) {
                // ROOM에 데이터가 없으면 API 호출
                startInitialSync()
            }
            // 데이터가 있으면 observeAllResults()에서 자동으로 표시됨
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
}
