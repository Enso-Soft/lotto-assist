package com.enso.data.mapper

import com.enso.domain.model.FirstPrizeInfo
import com.enso.domain.model.LottoResult
import com.enso.network.model.LottoInformation

fun LottoInformation.toDomain(): LottoResult {
    return LottoResult(
        round = drawRound,
        drawDate = drawDate,
        numbers = listOf(
            drawNo1,
            drawNo2,
            drawNo3,
            drawNo4,
            drawNo5,
            drawNo6
        ).sorted(),
        bonusNumber = drawBonusNo,
        firstPrize = FirstPrizeInfo(
            winAmount = firstStPrizeMoney,
            winnerCount = firstStPrizeWinners,
            totalSalesAmount = totalSellerMoney
        )
    )
}
