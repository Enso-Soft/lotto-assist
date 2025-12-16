package com.enso.data.datasource

import com.enso.database.dao.UserLottoTicketDao
import com.enso.database.entity.UserLottoTicketEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserTicketLocalDataSource @Inject constructor(
    private val userLottoTicketDao: UserLottoTicketDao
) {
    suspend fun insert(ticket: UserLottoTicketEntity): Long {
        return userLottoTicketDao.insert(ticket)
    }

    suspend fun insertAll(tickets: List<UserLottoTicketEntity>) {
        userLottoTicketDao.insertAll(tickets)
    }

    suspend fun update(ticket: UserLottoTicketEntity) {
        userLottoTicketDao.update(ticket)
    }

    suspend fun delete(ticketId: Long) {
        userLottoTicketDao.deleteById(ticketId)
    }

    fun getAllTickets(): Flow<List<UserLottoTicketEntity>> {
        return userLottoTicketDao.getAllTickets()
    }

    fun getTicketsByRound(round: Int): Flow<List<UserLottoTicketEntity>> {
        return userLottoTicketDao.getTicketsByRound(round)
    }

    suspend fun getTicketById(id: Long): UserLottoTicketEntity? {
        return userLottoTicketDao.getTicketById(id)
    }

    suspend fun getCount(): Int {
        return userLottoTicketDao.getCount()
    }

    suspend fun getCountByRound(round: Int): Int {
        return userLottoTicketDao.getCountByRound(round)
    }
}
