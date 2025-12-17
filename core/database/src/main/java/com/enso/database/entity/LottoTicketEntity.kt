package com.enso.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lotto_tickets")
data class LottoTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val ticketId: Long = 0,
    val round: Int,                    // 회차
    val registeredDate: Long,          // 등록일시 (timestamp)
    val isChecked: Boolean = false     // 당첨 확인 여부
)
