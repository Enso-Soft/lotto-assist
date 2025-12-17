package com.enso.domain.model

data class LottoGame(
    val gameId: Long = 0,
    val gameLabel: String,        // "A", "B", "C", "D", "E"
    val numbers: List<Int>,
    val gameType: GameType,
    val winningRank: Int = 0
)

enum class GameType(val code: String, val displayName: String) {
    AUTO("A", "자동"),
    MANUAL("B", "수동");

    companion object {
        fun fromCode(code: String): GameType {
            return entries.find { it.code == code } ?: AUTO
        }
    }
}
