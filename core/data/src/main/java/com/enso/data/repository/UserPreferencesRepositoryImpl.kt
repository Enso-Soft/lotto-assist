package com.enso.data.repository

import com.enso.data.datasource.UserPreferencesDataSource
import com.enso.domain.model.TicketSortType
import com.enso.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataSource: UserPreferencesDataSource
) : UserPreferencesRepository {

    override fun getSortType(): Flow<TicketSortType> {
        return dataSource.getSortType()
    }

    override suspend fun saveSortType(sortType: TicketSortType) {
        dataSource.saveSortType(sortType)
    }
}
