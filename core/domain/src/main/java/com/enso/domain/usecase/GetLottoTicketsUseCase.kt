package com.enso.domain.usecase

import com.enso.domain.model.LottoTicket
import com.enso.domain.repository.LottoTicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLottoTicketsUseCase @Inject constructor(
    private val repository: LottoTicketRepository
) {
    operator fun invoke(): Flow<List<LottoTicket>> {
        return repository.getAllTickets()
    }

    fun getByRound(round: Int): Flow<List<LottoTicket>> {
        return repository.getTicketsByRound(round)
    }
}
