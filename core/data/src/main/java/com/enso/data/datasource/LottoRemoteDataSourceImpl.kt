package com.enso.data.datasource

import com.enso.network.api.LottoApi
import com.enso.network.model.LottoInformation
import javax.inject.Inject

class LottoRemoteDataSourceImpl @Inject constructor(
    private val lottoApi: LottoApi
) : LottoRemoteDataSource {

    override suspend fun getLottoResult(round: Int): LottoInformation {
        return lottoApi.getLottoNumber(drwNo = round)
    }
}
