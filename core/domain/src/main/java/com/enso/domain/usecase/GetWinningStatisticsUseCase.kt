package com.enso.domain.usecase

import com.enso.domain.model.LottoTicket
import com.enso.domain.model.WinningStatistics
import com.enso.domain.repository.LottoTicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving winning statistics from all lottery tickets.
 *
 * Calculates aggregated statistics including total games played,
 * winning count, winning rate, and breakdown by rank.
 */
class GetWinningStatisticsUseCase @Inject constructor(
    private val repository: LottoTicketRepository
) {
    /**
     * Returns a Flow of [WinningStatistics] that emits updated statistics
     * whenever the ticket data changes.
     */
    operator fun invoke(): Flow<WinningStatistics> {
        return repository.getAllTickets().map { tickets ->
            calculateStatistics(tickets)
        }
    }

    /**
     * Calculates statistics from a list of tickets using single-pass iteration.
     * This function is internal for testing purposes.
     */
    internal fun calculateStatistics(tickets: List<LottoTicket>): WinningStatistics {
        if (tickets.isEmpty()) {
            return WinningStatistics.EMPTY
        }

        val validRanks = WinningStatistics.VALID_WINNING_RANKS

        var totalGamesPlayed = 0
        var checkedGamesCount = 0
        var winningGamesCount = 0
        val rankCounts = mutableMapOf<Int, Int>().apply {
            validRanks.forEach { rank -> this[rank] = 0 }
        }

        // Single-pass iteration through all tickets and games
        for (ticket in tickets) {
            val gamesInTicket = ticket.games
            totalGamesPlayed += gamesInTicket.size

            if (ticket.isChecked) {
                for (game in gamesInTicket) {
                    checkedGamesCount++

                    val rank = game.winningRank
                    if (rank in validRanks) {
                        winningGamesCount++
                        rankCounts[rank] = rankCounts.getValue(rank) + 1
                    }
                }
            }
        }

        // Calculate winning rate (avoid division by zero)
        val winningRate = if (checkedGamesCount > 0) {
            (winningGamesCount.toFloat() / checkedGamesCount.toFloat()) * 100f
        } else {
            0f
        }

        return WinningStatistics(
            totalGamesPlayed = totalGamesPlayed,
            checkedGamesCount = checkedGamesCount,
            winningGamesCount = winningGamesCount,
            winningRate = winningRate,
            rankBreakdown = rankCounts.toMap(),
            totalTickets = tickets.size
        )
    }
}
