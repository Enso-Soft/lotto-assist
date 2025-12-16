package com.enso.data.datasource

import com.enso.database.dao.LottoResultDao
import com.enso.database.entity.LottoResultEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LottoLocalDataSourceImpl @Inject constructor(
    private val lottoResultDao: LottoResultDao
) : LottoLocalDataSource {

    override fun getAllResults(): Flow<List<LottoResultEntity>> {
        return lottoResultDao.getAllResults()
    }

    override suspend fun getResultByRound(round: Int): LottoResultEntity? {
        return lottoResultDao.getResultByRound(round)
    }

    override suspend fun getCount(): Int {
        return lottoResultDao.getCount()
    }

    override suspend fun getLatestRound(): Int? {
        return lottoResultDao.getLatestRound()
    }

    override suspend fun insertResult(result: LottoResultEntity) {
        lottoResultDao.insertResult(result)
    }

    override suspend fun insertResults(results: List<LottoResultEntity>) {
        lottoResultDao.insertResults(results)
    }

    override suspend fun getResultsByRounds(rounds: List<Int>): List<LottoResultEntity> {
        return lottoResultDao.getResultsByRounds(rounds)
    }
}
