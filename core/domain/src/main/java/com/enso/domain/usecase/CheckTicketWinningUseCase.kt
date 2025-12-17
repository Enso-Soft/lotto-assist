package com.enso.domain.usecase

import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoTicket
import com.enso.domain.repository.LottoRepository
import com.enso.domain.repository.LottoTicketRepository
import javax.inject.Inject

class CheckTicketWinningUseCase @Inject constructor(
    private val lottoRepository: LottoRepository,
    private val ticketRepository: LottoTicketRepository
) {
    suspend operator fun invoke(ticket: LottoTicket): Result<Unit> = runCatching {
        val lottoResult = lottoRepository.getLottoResult(ticket.round).getOrThrow()

        // 각 게임의 당첨 확인
        ticket.games.forEach { game ->
            val matchedCount = game.numbers.count { it in lottoResult.numbers }
            val bonusMatched = lottoResult.bonusNumber in game.numbers

            val rank = when {
                matchedCount == 6 -> 1               // 1등: 6개 일치
                matchedCount == 5 && bonusMatched -> 2  // 2등: 5개 + 보너스
                matchedCount == 5 -> 3               // 3등: 5개 일치
                matchedCount == 4 -> 4               // 4등: 4개 일치
                matchedCount == 3 -> 5               // 5등: 3개 일치
                else -> 0                            // 낙첨
            }

            ticketRepository.updateGameWinningRank(game.gameId, rank).getOrThrow()
        }

        // 티켓 확인 상태 업데이트
        ticketRepository.updateTicketCheckedStatus(ticket.ticketId, true).getOrThrow()
    }
}
