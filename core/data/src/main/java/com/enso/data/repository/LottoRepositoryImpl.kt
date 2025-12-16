package com.enso.data.repository

import com.enso.data.mapper.toDomain
import com.enso.di.IoDispatcher
import com.enso.domain.model.LottoResult
import com.enso.domain.repository.LottoRepository
import com.enso.network.api.LottoApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LottoRepositoryImpl @Inject constructor(
    private val lottoApi: LottoApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : LottoRepository {

    override suspend fun getLottoResult(round: Int): Result<LottoResult> = withContext(ioDispatcher) {
        runCatching {
            val response = lottoApi.getLottoNumber(drwNo = round)
            response.toDomain()
        }
    }
}
