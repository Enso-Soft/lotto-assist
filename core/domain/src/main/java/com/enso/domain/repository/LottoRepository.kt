package com.enso.domain.repository

import com.enso.domain.model.LottoResult
import kotlinx.coroutines.flow.Flow

interface LottoRepository {
    suspend fun getLottoResult(round: Int): Result<LottoResult>
    fun getAllLottoResults(): Flow<List<LottoResult>>
    suspend fun syncAllLottoResults(currentRound: Int): Result<Unit>
    suspend fun getLocalCount(): Int
    suspend fun getLatestLocalRound(): Int?
}
