package com.enso.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.enso.domain.model.TicketSortType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<Preferences> = context.dataStore

    fun getSortType(): Flow<TicketSortType> {
        return dataStore.data.map { preferences ->
            val sortTypeName = preferences[SORT_TYPE_KEY] ?: TicketSortType.DEFAULT.name
            try {
                TicketSortType.valueOf(sortTypeName)
            } catch (e: IllegalArgumentException) {
                TicketSortType.DEFAULT
            }
        }
    }

    suspend fun saveSortType(sortType: TicketSortType) {
        dataStore.edit { preferences ->
            preferences[SORT_TYPE_KEY] = sortType.name
        }
    }

    companion object {
        private val SORT_TYPE_KEY = stringPreferencesKey("ticket_sort_type")
    }
}
