package com.enso.database.di

import android.content.Context
import androidx.room.Room
import com.enso.database.LottoDatabase
import com.enso.database.dao.LottoGameDao
import com.enso.database.dao.LottoResultDao
import com.enso.database.dao.LottoTicketDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLottoDatabase(@ApplicationContext context: Context): LottoDatabase {
        return Room.databaseBuilder(
            context,
            LottoDatabase::class.java,
            "lotto_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLottoResultDao(database: LottoDatabase): LottoResultDao {
        return database.lottoResultDao()
    }

    @Provides
    @Singleton
    fun provideLottoTicketDao(database: LottoDatabase): LottoTicketDao {
        return database.lottoTicketDao()
    }

    @Provides
    @Singleton
    fun provideLottoGameDao(database: LottoDatabase): LottoGameDao {
        return database.lottoGameDao()
    }
}
