package com.enso.domain.usecase

import com.enso.domain.model.UserLottoTicket
import com.enso.domain.repository.UserTicketRepository
import javax.inject.Inject

class SaveUserTicketUseCase @Inject constructor(
    private val userTicketRepository: UserTicketRepository
) {
    suspend operator fun invoke(ticket: UserLottoTicket): Result<Long> {
        return userTicketRepository.saveTicket(ticket)
    }

    suspend fun saveMultiple(tickets: List<UserLottoTicket>): Result<Unit> {
        return userTicketRepository.saveTickets(tickets)
    }
}
