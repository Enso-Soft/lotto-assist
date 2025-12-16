package com.enso.domain.model

import java.util.Date

data class UserLottoTicket(
    val id: Long = 0,
    val round: Int,
    val numbers: List<Int>,
    val gameType: GameType,
    val registeredDate: Date,
    val winningRank: Int = 0,
    val isChecked: Boolean = false
)

enum class GameType(val code: String, val displayName: String) {
    AUTO("A", "자동"),
    MANUAL("B", "수동");

    companion object {
        fun fromCode(code: String): GameType {
            return values().find { it.code == code } ?: AUTO
        }
    }
}
