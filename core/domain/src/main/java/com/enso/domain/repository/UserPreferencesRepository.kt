package com.enso.domain.repository

import com.enso.domain.model.TicketSortType
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getSortType(): Flow<TicketSortType>
    suspend fun saveSortType(sortType: TicketSortType)
}
