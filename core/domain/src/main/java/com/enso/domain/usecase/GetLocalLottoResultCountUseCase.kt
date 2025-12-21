package com.enso.domain.usecase

import com.enso.domain.repository.LottoRepository
import javax.inject.Inject

class GetLocalLottoResultCountUseCase @Inject constructor(
    private val lottoRepository: LottoRepository
) {
    suspend operator fun invoke(): Int {
        return lottoRepository.getLocalCount()
    }
}
