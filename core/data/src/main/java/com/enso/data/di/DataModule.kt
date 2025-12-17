package com.enso.data.di

import com.enso.data.datasource.LottoLocalDataSource
import com.enso.data.datasource.LottoLocalDataSourceImpl
import com.enso.data.datasource.LottoRemoteDataSource
import com.enso.data.datasource.LottoRemoteDataSourceImpl
import com.enso.data.datasource.LottoTicketLocalDataSource
import com.enso.data.datasource.LottoTicketLocalDataSourceImpl
import com.enso.data.repository.LottoRepositoryImpl
import com.enso.data.repository.LottoTicketRepositoryImpl
import com.enso.domain.repository.LottoRepository
import com.enso.domain.repository.LottoTicketRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindLottoRepository(impl: LottoRepositoryImpl): LottoRepository

    @Binds
    @Singleton
    fun bindLottoTicketRepository(impl: LottoTicketRepositoryImpl): LottoTicketRepository

    @Binds
    @Singleton
    fun bindLottoLocalDataSource(impl: LottoLocalDataSourceImpl): LottoLocalDataSource

    @Binds
    @Singleton
    fun bindLottoRemoteDataSource(impl: LottoRemoteDataSourceImpl): LottoRemoteDataSource

    @Binds
    @Singleton
    fun bindLottoTicketLocalDataSource(impl: LottoTicketLocalDataSourceImpl): LottoTicketLocalDataSource
}
