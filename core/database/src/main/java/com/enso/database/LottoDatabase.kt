package com.enso.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.enso.database.dao.LottoResultDao
import com.enso.database.entity.LottoResultEntity

@Database(
    entities = [LottoResultEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LottoDatabase : RoomDatabase() {
    abstract fun lottoResultDao(): LottoResultDao
}
