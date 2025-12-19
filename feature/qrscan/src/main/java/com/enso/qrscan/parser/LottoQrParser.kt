package com.enso.qrscan.parser

import android.util.Log

data class GameInfo(
    val numbers: List<Int>,
    val isAuto: Boolean
)

data class LottoTicketInfo(
    val round: Int,
    val games: List<GameInfo>,
    val qrUrl: String
)

object LottoQrParser {
    fun parse(qrContent: String): LottoTicketInfo? {
        return try {
            val vParam = qrContent.substringAfter("?v=", "")
            if (vParam.isEmpty()) return null

            Log.d("whk__", "vParam : $vParam")

            val round = vParam.take(4).toInt()
            Log.d("whk__", "round : $round")

            val gamesString = vParam.drop(4)
            Log.d("whk__", "gamesString : $gamesString")

            // m 또는 q로 시작하는 게임들을 정규표현식으로 찾기
            // m = 수동, q = 자동
            val gamePattern = "[mq]\\d{12}".toRegex()
            val gameMatches = gamePattern.findAll(gamesString).toList()
            Log.d("whk__", "gameMatches count : ${gameMatches.size}")

            val games = gameMatches.map { match ->
                val gameString = match.value
                val isAuto = gameString[0] == 'q'  // q면 자동, m이면 수동
                val numbers = (0 until 6).map { numIndex ->
                    gameString.drop(1 + numIndex * 2).take(2).toInt()
                }
                Log.d("whk__", "game: isAuto=$isAuto, numbers=$numbers")
                GameInfo(numbers = numbers, isAuto = isAuto)
            }

            if (games.isEmpty()) return null

            LottoTicketInfo(round = round, games = games, qrUrl = qrContent)
        } catch (e: Exception) {
            Log.d("whk__", "error : $e")
            null
        }
    }
}
