package com.enso.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.enso.database.entity.LottoTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LottoTicketDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(ticket: LottoTicketEntity): Long

    @Query("DELETE FROM lotto_tickets WHERE ticketId = :ticketId")
    suspend fun delete(ticketId: Long)

    @Query("DELETE FROM lotto_tickets WHERE qrUrl = :qrUrl")
    suspend fun deleteByQrUrl(qrUrl: String)

    @Query("SELECT * FROM lotto_tickets WHERE qrUrl = :qrUrl LIMIT 1")
    suspend fun getTicketByQrUrl(qrUrl: String): LottoTicketEntity?

    @Query("SELECT * FROM lotto_tickets ORDER BY registeredDate DESC")
    fun getAllTicketsByRegisteredDateDesc(): Flow<List<LottoTicketEntity>>

    @Query("SELECT * FROM lotto_tickets ORDER BY registeredDate ASC")
    fun getAllTicketsByRegisteredDateAsc(): Flow<List<LottoTicketEntity>>

    @Query("SELECT * FROM lotto_tickets ORDER BY round DESC, registeredDate DESC")
    fun getAllTicketsByRoundDesc(): Flow<List<LottoTicketEntity>>

    @Query("SELECT * FROM lotto_tickets ORDER BY round ASC, registeredDate DESC")
    fun getAllTicketsByRoundAsc(): Flow<List<LottoTicketEntity>>

    @Query("SELECT * FROM lotto_tickets WHERE round = :round ORDER BY registeredDate DESC")
    fun getTicketsByRound(round: Int): Flow<List<LottoTicketEntity>>

    @Query("SELECT * FROM lotto_tickets WHERE ticketId = :ticketId")
    suspend fun getTicketById(ticketId: Long): LottoTicketEntity?

    @Query("UPDATE lotto_tickets SET isChecked = :isChecked WHERE ticketId = :ticketId")
    suspend fun updateCheckedStatus(ticketId: Long, isChecked: Boolean)

    @Query("SELECT COUNT(*) FROM lotto_tickets")
    suspend fun getCount(): Int
}
