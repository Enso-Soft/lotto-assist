package com.enso.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lotto_games",
    foreignKeys = [ForeignKey(
        entity = LottoTicketEntity::class,
        parentColumns = ["ticketId"],
        childColumns = ["ticketId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("ticketId")]
)
data class LottoGameEntity(
    @PrimaryKey(autoGenerate = true)
    val gameId: Long = 0,
    val ticketId: Long,                // FK -> lotto_tickets
    val gameLabel: String,             // "A", "B", "C", "D", "E"
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val gameType: String,              // "A": 자동, "B": 수동
    val winningRank: Int = 0           // 당첨 등수 (0: 미확인/낙첨)
)
