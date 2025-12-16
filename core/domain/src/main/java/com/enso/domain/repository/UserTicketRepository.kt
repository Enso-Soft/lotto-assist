package com.enso.domain.repository

import com.enso.domain.model.UserLottoTicket
import kotlinx.coroutines.flow.Flow

interface UserTicketRepository {
    suspend fun saveTicket(ticket: UserLottoTicket): Result<Long>
    suspend fun saveTickets(tickets: List<UserLottoTicket>): Result<Unit>
    suspend fun updateTicket(ticket: UserLottoTicket): Result<Unit>
    suspend fun deleteTicket(ticketId: Long): Result<Unit>
    fun getAllTickets(): Flow<List<UserLottoTicket>>
    fun getTicketsByRound(round: Int): Flow<List<UserLottoTicket>>
    suspend fun getTicketById(id: Long): UserLottoTicket?
    suspend fun getTicketCount(): Int
    suspend fun getTicketCountByRound(round: Int): Int
}
