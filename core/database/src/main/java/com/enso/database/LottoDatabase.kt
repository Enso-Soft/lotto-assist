package com.enso.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enso.database.dao.LottoResultDao
import com.enso.database.dao.UserLottoTicketDao
import com.enso.database.entity.LottoResultEntity
import com.enso.database.entity.UserLottoTicketEntity

@Database(
    entities = [
        LottoResultEntity::class,
        UserLottoTicketEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class LottoDatabase : RoomDatabase() {
    abstract fun lottoResultDao(): LottoResultDao
    abstract fun userLottoTicketDao(): UserLottoTicketDao
}
