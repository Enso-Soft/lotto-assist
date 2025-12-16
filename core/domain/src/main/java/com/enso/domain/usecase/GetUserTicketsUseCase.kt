package com.enso.domain.usecase

import com.enso.domain.model.UserLottoTicket
import com.enso.domain.repository.UserTicketRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserTicketsUseCase @Inject constructor(
    private val userTicketRepository: UserTicketRepository
) {
    operator fun invoke(): Flow<List<UserLottoTicket>> {
        return userTicketRepository.getAllTickets()
    }

    fun getByRound(round: Int): Flow<List<UserLottoTicket>> {
        return userTicketRepository.getTicketsByRound(round)
    }
}
