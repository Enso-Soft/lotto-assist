package com.enso.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.enso.database.entity.LottoTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LottoTicketDao {
    @Insert
    suspend fun insert(ticket: LottoTicketEntity): Long

    @Query("DELETE FROM lotto_tickets WHERE ticketId = :ticketId")
    suspend fun delete(ticketId: Long)

    @Query("SELECT * FROM lotto_tickets ORDER BY round DESC, registeredDate DESC")
    fun getAllTickets(): Flow<List<LottoTicketEntity>>

    @Query("SELECT * FROM lotto_tickets WHERE round = :round ORDER BY registeredDate DESC")
    fun getTicketsByRound(round: Int): Flow<List<LottoTicketEntity>>

    @Query("SELECT * FROM lotto_tickets WHERE ticketId = :ticketId")
    suspend fun getTicketById(ticketId: Long): LottoTicketEntity?

    @Query("UPDATE lotto_tickets SET isChecked = :isChecked WHERE ticketId = :ticketId")
    suspend fun updateCheckedStatus(ticketId: Long, isChecked: Boolean)

    @Query("SELECT COUNT(*) FROM lotto_tickets")
    suspend fun getCount(): Int
}
