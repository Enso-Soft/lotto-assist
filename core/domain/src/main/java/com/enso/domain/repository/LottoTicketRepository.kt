package com.enso.domain.repository

import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
import kotlinx.coroutines.flow.Flow

interface LottoTicketRepository {
    suspend fun saveTicket(ticket: LottoTicket): Result<Long>
    suspend fun deleteTicket(ticketId: Long): Result<Unit>
    fun getAllTickets(sortType: TicketSortType = TicketSortType.DEFAULT): Flow<List<LottoTicket>>
    fun getTicketsByRound(round: Int): Flow<List<LottoTicket>>
    suspend fun updateGameWinningRank(gameId: Long, rank: Int): Result<Unit>
    suspend fun updateTicketCheckedStatus(ticketId: Long, isChecked: Boolean): Result<Unit>
}
