package com.enso.domain.repository

import com.enso.domain.model.TicketSortType
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    fun getSortType(): Flow<TicketSortType>
    suspend fun saveSortType(sortType: TicketSortType)

    /**
     * 번호 직접 입력 시 자동/수동 기본값 조회
     */
    fun getManualInputDefaultIsAuto(): Flow<Boolean>

    /**
     * 번호 직접 입력 시 자동/수동 기본값 저장
     */
    suspend fun saveManualInputDefaultIsAuto(isAuto: Boolean)
}
