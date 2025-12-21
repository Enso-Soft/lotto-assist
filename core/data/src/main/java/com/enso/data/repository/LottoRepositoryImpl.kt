package com.enso.data.repository

import com.enso.data.datasource.LottoLocalDataSource
import com.enso.data.datasource.LottoRemoteDataSource
import com.enso.data.mapper.toDomain
import com.enso.data.mapper.toEntity
import com.enso.di.IoDispatcher
import com.enso.domain.model.LottoResult
import com.enso.domain.model.SyncResult
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

    override suspend fun syncAllLottoResults(currentRound: Int): Result<SyncResult> = withContext(ioDispatcher) {
        runCatching {
            val localCount = localDataSource.getCount()

            if (localCount == 0) {
                syncAllRounds(1, currentRound)
            } else {
                val latestLocalRound = localDataSource.getLatestRound() ?: 0
                if (latestLocalRound < currentRound) {
                    syncAllRounds(latestLocalRound + 1, currentRound)
                } else {
                    SyncResult(successCount = 0, failedCount = 0, totalCount = 0)
                }
            }
        }
    }

    private suspend fun syncAllRounds(fromRound: Int, toRound: Int): SyncResult = coroutineScope {
        val batchSize = 50
        val rounds = (fromRound..toRound).toList()
        var totalSuccess = 0
        var totalFailed = 0

        rounds.chunked(batchSize).forEach { batch ->
            val results = batch.map { round ->
                async {
                    runCatching { remoteDataSource.getLottoResult(round) }
                }
            }.awaitAll()

            val successfulResults = results
                .filter { it.isSuccess }
                .mapIndexedNotNull { index, result -> result.getOrNull()?.toEntity(batch[index]) }

            val batchSuccess = successfulResults.size
            val batchFailed = batch.size - batchSuccess
            totalSuccess += batchSuccess
            totalFailed += batchFailed

            if (successfulResults.isNotEmpty()) {
                localDataSource.insertResults(successfulResults)
            }
        }

        SyncResult(
            successCount = totalSuccess,
            failedCount = totalFailed,
            totalCount = rounds.size
        )
    }

    override suspend fun getLocalCount(): Int = withContext(ioDispatcher) {
        localDataSource.getCount()
    }

    override suspend fun getLatestLocalRound(): Int? = withContext(ioDispatcher) {
        localDataSource.getLatestRound()
    }
}
