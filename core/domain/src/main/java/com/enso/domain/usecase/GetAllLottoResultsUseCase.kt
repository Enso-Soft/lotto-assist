package com.enso.domain.usecase

import com.enso.domain.model.LottoResult
import com.enso.domain.repository.LottoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllLottoResultsUseCase @Inject constructor(
    private val lottoRepository: LottoRepository
) {
    operator fun invoke(): Flow<List<LottoResult>> {
        return lottoRepository.getAllLottoResults()
    }
}
