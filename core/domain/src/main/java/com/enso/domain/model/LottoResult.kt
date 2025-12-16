package com.enso.domain.model

import java.util.Date

data class LottoResult(
    val round: Int,
    val drawDate: Date,
    val numbers: List<Int>,
    val bonusNumber: Int,
    val firstPrize: FirstPrizeInfo
)

data class FirstPrizeInfo(
    val winAmount: Long,
    val winnerCount: Int,
    val totalSalesAmount: Long
)
