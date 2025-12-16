package com.enso.data.repository

import com.enso.data.datasource.UserTicketLocalDataSource
import com.enso.data.mapper.toDomain
import com.enso.data.mapper.toEntity
import com.enso.di.IoDispatcher
import com.enso.domain.model.UserLottoTicket
import com.enso.domain.repository.UserTicketRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserTicketRepositoryImpl @Inject constructor(
    private val localDataSource: UserTicketLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserTicketRepository {

    override suspend fun saveTicket(ticket: UserLottoTicket): Result<Long> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.insert(ticket.toEntity())
        }
    }

    override suspend fun saveTickets(tickets: List<UserLottoTicket>): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.insertAll(tickets.map { it.toEntity() })
        }
    }

    override suspend fun updateTicket(ticket: UserLottoTicket): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.update(ticket.toEntity())
        }
    }

    override suspend fun deleteTicket(ticketId: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.delete(ticketId)
        }
    }

    override fun getAllTickets(): Flow<List<UserLottoTicket>> {
        return localDataSource.getAllTickets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTicketsByRound(round: Int): Flow<List<UserLottoTicket>> {
        return localDataSource.getTicketsByRound(round).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTicketById(id: Long): UserLottoTicket? = withContext(ioDispatcher) {
        localDataSource.getTicketById(id)?.toDomain()
    }

    override suspend fun getTicketCount(): Int = withContext(ioDispatcher) {
        localDataSource.getCount()
    }

    override suspend fun getTicketCountByRound(round: Int): Int = withContext(ioDispatcher) {
        localDataSource.getCountByRound(round)
    }
}
