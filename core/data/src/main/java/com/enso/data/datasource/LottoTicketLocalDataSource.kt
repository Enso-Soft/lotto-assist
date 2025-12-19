package com.enso.data.datasource

import com.enso.database.dao.LottoGameDao
import com.enso.database.dao.LottoTicketDao
import com.enso.database.entity.LottoGameEntity
import com.enso.database.entity.LottoTicketEntity
import com.enso.domain.model.TicketSortType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface LottoTicketLocalDataSource {
    suspend fun insertTicket(ticket: LottoTicketEntity): Long
    suspend fun insertGames(games: List<LottoGameEntity>)
    suspend fun deleteTicket(ticketId: Long)
    suspend fun deleteTicketByQrUrl(qrUrl: String)
    suspend fun getTicketByQrUrl(qrUrl: String): LottoTicketEntity?
    fun getAllTickets(sortType: TicketSortType = TicketSortType.DEFAULT): Flow<List<LottoTicketEntity>>
    fun getTicketsByRound(round: Int): Flow<List<LottoTicketEntity>>
    suspend fun getTicketById(ticketId: Long): LottoTicketEntity?
    suspend fun getGamesByTicketId(ticketId: Long): List<LottoGameEntity>
    suspend fun updateGameWinningRank(gameId: Long, rank: Int)
    suspend fun updateTicketCheckedStatus(ticketId: Long, isChecked: Boolean)
}

class LottoTicketLocalDataSourceImpl @Inject constructor(
    private val ticketDao: LottoTicketDao,
    private val gameDao: LottoGameDao
) : LottoTicketLocalDataSource {
    override suspend fun insertTicket(ticket: LottoTicketEntity): Long {
        return ticketDao.insert(ticket)
    }

    override suspend fun insertGames(games: List<LottoGameEntity>) {
        gameDao.insertAll(games)
    }

    override suspend fun deleteTicket(ticketId: Long) {
        ticketDao.delete(ticketId)
    }

    override suspend fun deleteTicketByQrUrl(qrUrl: String) {
        ticketDao.deleteByQrUrl(qrUrl)
    }

    override suspend fun getTicketByQrUrl(qrUrl: String): LottoTicketEntity? {
        return ticketDao.getTicketByQrUrl(qrUrl)
    }

    override fun getAllTickets(sortType: TicketSortType): Flow<List<LottoTicketEntity>> {
        return when (sortType) {
            TicketSortType.REGISTERED_DATE_DESC -> ticketDao.getAllTicketsByRegisteredDateDesc()
            TicketSortType.REGISTERED_DATE_ASC -> ticketDao.getAllTicketsByRegisteredDateAsc()
            TicketSortType.ROUND_DESC -> ticketDao.getAllTicketsByRoundDesc()
            TicketSortType.ROUND_ASC -> ticketDao.getAllTicketsByRoundAsc()
        }
    }

    override fun getTicketsByRound(round: Int): Flow<List<LottoTicketEntity>> {
        return ticketDao.getTicketsByRound(round)
    }

    override suspend fun getTicketById(ticketId: Long): LottoTicketEntity? {
        return ticketDao.getTicketById(ticketId)
    }

    override suspend fun getGamesByTicketId(ticketId: Long): List<LottoGameEntity> {
        return gameDao.getGamesByTicketId(ticketId)
    }

    override suspend fun updateGameWinningRank(gameId: Long, rank: Int) {
        gameDao.updateWinningRank(gameId, rank)
    }

    override suspend fun updateTicketCheckedStatus(ticketId: Long, isChecked: Boolean) {
        ticketDao.updateCheckedStatus(ticketId, isChecked)
    }
}
