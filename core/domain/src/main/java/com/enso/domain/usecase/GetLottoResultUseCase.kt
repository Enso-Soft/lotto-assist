package com.enso.domain.usecase

import com.enso.domain.model.LottoResult
import com.enso.domain.repository.LottoRepository
import javax.inject.Inject

class GetLottoResultUseCase @Inject constructor(
    private val lottoRepository: LottoRepository
) {
    suspend operator fun invoke(round: Int): Result<LottoResult> {
        return lottoRepository.getLottoResult(round)
    }
}
