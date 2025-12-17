package com.enso.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.enso.database.entity.LottoGameEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LottoGameDao {
    @Insert
    suspend fun insertAll(games: List<LottoGameEntity>)

    @Query("SELECT * FROM lotto_games WHERE ticketId = :ticketId ORDER BY gameLabel ASC")
    suspend fun getGamesByTicketId(ticketId: Long): List<LottoGameEntity>

    @Query("UPDATE lotto_games SET winningRank = :rank WHERE gameId = :gameId")
    suspend fun updateWinningRank(gameId: Long, rank: Int)

    @Query("SELECT * FROM lotto_games ORDER BY gameId DESC")
    fun getAllGames(): Flow<List<LottoGameEntity>>
}
