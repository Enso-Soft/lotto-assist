package com.enso.qrscan.parser

data class LottoTicketInfo(
    val round: Int,
    val games: List<List<Int>>
)

object LottoQrParser {
    fun parse(qrContent: String): LottoTicketInfo? {
        return try {
            val vParam = qrContent.substringAfter("?v=", "")
            if (vParam.isEmpty()) return null

            val round = vParam.take(4).toInt()

            val gameCount = when (vParam.getOrNull(4)) {
                'm' -> 1
                'n' -> 2
                'o' -> 3
                'p' -> 4
                'q' -> 5
                else -> return null
            }

            val numbersString = vParam.drop(5)
            val games = (0 until gameCount).map { gameIndex ->
                val gameNumbers = numbersString.drop(gameIndex * 12).take(12)
                (0 until 6).map { numIndex ->
                    gameNumbers.drop(numIndex * 2).take(2).toInt()
                }
            }

            LottoTicketInfo(round = round, games = games)
        } catch (e: Exception) {
            null
        }
    }
}
