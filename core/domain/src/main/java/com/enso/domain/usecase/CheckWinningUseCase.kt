package com.enso.domain.usecase

import com.enso.domain.model.LottoResult
import com.enso.domain.model.WinningCheckResult
import com.enso.domain.repository.LottoRepository
import javax.inject.Inject

class CheckWinningUseCase @Inject constructor(
    private val lottoRepository: LottoRepository
) {
    suspend operator fun invoke(
        round: Int,
        userNumbers: List<Int>
    ): Result<WinningCheckResult> {
        return lottoRepository.getLottoResult(round).map { lottoResult ->
            checkWinning(lottoResult, userNumbers)
        }
    }

    private fun checkWinning(lottoResult: LottoResult, userNumbers: List<Int>): WinningCheckResult {
        val matchedNumbers = userNumbers.filter { it in lottoResult.numbers }
        val matchedCount = matchedNumbers.size
        val bonusMatched = lottoResult.bonusNumber in userNumbers

        val rank = when {
            matchedCount == 6 -> 1
            matchedCount == 5 && bonusMatched -> 2
            matchedCount == 5 -> 3
            matchedCount == 4 -> 4
            matchedCount == 3 -> 5
            else -> 0
        }

        return WinningCheckResult(
            round = lottoResult.round,
            userNumbers = userNumbers,
            winningNumbers = lottoResult.numbers,
            bonusNumber = lottoResult.bonusNumber,
            matchedNumbers = matchedNumbers,
            bonusMatched = bonusMatched,
            rank = rank
        )
    }
}
