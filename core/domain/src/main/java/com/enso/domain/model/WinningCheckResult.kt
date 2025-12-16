package com.enso.domain.model

data class WinningCheckResult(
    val round: Int,
    val userNumbers: List<Int>,
    val winningNumbers: List<Int>,
    val bonusNumber: Int,
    val matchedNumbers: List<Int>,
    val bonusMatched: Boolean,
    val rank: Int
)
