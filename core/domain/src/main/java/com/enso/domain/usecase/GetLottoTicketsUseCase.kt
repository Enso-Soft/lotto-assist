package com.enso.domain.usecase

import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
import com.enso.domain.repository.LottoTicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLottoTicketsUseCase @Inject constructor(
    private val repository: LottoTicketRepository
) {
    operator fun invoke(sortType: TicketSortType = TicketSortType.DEFAULT): Flow<List<LottoTicket>> {
        return repository.getAllTickets(sortType)
    }

    fun getByRound(round: Int): Flow<List<LottoTicket>> {
        return repository.getTicketsByRound(round)
    }
}
