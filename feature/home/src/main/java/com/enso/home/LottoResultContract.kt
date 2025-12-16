package com.enso.home

import com.enso.domain.model.LottoResult

data class LottoResultUiState(
    val isLoading: Boolean = false,
    val lottoResult: LottoResult? = null,
    val error: String? = null,
    val currentRound: Int = 0
)

sealed class LottoResultEvent {
    data class LoadResult(val round: Int) : LottoResultEvent()
    object LoadLatestResult : LottoResultEvent()
    object Refresh : LottoResultEvent()
}

sealed class LottoResultEffect {
    data class ShowError(val message: String) : LottoResultEffect()
}
