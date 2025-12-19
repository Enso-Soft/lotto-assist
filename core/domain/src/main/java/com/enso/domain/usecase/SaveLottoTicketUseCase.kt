package com.enso.domain.usecase

import com.enso.domain.model.LottoTicket
import com.enso.domain.repository.LottoTicketRepository
import javax.inject.Inject

class SaveLottoTicketUseCase @Inject constructor(
    private val repository: LottoTicketRepository
) {
    suspend operator fun invoke(ticket: LottoTicket): Result<Long> {
        return repository.saveTicket(ticket)
    }

    suspend fun deleteTicketByQrUrl(qrUrl: String): Result<Unit> {
        return repository.deleteTicketByQrUrl(qrUrl)
    }
}
