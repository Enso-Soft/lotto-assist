package com.enso.domain.model

import java.util.Date

data class LottoTicket(
    val ticketId: Long = 0,
    val round: Int,
    val registeredDate: Date,
    val isChecked: Boolean = false,
    val games: List<LottoGame> = emptyList(),
    val qrUrl: String? = null
)
