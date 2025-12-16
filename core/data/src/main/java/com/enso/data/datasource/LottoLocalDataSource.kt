package com.enso.data.datasource

import com.enso.database.entity.LottoResultEntity
import kotlinx.coroutines.flow.Flow

interface LottoLocalDataSource {
    fun getAllResults(): Flow<List<LottoResultEntity>>
    suspend fun getResultByRound(round: Int): LottoResultEntity?
    suspend fun getCount(): Int
    suspend fun getLatestRound(): Int?
    suspend fun insertResult(result: LottoResultEntity)
    suspend fun insertResults(results: List<LottoResultEntity>)
    suspend fun getResultsByRounds(rounds: List<Int>): List<LottoResultEntity>
}
