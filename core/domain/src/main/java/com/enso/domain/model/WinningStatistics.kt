package com.enso.domain.model

import java.util.Locale

/**
 * Represents winning statistics aggregated from all lottery tickets.
 *
 * @property totalGamesPlayed Total number of games across all tickets
 * @property checkedGamesCount Number of games from checked tickets (draw completed)
 * @property winningGamesCount Number of games that won (rank 1-5)
 * @property winningRate Winning percentage (0-100)
 * @property rankBreakdown Count of wins per rank (1-5)
 * @property totalTickets Total number of tickets
 */
data class WinningStatistics(
    val totalGamesPlayed: Int,
    val checkedGamesCount: Int,
    val winningGamesCount: Int,
    val winningRate: Float,
    val rankBreakdown: Map<Int, Int>,
    val totalTickets: Int
) {
    companion object {
        /**
         * Valid winning ranks in Korean Lotto (1st to 5th place).
         * Used for statistics calculation and rank breakdown initialization.
         */
        val VALID_WINNING_RANKS: IntRange = 1..5

        val EMPTY = WinningStatistics(
            totalGamesPlayed = 0,
            checkedGamesCount = 0,
            winningGamesCount = 0,
            winningRate = 0f,
            rankBreakdown = VALID_WINNING_RANKS.associateWith { 0 },
            totalTickets = 0
        )
    }

    /**
     * Formatted winning rate string with 1 decimal place and % suffix.
     * Uses US locale to ensure consistent decimal point representation.
     * Example: "12.5%"
     */
    val formattedWinningRate: String
        get() = String.format(Locale.US, "%.1f%%", winningRate)

    /**
     * Whether there is any checked game data available.
     */
    val hasData: Boolean
        get() = checkedGamesCount > 0

    /**
     * Whether there are any winning games.
     */
    val hasWins: Boolean
        get() = winningGamesCount > 0
}
