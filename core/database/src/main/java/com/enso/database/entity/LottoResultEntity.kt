package com.enso.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotto_results")
data class LottoResultEntity(
    @PrimaryKey
    val round: Int,
    val drawDate: Long,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val bonusNumber: Int,
    val firstPrizeAmount: Long,
    val firstPrizeWinnerCount: Int,
    val totalSalesAmount: Long
)
