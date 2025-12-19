package com.enso.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enso.database.dao.LottoGameDao
import com.enso.database.dao.LottoResultDao
import com.enso.database.dao.LottoTicketDao
import com.enso.database.entity.LottoGameEntity
import com.enso.database.entity.LottoResultEntity
import com.enso.database.entity.LottoTicketEntity

@Database(
    entities = [
        LottoResultEntity::class,
        LottoTicketEntity::class,
        LottoGameEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class LottoDatabase : RoomDatabase() {
    abstract fun lottoResultDao(): LottoResultDao
    abstract fun lottoTicketDao(): LottoTicketDao
    abstract fun lottoGameDao(): LottoGameDao
}
