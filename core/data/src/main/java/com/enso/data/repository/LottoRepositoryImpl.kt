package com.enso.data.repository

import android.util.Log
import com.enso.data.datasource.LottoLocalDataSource
import com.enso.data.datasource.LottoRemoteDataSource
import com.enso.data.mapper.toDomain
import com.enso.data.mapper.toEntity
import com.enso.di.IoDispatcher
import com.enso.domain.model.LottoResult
import com.enso.domain.repository.LottoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LottoRepositoryImpl @Inject constructor(
    private val remoteDataSource: LottoRemoteDataSource,
    private val localDataSource: LottoLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LottoRepository {

    override suspend fun getLottoResult(round: Int): Result<LottoResult> = withContext(ioDispatcher) {
        runCatching {
            localDataSource.getResultByRound(round)?.toDomain()
                ?: run {
                    val response = remoteDataSource.getLottoResult(round)
                    localDataSource.insertResult(response.toEntity(round))
                    response.toDomain()
                }
        }
    }

    override fun getAllLottoResults(): Flow<List<LottoResult>> {
        return localDataSource.getAllResults().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncAllLottoResults(currentRound: Int): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val localCount = localDataSource.getCount()

            if (localCount == 0) {
                // 최근 100개 회차만 가져오기
                syncAllRounds(1, currentRound)
            } else {
                val latestLocalRound = localDataSource.getLatestRound() ?: 0
                if (latestLocalRound < currentRound) {
                    // 최신 회차만 추가
                    syncAllRounds(latestLocalRound + 1, currentRound)
                }
            }
            Unit
        }
    }

    private suspend fun syncAllRounds(fromRound: Int, toRound: Int) = coroutineScope {
        Log.d("whk__", "Syncing rounds from $fromRound to $toRound")
        val batchSize = 50
        val rounds = (fromRound..toRound).toList()

        rounds.chunked(batchSize).forEach { batch ->
            Log.d("whk__", "batch : $batch")
            val results = batch.map { round ->
                async {
                    runCatching { remoteDataSource.getLottoResult(round) }
                }
            }.awaitAll()

            val successfulResults = results
                .filter { it.isSuccess }
                .mapIndexedNotNull { index, result -> result.getOrNull()?.toEntity(batch[index]) }

            if (successfulResults.isNotEmpty()) {
                android.util.Log.d("LottoRepository", "Inserting ${successfulResults.size} results")
                localDataSource.insertResults(successfulResults)
            } else {
                android.util.Log.w("LottoRepository", "No successful results in batch")
            }
        }

        val finalCount = localDataSource.getCount()
        android.util.Log.d("LottoRepository", "Sync complete. Total count in DB: $finalCount")
    }

    override suspend fun getLocalCount(): Int = withContext(ioDispatcher) {
        localDataSource.getCount()
    }

    override suspend fun getLatestLocalRound(): Int? = withContext(ioDispatcher) {
        localDataSource.getLatestRound()
    }
}
