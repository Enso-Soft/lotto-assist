package com.enso.home

import app.cash.turbine.test
import com.enso.domain.model.FirstPrizeInfo
import com.enso.domain.model.LottoResult
import com.enso.domain.model.SyncResult
import com.enso.domain.model.WinningStatistics
import com.enso.domain.usecase.CheckTicketWinningUseCase
import com.enso.domain.usecase.DeleteLottoTicketUseCase
import com.enso.domain.usecase.GetAllLottoResultsUseCase
import com.enso.domain.usecase.GetLocalLottoResultCountUseCase
import com.enso.domain.usecase.GetLottoResultUseCase
import com.enso.domain.usecase.GetLottoTicketsUseCase
import com.enso.domain.usecase.GetWinningStatisticsUseCase
import com.enso.domain.usecase.SaveLottoTicketUseCase
import com.enso.domain.usecase.SyncLottoResultsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class LottoResultViewModelTest {

    private lateinit var viewModel: LottoResultViewModel
    private lateinit var getLottoResultUseCase: GetLottoResultUseCase
    private lateinit var getAllLottoResultsUseCase: GetAllLottoResultsUseCase
    private lateinit var syncLottoResultsUseCase: SyncLottoResultsUseCase
    private lateinit var getLottoTicketsUseCase: GetLottoTicketsUseCase
    private lateinit var saveLottoTicketUseCase: SaveLottoTicketUseCase
    private lateinit var deleteLottoTicketUseCase: DeleteLottoTicketUseCase
    private lateinit var checkTicketWinningUseCase: CheckTicketWinningUseCase
    private lateinit var getLocalLottoResultCountUseCase: GetLocalLottoResultCountUseCase
    private lateinit var getWinningStatisticsUseCase: GetWinningStatisticsUseCase

    private val testDispatcher = StandardTestDispatcher()

    private val mockLottoResult = LottoResult(
        round = 1145,
        drawDate = Date(),
        numbers = listOf(3, 12, 17, 23, 28, 41),
        bonusNumber = 35,
        firstPrize = FirstPrizeInfo(
            winAmount = 2_500_000_000,
            winnerCount = 12,
            totalSalesAmount = 125_000_000_000
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getLottoResultUseCase = mockk()
        getAllLottoResultsUseCase = mockk()
        syncLottoResultsUseCase = mockk()
        getLottoTicketsUseCase = mockk()
        saveLottoTicketUseCase = mockk()
        deleteLottoTicketUseCase = mockk()
        checkTicketWinningUseCase = mockk()
        getLocalLottoResultCountUseCase = mockk()
        getWinningStatisticsUseCase = mockk()

        coEvery { getAllLottoResultsUseCase() } returns flowOf(emptyList())
        coEvery { getWinningStatisticsUseCase() } returns flowOf(WinningStatistics.EMPTY)
        coEvery { getLottoTicketsUseCase(any()) } returns flowOf(emptyList())
        coEvery { syncLottoResultsUseCase(any()) } returns Result.success(
            SyncResult(successCount = 0, failedCount = 0, totalCount = 0)
        )
        coEvery { saveLottoTicketUseCase(any()) } returns Result.success(1L)
        coEvery { deleteLottoTicketUseCase(any()) } returns Result.success(Unit)
        coEvery { checkTicketWinningUseCase(any()) } returns Result.success(Unit)
        coEvery { getLocalLottoResultCountUseCase() } returns 1
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기화 시 로컬 결과가 있으면 선택 결과가 설정된다`() = runTest {
        coEvery { getAllLottoResultsUseCase() } returns flowOf(listOf(mockLottoResult))

        viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(mockLottoResult, state.selectedResult)
        assertNull(state.error)
        assertTrue(state.currentRound > 0)
    }

    @Test
    fun `로또 결과 로드 성공 시 상태가 업데이트된다`() = runTest {
        coEvery { getLottoResultUseCase(any()) } returns Result.success(mockLottoResult)

        viewModel = createViewModel()
        viewModel.onEvent(LottoResultEvent.LoadResult(mockLottoResult.round))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(mockLottoResult, state.selectedResult)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `로또 결과 로드 실패 시 에러 상태가 업데이트된다`() = runTest {
        val errorMessage = "네트워크 오류"
        coEvery { getLottoResultUseCase(any()) } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()
        viewModel.onEvent(LottoResultEvent.LoadResult(mockLottoResult.round))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.selectedResult)
        assertNotNull(state.error)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `로또 결과 로드 실패 시 에러 Effect를 발생시킨다`() = runTest {
        val errorMessage = "네트워크 오류"
        coEvery { getLottoResultUseCase(any()) } returns Result.failure(Exception(errorMessage))

        viewModel = createViewModel()

        viewModel.effect.test {
            viewModel.onEvent(LottoResultEvent.LoadResult(mockLottoResult.round))
            val effect = awaitItem()
            assertTrue(effect is LottoResultEffect.ShowError)
            assertEquals(errorMessage, (effect as LottoResultEffect.ShowError).message)
        }
    }

    @Test
    fun `특정 회차 로드 이벤트 시 해당 회차를 로드한다`() = runTest {
        val targetRound = 1100
        coEvery { getLottoResultUseCase(any()) } returns Result.success(
            mockLottoResult.copy(round = targetRound)
        )

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(LottoResultEvent.LoadResult(targetRound))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(targetRound, state.selectedResult?.round)
    }

    @Test
    fun `새로고침 이벤트 시 동기화가 수행된다`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onEvent(LottoResultEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isSyncing)
        assertTrue(state.currentRound > 0)
    }

    @Test
    fun `로딩 중에는 isLoading이 true다`() = runTest {
        coEvery { getLottoResultUseCase(any()) } returns Result.success(mockLottoResult)

        viewModel = createViewModel()

        viewModel.state.test {
            val initialState = awaitItem()
            assertFalse(initialState.isLoading)

            viewModel.onEvent(LottoResultEvent.LoadResult(mockLottoResult.round))

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            advanceUntilIdle()

            val loadedState = expectMostRecentItem()
            assertFalse(loadedState.isLoading)
        }
    }

    private fun createViewModel(): LottoResultViewModel {
        return LottoResultViewModel(
            getLottoResultUseCase = getLottoResultUseCase,
            getAllLottoResultsUseCase = getAllLottoResultsUseCase,
            syncLottoResultsUseCase = syncLottoResultsUseCase,
            getLottoTicketsUseCase = getLottoTicketsUseCase,
            saveLottoTicketUseCase = saveLottoTicketUseCase,
            deleteLottoTicketUseCase = deleteLottoTicketUseCase,
            checkTicketWinningUseCase = checkTicketWinningUseCase,
            getLocalLottoResultCountUseCase = getLocalLottoResultCountUseCase,
            getWinningStatisticsUseCase = getWinningStatisticsUseCase
        )
    }
}
