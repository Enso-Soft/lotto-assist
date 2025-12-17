package com.enso.data.repository

import com.enso.data.datasource.LottoTicketLocalDataSource
import com.enso.data.mapper.toGameEntity
import com.enso.data.mapper.toDomain
import com.enso.data.mapper.toTicketEntity
import com.enso.di.IoDispatcher
import com.enso.domain.model.LottoTicket
import com.enso.domain.repository.LottoTicketRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LottoTicketRepositoryImpl @Inject constructor(
    private val localDataSource: LottoTicketLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LottoTicketRepository {

    override suspend fun saveTicket(ticket: LottoTicket): Result<Long> = withContext(ioDispatcher) {
        runCatching {
            // 1. 티켓 먼저 저장 -> ticketId 획득
            val ticketId = localDataSource.insertTicket(ticket.toTicketEntity())

            // 2. 게임들 저장 (ticketId 사용)
            val gameEntities = ticket.games.map { it.toGameEntity(ticketId) }
            localDataSource.insertGames(gameEntities)

            ticketId
        }
    }

    override suspend fun deleteTicket(ticketId: Long): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.deleteTicket(ticketId)
        }
    }

    override fun getAllTickets(): Flow<List<LottoTicket>> {
        return localDataSource.getAllTickets().map { ticketEntities ->
            ticketEntities.map { ticketEntity ->
                val games = localDataSource.getGamesByTicketId(ticketEntity.ticketId)
                ticketEntity.toDomain(games)
            }
        }
    }

    override fun getTicketsByRound(round: Int): Flow<List<LottoTicket>> {
        return localDataSource.getTicketsByRound(round).map { ticketEntities ->
            ticketEntities.map { ticketEntity ->
                val games = localDataSource.getGamesByTicketId(ticketEntity.ticketId)
                ticketEntity.toDomain(games)
            }
        }
    }

    override suspend fun updateGameWinningRank(gameId: Long, rank: Int): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.updateGameWinningRank(gameId, rank)
        }
    }

    override suspend fun updateTicketCheckedStatus(ticketId: Long, isChecked: Boolean): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.updateTicketCheckedStatus(ticketId, isChecked)
        }
    }
}
