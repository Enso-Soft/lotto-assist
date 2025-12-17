package com.enso.data.mapper

import com.enso.database.entity.LottoGameEntity
import com.enso.database.entity.LottoTicketEntity
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoTicket
import java.util.Date

// Entity -> Domain
fun LottoTicketEntity.toDomain(games: List<LottoGameEntity>): LottoTicket {
    return LottoTicket(
        ticketId = this.ticketId,
        round = this.round,
        registeredDate = Date(this.registeredDate),
        isChecked = this.isChecked,
        games = games.map { it.toDomain() }
    )
}

fun LottoGameEntity.toDomain(): LottoGame {
    return LottoGame(
        gameId = this.gameId,
        gameLabel = this.gameLabel,
        numbers = listOf(number1, number2, number3, number4, number5, number6),
        gameType = GameType.fromCode(this.gameType),
        winningRank = this.winningRank
    )
}

// Domain -> Entity
fun LottoTicket.toTicketEntity(): LottoTicketEntity {
    return LottoTicketEntity(
        ticketId = this.ticketId,
        round = this.round,
        registeredDate = this.registeredDate.time,
        isChecked = this.isChecked
    )
}

fun LottoGame.toGameEntity(ticketId: Long): LottoGameEntity {
    require(numbers.size == 6) { "Lotto game must have exactly 6 numbers" }
    val sortedNumbers = numbers.sorted()
    return LottoGameEntity(
        gameId = this.gameId,
        ticketId = ticketId,
        gameLabel = this.gameLabel,
        number1 = sortedNumbers[0],
        number2 = sortedNumbers[1],
        number3 = sortedNumbers[2],
        number4 = sortedNumbers[3],
        number5 = sortedNumbers[4],
        number6 = sortedNumbers[5],
        gameType = this.gameType.code,
        winningRank = this.winningRank
    )
}
