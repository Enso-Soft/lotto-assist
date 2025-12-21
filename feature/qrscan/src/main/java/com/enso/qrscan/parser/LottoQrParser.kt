package com.enso.qrscan.parser

data class GameInfo(
    val numbers: List<Int>,
    val isAuto: Boolean
)

data class LottoTicketInfo(
    val round: Int,
    val games: List<GameInfo>
)

object LottoQrParser {
    fun parse(qrContent: String): LottoTicketInfo? {
        return try {
            val vParam = qrContent.substringAfter("?v=", "")
            if (vParam.isEmpty()) return null

            val round = vParam.take(4).toInt()
            val gamesString = vParam.drop(4)

            // m 또는 q로 시작하는 게임들을 정규표현식으로 찾기
            // m = 수동, q = 자동
            val gamePattern = "[mq]\\d{12}".toRegex()
            val gameMatches = gamePattern.findAll(gamesString).toList()

            val games = gameMatches.map { match ->
                val gameString = match.value
                val isAuto = gameString[0] == 'q'  // q면 자동, m이면 수동
                val numbers = (0 until 6).map { numIndex ->
                    gameString.drop(1 + numIndex * 2).take(2).toInt()
                }
                GameInfo(numbers = numbers, isAuto = isAuto)
            }

            if (games.isEmpty()) return null

            LottoTicketInfo(round = round, games = games)
        } catch (e: Exception) {
            null
        }
    }
}
