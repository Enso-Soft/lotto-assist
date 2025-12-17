package com.enso.domain.usecase

import com.enso.domain.repository.LottoTicketRepository
import javax.inject.Inject

class DeleteLottoTicketUseCase @Inject constructor(
    private val repository: LottoTicketRepository
) {
    suspend operator fun invoke(ticketId: Long): Result<Unit> {
        return repository.deleteTicket(ticketId)
    }
}
