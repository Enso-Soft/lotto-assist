package com.enso.domain.usecase

import com.enso.domain.repository.LottoRepository
import javax.inject.Inject

class SyncLottoResultsUseCase @Inject constructor(
    private val lottoRepository: LottoRepository
) {
    suspend operator fun invoke(currentRound: Int): Result<Unit> {
        return lottoRepository.syncAllLottoResults(currentRound)
    }
}
