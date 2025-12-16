package com.enso.domain.repository

import com.enso.domain.model.LottoResult

interface LottoRepository {
    suspend fun getLottoResult(round: Int): Result<LottoResult>
}
