package com.enso.home

import com.enso.domain.model.LottoResult

data class LottoResultUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val lottoResults: List<LottoResult> = emptyList(),
    val selectedResult: LottoResult? = null,
    val error: String? = null,
    val currentRound: Int = 0
)

sealed class LottoResultEvent {
    data class LoadResult(val round: Int) : LottoResultEvent()
    data object LoadLatestResult : LottoResultEvent()
    data object Refresh : LottoResultEvent()
    data class SelectResult(val result: LottoResult) : LottoResultEvent()
    data object StartSync : LottoResultEvent()
}

sealed class LottoResultEffect {
    data class ShowError(val message: String) : LottoResultEffect()
    data object SyncCompleted : LottoResultEffect()
}
