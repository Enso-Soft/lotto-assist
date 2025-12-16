package com.enso.data.mapper

import com.enso.database.entity.LottoResultEntity
import com.enso.domain.model.FirstPrizeInfo
import com.enso.domain.model.LottoResult
import com.enso.network.model.LottoInformation
import java.util.Date

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

fun LottoInformation.toEntity(): LottoResultEntity {
    return LottoResultEntity(
        round = drawRound,
        drawDate = drawDate.time,
        number1 = drawNo1,
        number2 = drawNo2,
        number3 = drawNo3,
        number4 = drawNo4,
        number5 = drawNo5,
        number6 = drawNo6,
        bonusNumber = drawBonusNo,
        firstPrizeAmount = firstStPrizeMoney,
        firstPrizeWinnerCount = firstStPrizeWinners,
        totalSalesAmount = totalSellerMoney
    )
}

fun LottoResultEntity.toDomain(): LottoResult {
    return LottoResult(
        round = round,
        drawDate = Date(drawDate),
        numbers = listOf(number1, number2, number3, number4, number5, number6).sorted(),
        bonusNumber = bonusNumber,
        firstPrize = FirstPrizeInfo(
            winAmount = firstPrizeAmount,
            winnerCount = firstPrizeWinnerCount,
            totalSalesAmount = totalSalesAmount
        )
    )
}
