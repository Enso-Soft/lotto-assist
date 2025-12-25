package com.enso.home

import com.enso.domain.model.LottoResult
import com.enso.domain.model.WinningStatistics

data class LottoResultUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val lottoResults: List<LottoResult> = emptyList(),
    val selectedResult: LottoResult? = null,
    val error: String? = null,
    val currentRound: Int = 0,
    val upcomingRound: Int = 0,  // 미추첨 회차 (티켓 등록용)
    val winningStatistics: WinningStatistics = WinningStatistics.EMPTY
)

sealed class LottoResultEvent {
    data class LoadResult(val round: Int) : LottoResultEvent()
    data object LoadLatestResult : LottoResultEvent()
    data object Refresh : LottoResultEvent()
    data class SelectResult(val result: LottoResult) : LottoResultEvent()
    data object StartSync : LottoResultEvent()
    data object OpenQrScan : LottoResultEvent()
    data object OpenManualInput : LottoResultEvent()
    data class SaveQrTickets(val round: Int, val games: List<List<Int>>, val gameTypes: List<Boolean>) : LottoResultEvent()
    data class SaveManualTicket(val round: Int, val numbers: List<Int>, val isAuto: Boolean) : LottoResultEvent()
}

sealed class LottoResultEffect {
    data class ShowError(val message: String) : LottoResultEffect()
    data object SyncCompleted : LottoResultEffect()
    data class PartialSyncCompleted(val successCount: Int, val failedCount: Int) : LottoResultEffect()
    data object NavigateToQrScan : LottoResultEffect()
    data object NavigateToManualInput : LottoResultEffect()
    data class ShowTicketSaved(val count: Int) : LottoResultEffect()
}
