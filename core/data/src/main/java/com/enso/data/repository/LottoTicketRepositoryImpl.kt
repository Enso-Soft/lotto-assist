package com.enso.data.repository

import com.enso.data.datasource.LottoTicketLocalDataSource
import com.enso.data.mapper.toGameEntity
import com.enso.data.mapper.toDomain
import com.enso.data.mapper.toTicketEntity
import com.enso.di.IoDispatcher
import com.enso.domain.exception.DuplicateQrException
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
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
            if (ticketId == -1L) {
                throw DuplicateQrException()
            }

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

    override suspend fun deleteTicketByQrUrl(qrUrl: String): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            // 먼저 해당 티켓을 조회하여 존재하는지 확인
            val existingTicket = localDataSource.getTicketByQrUrl(qrUrl)
            if (existingTicket != null) {
                // 티켓 삭제 (CASCADE로 연결된 게임들도 자동 삭제됨)
                localDataSource.deleteTicketByQrUrl(qrUrl)
            }
        }
    }

    override fun getAllTickets(sortType: TicketSortType): Flow<List<LottoTicket>> {
        return localDataSource.getAllTickets(sortType).map { ticketEntities ->
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
