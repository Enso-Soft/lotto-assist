package com.enso.data.repository

import app.cash.turbine.test
import com.enso.data.datasource.UserPreferencesDataSource
import com.enso.domain.model.TicketSortType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UserPreferencesRepositoryImplTest {

    private lateinit var repository: UserPreferencesRepositoryImpl
    private lateinit var dataSource: UserPreferencesDataSource

    @Before
    fun setup() {
        dataSource = mockk()
        repository = UserPreferencesRepositoryImpl(dataSource)
    }

    // ==================== getSortType Tests ====================

    @Test
    fun `getSortType은 DataSource로부터 Flow를 반환한다`() = runTest {
        // Given
        val expectedSortType = TicketSortType.REGISTERED_DATE_DESC
        every { dataSource.getSortType() } returns flowOf(expectedSortType)

        // When & Then
        repository.getSortType().test {
            val emittedValue = awaitItem()
            assertEquals(expectedSortType, emittedValue)
            awaitComplete()
        }
    }

    @Test
    fun `getSortType은 DEFAULT 정렬 타입을 반환한다`() = runTest {
        // Given
        every { dataSource.getSortType() } returns flowOf(TicketSortType.DEFAULT)

        // When & Then
        repository.getSortType().test {
            val emittedValue = awaitItem()
            assertEquals(TicketSortType.DEFAULT, emittedValue)
            assertEquals(TicketSortType.REGISTERED_DATE_DESC, emittedValue) // DEFAULT == REGISTERED_DATE_DESC
            awaitComplete()
        }
    }

    @Test
    fun `getSortType은 다양한 정렬 타입을 반환할 수 있다`() = runTest {
        // Test ROUND_DESC
        every { dataSource.getSortType() } returns flowOf(TicketSortType.ROUND_DESC)
        repository.getSortType().test {
            assertEquals(TicketSortType.ROUND_DESC, awaitItem())
            awaitComplete()
        }

        // Test ROUND_ASC
        every { dataSource.getSortType() } returns flowOf(TicketSortType.ROUND_ASC)
        repository.getSortType().test {
            assertEquals(TicketSortType.ROUND_ASC, awaitItem())
            awaitComplete()
        }

        // Test REGISTERED_DATE_ASC
        every { dataSource.getSortType() } returns flowOf(TicketSortType.REGISTERED_DATE_ASC)
        repository.getSortType().test {
            assertEquals(TicketSortType.REGISTERED_DATE_ASC, awaitItem())
            awaitComplete()
        }
    }

    // ==================== saveSortType Tests ====================

    @Test
    fun `saveSortType은 DataSource의 saveSortType을 호출한다`() = runTest {
        // Given
        val sortTypeToSave = TicketSortType.ROUND_DESC
        coEvery { dataSource.saveSortType(sortTypeToSave) } returns Unit

        // When
        repository.saveSortType(sortTypeToSave)

        // Then
        coVerify(exactly = 1) { dataSource.saveSortType(sortTypeToSave) }
    }

    @Test
    fun `saveSortType은 DEFAULT 정렬 타입을 저장할 수 있다`() = runTest {
        // Given
        val sortTypeToSave = TicketSortType.DEFAULT
        coEvery { dataSource.saveSortType(sortTypeToSave) } returns Unit

        // When
        repository.saveSortType(sortTypeToSave)

        // Then
        coVerify(exactly = 1) { dataSource.saveSortType(TicketSortType.REGISTERED_DATE_DESC) }
    }

    @Test
    fun `saveSortType은 모든 정렬 타입을 저장할 수 있다`() = runTest {
        // Given
        coEvery { dataSource.saveSortType(any()) } returns Unit

        // When & Then - Test all TicketSortType values
        TicketSortType.entries.forEach { sortType ->
            repository.saveSortType(sortType)
            coVerify { dataSource.saveSortType(sortType) }
        }
    }

    @Test
    fun `saveSortType 후 getSortType은 저장된 값을 반환한다`() = runTest {
        // Given
        val sortTypeToSave = TicketSortType.ROUND_ASC
        coEvery { dataSource.saveSortType(sortTypeToSave) } returns Unit
        every { dataSource.getSortType() } returns flowOf(sortTypeToSave)

        // When
        repository.saveSortType(sortTypeToSave)

        // Then
        repository.getSortType().test {
            assertEquals(sortTypeToSave, awaitItem())
            awaitComplete()
        }
    }
}
