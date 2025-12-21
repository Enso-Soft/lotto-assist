package com.enso.mylotto

import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType

data class MyLottoUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val tickets: List<LottoTicket> = emptyList(),
    val lottoResults: List<LottoResult> = emptyList(),
    val currentRound: Int = 0,
    val sortType: TicketSortType = TicketSortType.DEFAULT,
    val error: String? = null
)

sealed class MyLottoEvent {
    data object Refresh : MyLottoEvent()
    data class ChangeSortType(val sortType: TicketSortType) : MyLottoEvent()
    data class DeleteTicket(val ticketId: Long) : MyLottoEvent()
    data class CheckWinning(val ticketId: Long) : MyLottoEvent()
    data object ClearError : MyLottoEvent()
}

sealed class MyLottoEffect {
    data class ShowError(val message: String) : MyLottoEffect()
    data class ShowSnackbar(val message: String) : MyLottoEffect()
    data class TicketDeleted(val ticketId: Long) : MyLottoEffect()
}
