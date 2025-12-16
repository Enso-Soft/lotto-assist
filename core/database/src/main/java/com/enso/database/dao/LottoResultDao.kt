package com.enso.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.enso.database.entity.LottoResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LottoResultDao {
    @Query("SELECT * FROM lotto_results ORDER BY round DESC")
    fun getAllResults(): Flow<List<LottoResultEntity>>

    @Query("SELECT * FROM lotto_results WHERE round = :round")
    suspend fun getResultByRound(round: Int): LottoResultEntity?

    @Query("SELECT COUNT(*) FROM lotto_results")
    suspend fun getCount(): Int

    @Query("SELECT MAX(round) FROM lotto_results")
    suspend fun getLatestRound(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: LottoResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<LottoResultEntity>)

    @Query("SELECT * FROM lotto_results WHERE round IN (:rounds)")
    suspend fun getResultsByRounds(rounds: List<Int>): List<LottoResultEntity>
}
