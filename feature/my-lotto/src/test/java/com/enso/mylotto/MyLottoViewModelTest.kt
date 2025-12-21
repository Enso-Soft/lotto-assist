package com.enso.mylotto

import app.cash.turbine.test
import com.enso.domain.model.FirstPrizeInfo
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoResult
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.TicketSortType
import com.enso.domain.usecase.CheckTicketWinningUseCase
import com.enso.domain.usecase.DeleteLottoTicketUseCase
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLottoTicketsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
class MyLottoViewModelTest {

    private lateinit var viewModel: MyLottoViewModel
    private lateinit var getLottoTicketsUseCase: GetLottoTicketsUseCase
    private lateinit var deleteLottoTicketUseCase: DeleteLottoTicketUseCase
    private lateinit var checkTicketWinningUseCase: CheckTicketWinningUseCase
    private lateinit var getAllLottoResultsUseCase: GetAllLottoResultsUseCase

    private val testDispatcher = StandardTestDispatcher()

    // Mock data
    private val mockGame1 = LottoGame(
        gameId = 1,
        gameLabel = "A",
        numbers = listOf(1, 2, 3, 4, 5, 6),
        gameType = GameType.AUTO,
        winningRank = 1
    )

    private val mockGame2 = LottoGame(
        gameId = 2,
        gameLabel = "B",
        numbers = listOf(7, 8, 9, 10, 11, 12),
        gameType = GameType.MANUAL,
        winningRank = 0
    )

    private val mockGame3 = LottoGame(
        gameId = 3,
        gameLabel = "C",
        numbers = listOf(13, 14, 15, 16, 17, 18),
        gameType = GameType.AUTO,
        winningRank = 5
    )

    private val mockTicket1 = LottoTicket(
        ticketId = 1,
        round = 1145,
        registeredDate = Date(),
        isChecked = false,
        games = listOf(mockGame1, mockGame2)
    )

    private val mockTicket2 = LottoTicket(
        ticketId = 2,
        round = 1146,
        registeredDate = Date(),
        isChecked = true,
        games = listOf(mockGame3)
    )

    private val mockTicket3 = LottoTicket(
        ticketId = 3,
        round = 1147,
        registeredDate = Date(),
        isChecked = false,
        games = listOf(mockGame2)
    )

    private val mockLottoResult = LottoResult(
        round = 1145,
        drawDate = Date(),
        numbers = listOf(1, 2, 3, 4, 5, 6),
        bonusNumber = 7,
        firstPrize = FirstPrizeInfo(
            winAmount = 2_500_000_000,
            winnerCount = 12,
            totalSalesAmount = 125_000_000_000
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getLottoTicketsUseCase = mockk()
        deleteLottoTicketUseCase = mockk()
        checkTicketWinningUseCase = mockk()
        getAllLottoResultsUseCase = mockk()

        // Default mock behaviors
        every { getLottoTicketsUseCase(any()) } returns flowOf(emptyList())
        every { getAllLottoResultsUseCase() } returns flowOf(emptyList())
        coEvery { deleteLottoTicketUseCase(any()) } returns Result.success(Unit)
        coEvery { checkTicketWinningUseCase(any()) } returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ==================== Initialization Tests ====================

    @Test
    fun `초기화 시 티켓 목록이 로드되고 currentRound가 설정된다`() = runTest {
        // Given
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(mockTicket1, mockTicket2))

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(2, state.tickets.size)
        assertEquals(mockTicket1, state.tickets[0])
        assertEquals(mockTicket2, state.tickets[1])
        assertTrue(state.currentRound > 0)
        assertFalse(state.isLoading)
    }

    @Test
    fun `초기화 시 로또 결과 목록이 로드된다`() = runTest {
        // Given
        every { getAllLottoResultsUseCase() } returns flowOf(listOf(mockLottoResult))

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(1, state.lottoResults.size)
        assertEquals(mockLottoResult, state.lottoResults[0])
    }

    // ==================== Refresh Event Tests ====================

    @Test
    fun `Refresh 이벤트 시 isRefreshing이 true가 되고 완료 후 false가 된다`() = runTest {
        // Given
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(mockTicket1))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.state.test {
            val initialState = awaitItem()
            assertFalse(initialState.isRefreshing)

            viewModel.onEvent(MyLottoEvent.Refresh)

            val refreshingState = awaitItem()
            assertTrue(refreshingState.isRefreshing)

            advanceUntilIdle()

            val completedState = expectMostRecentItem()
            assertFalse(completedState.isRefreshing)
        }
    }

    @Test
    fun `Refresh 이벤트 시 미확인 티켓들의 당첨이 확인된다`() = runTest {
        // Given
        // Use very low round numbers that are definitely in the past
        val tickets = listOf(
            mockTicket1.copy(round = 1, isChecked = false),  // Should be checked (old round)
            mockTicket2.copy(round = 2, isChecked = false),  // Should be checked (old round)
            mockTicket3.copy(round = 9999, isChecked = false)  // Should NOT be checked (future round)
        )
        every { getLottoTicketsUseCase(any()) } returns flowOf(tickets)

        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(MyLottoEvent.Refresh)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { checkTicketWinningUseCase(any()) }
        coVerify { checkTicketWinningUseCase(tickets[0]) }
        coVerify { checkTicketWinningUseCase(tickets[1]) }
    }

    @Test
    fun `Refresh 이벤트 시 이미 확인된 티켓은 재확인하지 않는다`() = runTest {
        // Given
        val tickets = listOf(
            mockTicket1.copy(isChecked = true), // Already checked - should NOT be checked again
            mockTicket2.copy(isChecked = false) // Unchecked - should be checked
        )
        every { getLottoTicketsUseCase(any()) } returns flowOf(tickets)

        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(MyLottoEvent.Refresh)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { checkTicketWinningUseCase(any()) }
        coVerify { checkTicketWinningUseCase(tickets[1]) }
    }

    @Test
    fun `Refresh 완료 시 ShowSnackbar Effect가 발생한다`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onEvent(MyLottoEvent.Refresh)
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is MyLottoEffect.ShowSnackbar)
            assertEquals("새로고침 완료", (effect as MyLottoEffect.ShowSnackbar).message)
        }
    }

    // ==================== ChangeSortType Event Tests ====================

    @Test
    fun `ChangeSortType 이벤트 시 sortType이 업데이트된다`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(MyLottoEvent.ChangeSortType(TicketSortType.ROUND_DESC))
        advanceUntilIdle()

        // Then
        val state = viewModel.state.value
        assertEquals(TicketSortType.ROUND_DESC, state.sortType)
    }

    @Test
    fun `ChangeSortType 이벤트 시 새로운 sortType으로 티켓 목록이 다시 로드된다`() = runTest {
        // Given
        val ticketsSortedByDate = listOf(mockTicket1, mockTicket2)
        val ticketsSortedByRound = listOf(mockTicket2, mockTicket1)

        every { getLottoTicketsUseCase(TicketSortType.DEFAULT) } returns flowOf(ticketsSortedByDate)
        every { getLottoTicketsUseCase(TicketSortType.ROUND_DESC) } returns flowOf(ticketsSortedByRound)

        viewModel = createViewModel()
        advanceUntilIdle()

        // Verify initial state
        assertEquals(ticketsSortedByDate, viewModel.state.value.tickets)

        // When
        viewModel.state.test {
            skipItems(1) // Skip initial state

            viewModel.onEvent(MyLottoEvent.ChangeSortType(TicketSortType.ROUND_DESC))
            advanceUntilIdle()

            // Then
            val updatedState = expectMostRecentItem()
            assertEquals(TicketSortType.ROUND_DESC, updatedState.sortType)
            assertEquals(ticketsSortedByRound, updatedState.tickets)
        }
    }

    // ==================== DeleteTicket Event Tests ====================

    @Test
    fun `DeleteTicket 성공 시 TicketDeleted와 ShowSnackbar Effect가 발생한다`() = runTest {
        // Given
        val ticketId = 1L
        coEvery { deleteLottoTicketUseCase(ticketId) } returns Result.success(Unit)
        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onEvent(MyLottoEvent.DeleteTicket(ticketId))
            advanceUntilIdle()

            val effect1 = awaitItem()
            assertTrue(effect1 is MyLottoEffect.TicketDeleted)
            assertEquals(ticketId, (effect1 as MyLottoEffect.TicketDeleted).ticketId)

            val effect2 = awaitItem()
            assertTrue(effect2 is MyLottoEffect.ShowSnackbar)
            assertEquals("티켓이 삭제되었습니다", (effect2 as MyLottoEffect.ShowSnackbar).message)
        }
    }

    @Test
    fun `DeleteTicket 실패 시 ShowError Effect가 발생한다`() = runTest {
        // Given
        val ticketId = 1L
        val errorMessage = "티켓 삭제 실패"
        coEvery { deleteLottoTicketUseCase(ticketId) } returns Result.failure(Exception(errorMessage))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onEvent(MyLottoEvent.DeleteTicket(ticketId))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is MyLottoEffect.ShowError)
            assertEquals(errorMessage, (effect as MyLottoEffect.ShowError).message)
        }
    }

    @Test
    fun `DeleteTicket 호출 시 DeleteLottoTicketUseCase가 올바른 ticketId로 호출된다`() = runTest {
        // Given
        val ticketId = 123L
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(MyLottoEvent.DeleteTicket(ticketId))
        advanceUntilIdle()

        // Then
        coVerify { deleteLottoTicketUseCase(ticketId) }
    }

    // ==================== CheckWinning Event Tests ====================

    @Test
    fun `CheckWinning 성공 시 당첨 등수에 따른 ShowSnackbar Effect가 발생한다`() = runTest {
        // Given
        val ticket = mockTicket1.copy(
            ticketId = 1,
            games = listOf(
                mockGame1.copy(winningRank = 1), // 1st place
                mockGame2.copy(winningRank = 5)  // 5th place
            )
        )
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(ticket))
        coEvery { checkTicketWinningUseCase(ticket) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onEvent(MyLottoEvent.CheckWinning(1))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is MyLottoEffect.ShowSnackbar)
            assertEquals("1등 당첨!", (effect as MyLottoEffect.ShowSnackbar).message)
        }
    }

    @Test
    fun `CheckWinning 성공 시 당첨되지 않은 경우 당첨 확인 완료 메시지가 표시된다`() = runTest {
        // Given
        val ticket = mockTicket1.copy(
            ticketId = 1,
            games = listOf(
                mockGame1.copy(winningRank = 0), // No win
                mockGame2.copy(winningRank = 0)  // No win
            )
        )
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(ticket))
        coEvery { checkTicketWinningUseCase(ticket) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onEvent(MyLottoEvent.CheckWinning(1))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is MyLottoEffect.ShowSnackbar)
            assertEquals("당첨 확인 완료", (effect as MyLottoEffect.ShowSnackbar).message)
        }
    }

    @Test
    fun `CheckWinning 실패 시 ShowError Effect가 발생한다`() = runTest {
        // Given
        val ticket = mockTicket1
        val errorMessage = "당첨 확인 실패"
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(ticket))
        coEvery { checkTicketWinningUseCase(ticket) } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onEvent(MyLottoEvent.CheckWinning(ticket.ticketId))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is MyLottoEffect.ShowError)
            assertEquals(errorMessage, (effect as MyLottoEffect.ShowError).message)
        }
    }

    @Test
    fun `CheckWinning 호출 시 티켓을 찾을 수 없으면 UseCase가 호출되지 않는다`() = runTest {
        // Given
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(mockTicket1))
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.onEvent(MyLottoEvent.CheckWinning(999L)) // Non-existent ticketId
        advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { checkTicketWinningUseCase(any()) }
    }

    @Test
    fun `CheckWinning 시 여러 게임 중 가장 높은 등수를 표시한다`() = runTest {
        // Given
        val ticket = mockTicket1.copy(
            ticketId = 1,
            games = listOf(
                mockGame1.copy(winningRank = 3), // 3rd place
                mockGame2.copy(winningRank = 1), // 1st place (highest)
                mockGame3.copy(winningRank = 5)  // 5th place
            )
        )
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(ticket))
        coEvery { checkTicketWinningUseCase(ticket) } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        // When & Then
        viewModel.effect.test {
            viewModel.onEvent(MyLottoEvent.CheckWinning(1))
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is MyLottoEffect.ShowSnackbar)
            assertEquals("1등 당첨!", (effect as MyLottoEffect.ShowSnackbar).message)
        }
    }

    // ==================== ClearError Event Tests ====================

    @Test
    fun `ClearError 이벤트 시 error가 null이 된다`() = runTest {
        // Given - 티켓 로드 실패 시 에러가 설정됨
        val errorMessage = "티켓 로드 실패"
        every { getLottoTicketsUseCase(any()) } returns flow {
            throw Exception(errorMessage)
        }
        viewModel = createViewModel()
        advanceUntilIdle()

        // Verify error is set
        assertNotNull(viewModel.state.value.error)

        // When
        viewModel.onEvent(MyLottoEvent.ClearError)

        // Then
        val state = viewModel.state.value
        assertNull(state.error)
    }

    @Test
    fun `ClearError 이벤트 시 다른 상태는 유지된다`() = runTest {
        // Given
        every { getLottoTicketsUseCase(any()) } returns flowOf(listOf(mockTicket1))
        viewModel = createViewModel()
        advanceUntilIdle()

        val stateBeforeClear = viewModel.state.value

        // When
        viewModel.onEvent(MyLottoEvent.ClearError)

        // Then
        val stateAfterClear = viewModel.state.value
        assertEquals(stateBeforeClear.tickets, stateAfterClear.tickets)
        assertEquals(stateBeforeClear.currentRound, stateAfterClear.currentRound)
        assertEquals(stateBeforeClear.sortType, stateAfterClear.sortType)
        assertNull(stateAfterClear.error)
    }

    // ==================== Helper Functions ====================

    private fun createViewModel(): MyLottoViewModel {
        return MyLottoViewModel(
            getLottoTicketsUseCase = getLottoTicketsUseCase,
            deleteLottoTicketUseCase = deleteLottoTicketUseCase,
            checkTicketWinningUseCase = checkTicketWinningUseCase,
            getAllLottoResultsUseCase = getAllLottoResultsUseCase
        )
    }
}
