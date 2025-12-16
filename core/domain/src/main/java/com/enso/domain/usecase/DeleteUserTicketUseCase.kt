package com.enso.domain.usecase

import com.enso.domain.repository.UserTicketRepository
import javax.inject.Inject

class DeleteUserTicketUseCase @Inject constructor(
    private val userTicketRepository: UserTicketRepository
) {
    suspend operator fun invoke(ticketId: Long): Result<Unit> {
        return userTicketRepository.deleteTicket(ticketId)
    }
}
