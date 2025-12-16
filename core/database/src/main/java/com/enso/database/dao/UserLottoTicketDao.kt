package com.enso.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.enso.database.entity.UserLottoTicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserLottoTicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ticket: UserLottoTicketEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tickets: List<UserLottoTicketEntity>)

    @Update
    suspend fun update(ticket: UserLottoTicketEntity)

    @Delete
    suspend fun delete(ticket: UserLottoTicketEntity)

    @Query("SELECT * FROM user_lotto_tickets ORDER BY round DESC, registeredDate DESC")
    fun getAllTickets(): Flow<List<UserLottoTicketEntity>>

    @Query("SELECT * FROM user_lotto_tickets WHERE round = :round ORDER BY registeredDate DESC")
    fun getTicketsByRound(round: Int): Flow<List<UserLottoTicketEntity>>

    @Query("SELECT * FROM user_lotto_tickets WHERE id = :id")
    suspend fun getTicketById(id: Long): UserLottoTicketEntity?

    @Query("DELETE FROM user_lotto_tickets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM user_lotto_tickets")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM user_lotto_tickets WHERE round = :round")
    suspend fun getCountByRound(round: Int): Int
}
