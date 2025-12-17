package com.enso.home

import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType

data class LottoResultUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val lottoResults: List<LottoResult> = emptyList(),
    val selectedResult: LottoResult? = null,
    val tickets: List<LottoTicket> = emptyList(),
    val error: String? = null,
    val currentRound: Int = 0,
    val isBottomSheetOpen: Boolean = false,
    val ticketSortType: TicketSortType = TicketSortType.DEFAULT
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
    data class DeleteTicket(val ticketId: Long) : LottoResultEvent()
    data class CheckWinning(val ticketId: Long) : LottoResultEvent()
    data object ToggleBottomSheet : LottoResultEvent()
    data class ChangeSortType(val sortType: TicketSortType) : LottoResultEvent()
}

sealed class LottoResultEffect {
    data class ShowError(val message: String) : LottoResultEffect()
    data object SyncCompleted : LottoResultEffect()
    data object NavigateToQrScan : LottoResultEffect()
    data object NavigateToManualInput : LottoResultEffect()
    data class ShowTicketSaved(val count: Int) : LottoResultEffect()
    data class ShowWinningResult(val rank: Int) : LottoResultEffect()
}
