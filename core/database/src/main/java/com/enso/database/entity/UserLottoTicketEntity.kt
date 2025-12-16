package com.enso.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_lotto_tickets")
data class UserLottoTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val round: Int,
    val number1: Int,
    val number2: Int,
    val number3: Int,
    val number4: Int,
    val number5: Int,
    val number6: Int,
    val gameType: String, // "A" for 자동, "B" for 수동
    val registeredDate: Long, // timestamp in milliseconds
    val winningRank: Int = 0, // 0: 미확인/낙첨, 1-5: 당첨 등수
    val isChecked: Boolean = false // 당첨 확인 여부
)
