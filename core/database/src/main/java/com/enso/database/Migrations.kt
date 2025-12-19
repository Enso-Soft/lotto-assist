package com.enso.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE lotto_tickets ADD COLUMN qrUrl TEXT")
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS index_lotto_tickets_qrUrl ON lotto_tickets(qrUrl)"
        )
    }
}
