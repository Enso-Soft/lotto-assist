package com.enso.domain.usecase

import com.enso.domain.model.UserLottoTicket
import com.enso.domain.repository.UserTicketRepository
import javax.inject.Inject

class UpdateUserTicketUseCase @Inject constructor(
    private val userTicketRepository: UserTicketRepository
) {
    suspend operator fun invoke(ticket: UserLottoTicket): Result<Unit> {
        return userTicketRepository.updateTicket(ticket)
    }
}
