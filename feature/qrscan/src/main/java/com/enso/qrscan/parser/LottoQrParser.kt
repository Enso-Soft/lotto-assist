package com.enso.qrscan.parser

import android.util.Log

data class LottoTicketInfo(
    val round: Int,
    val games: List<List<Int>>
)

object LottoQrParser {
    fun parse(qrContent: String): LottoTicketInfo? {
        return try {
            val vParam = qrContent.substringAfter("?v=", "")
            if (vParam.isEmpty()) return null

            Log.d("whk__", "vParam : $vParam")

            val round = vParam.take(4).toInt()
            Log.d("whk__", "round : $round")

            val gameCount = when (vParam.getOrNull(4)) {
                'm' -> 1
                'n' -> 2
                'o' -> 3
                'p' -> 4
                'q' -> 5
                else -> return null
            }

            Log.d("whk__", "gameCount : $gameCount")

            val numbersString = vParam.drop(5)
            Log.d("whk__", "numbersString : $numbersString")

            // Split by 'q' to separate individual games
            val gameStrings = numbersString.split('q').filter { it.isNotEmpty() }
            Log.d("whk__", "gameStrings : $gameStrings")

            val games = gameStrings.take(gameCount).map { gameNumbers ->
                // Each game should have 12 digits (6 numbers x 2 digits each)
                (0 until 6).map { numIndex ->
                    gameNumbers.drop(numIndex * 2).take(2).toInt()
                }
            }
            Log.d("whk__", "games : $games")

            LottoTicketInfo(round = round, games = games)
        } catch (e: Exception) {
            Log.d("whk__", "error : $e")
            null
        }
    }
}
