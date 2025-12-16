package com.enso.data.mapper

import com.enso.database.entity.UserLottoTicketEntity
import com.enso.domain.model.GameType
import com.enso.domain.model.UserLottoTicket
import java.util.Date

fun UserLottoTicketEntity.toDomain(): UserLottoTicket {
    return UserLottoTicket(
        id = this.id,
        round = this.round,
        numbers = listOf(
            this.number1,
            this.number2,
            this.number3,
            this.number4,
            this.number5,
            this.number6
        ),
        gameType = GameType.fromCode(this.gameType),
        registeredDate = Date(this.registeredDate),
        winningRank = this.winningRank,
        isChecked = this.isChecked
    )
}

fun UserLottoTicket.toEntity(): UserLottoTicketEntity {
    require(numbers.size == 6) { "Lotto ticket must have exactly 6 numbers" }

    return UserLottoTicketEntity(
        id = this.id,
        round = this.round,
        number1 = this.numbers[0],
        number2 = this.numbers[1],
        number3 = this.numbers[2],
        number4 = this.numbers[3],
        number5 = this.numbers[4],
        number6 = this.numbers[5],
        gameType = this.gameType.code,
        registeredDate = this.registeredDate.time,
        winningRank = this.winningRank,
        isChecked = this.isChecked
    )
}
