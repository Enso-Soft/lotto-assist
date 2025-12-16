package com.enso.data.datasource

import com.enso.network.model.LottoInformation

interface LottoRemoteDataSource {
    suspend fun getLottoResult(round: Int): LottoInformation
}
