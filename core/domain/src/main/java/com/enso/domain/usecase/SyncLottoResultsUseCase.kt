package com.enso.domain.usecase

import com.enso.domain.model.SyncResult
import com.enso.domain.repository.LottoRepository
import javax.inject.Inject

class SyncLottoResultsUseCase @Inject constructor(
    private val lottoRepository: LottoRepository
) {
    suspend operator fun invoke(currentRound: Int): Result<SyncResult> {
        return lottoRepository.syncAllLottoResults(currentRound)
    }
}
