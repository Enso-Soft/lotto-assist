package com.enso.network.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class LottoInformation(
    @SerializedName("drwtNo")
    val drawRound: Int,
    @SerializedName("totSellamnt")
    val totalSellerMoney: Long,
    @SerializedName("drwNoDate")
    val drawDate: Date,
    @SerializedName("firstWinamnt")
    val firstStPrizeMoney: Long,
    @SerializedName("firstPrzwnerCo")
    val firstStPrizeWinners: Int,
    @SerializedName("firstAccumamnt")
    val firstStAccumulatedMoney: Long,

    @SerializedName("drwtNo1")
    val drawNo1: Int,
    @SerializedName("drwtNo2")
    val drawNo2: Int,
    @SerializedName("drwtNo3")
    val drawNo3: Int,
    @SerializedName("drwtNo4")
    val drawNo4: Int,
    @SerializedName("drwtNo5")
    val drawNo5: Int,
    @SerializedName("drwtNo6")
    val drawNo6: Int,
    @SerializedName("bnusNo")
    val drawBonusNo: Int
)