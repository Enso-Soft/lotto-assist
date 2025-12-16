package com.enso.home

import app.cash.turbine.test
import com.enso.domain.model.FirstPrizeInfo
import com.enso.domain.model.LottoResult
import com.enso.domain.usecase.GetLottoResultUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `초기화 시 최신 회차 로또 결과를 로드한다`() = runTest {
        coEvery { getLottoResultUseCase(any()) } returns Result.success(mockLottoResult)

        viewModel = LottoResultViewModel(getLottoResultUseCase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.lottoResult)
        assertNull(state.error)
        assertTrue(state.currentRound > 0)
    }

    @Test
    fun `로또 결과 로드 성공 시 상태가 업데이트된다`() = runTest {
        coEvery { getLottoResultUseCase(any()) } returns Result.success(mockLottoResult)

        viewModel = LottoResultViewModel(getLottoResultUseCase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(mockLottoResult, state.lottoResult)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `로또 결과 로드 실패 시 에러 상태가 업데이트된다`() = runTest {
        val errorMessage = "네트워크 오류"
        coEvery { getLottoResultUseCase(any()) } returns Result.failure(Exception(errorMessage))

        viewModel = LottoResultViewModel(getLottoResultUseCase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.lottoResult)
        assertNotNull(state.error)
        assertEquals(errorMessage, state.error)
    }

    @Test
    fun `로또 결과 로드 실패 시 에러 Effect를 발생시킨다`() = runTest {
        val errorMessage = "네트워크 오류"
        coEvery { getLottoResultUseCase(any()) } returns Result.failure(Exception(errorMessage))

        viewModel = LottoResultViewModel(getLottoResultUseCase)

        viewModel.effect.test {
            advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is LottoResultEffect.ShowError)
            assertEquals(errorMessage, (effect as LottoResultEffect.ShowError).message)
        }
    }

    @Test
    fun `특정 회차 로드 이벤트 시 해당 회차를 로드한다`() = runTest {
        val targetRound = 1100
        coEvery { getLottoResultUseCase(any()) } returns Result.success(mockLottoResult) andThen
            Result.success(mockLottoResult.copy(round = targetRound))

        viewModel = LottoResultViewModel(getLottoResultUseCase)
        advanceUntilIdle()

        viewModel.onEvent(LottoResultEvent.LoadResult(targetRound))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(targetRound, state.lottoResult?.round)
    }

    @Test
    fun `새로고침 이벤트 시 현재 회차를 다시 로드한다`() = runTest {
        coEvery { getLottoResultUseCase(any()) } returns Result.success(mockLottoResult)

        viewModel = LottoResultViewModel(getLottoResultUseCase)
        advanceUntilIdle()

        val currentRound = viewModel.state.value.currentRound

        viewModel.onEvent(LottoResultEvent.Refresh)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNotNull(state.lottoResult)
        assertEquals(currentRound, state.currentRound)
    }

    @Test
    fun `로딩 중에는 isLoading이 true다`() = runTest {
        coEvery { getLottoResultUseCase(any()) } returns Result.success(mockLottoResult)

        viewModel = LottoResultViewModel(getLottoResultUseCase)

        viewModel.state.test {
            // 초기 상태 (아직 로딩 시작 전)
            val initialState = awaitItem()

            // 로딩 시작 상태
            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            advanceUntilIdle()

            // 로딩 완료 상태
            val loadedState = expectMostRecentItem()
            assertFalse(loadedState.isLoading)
        }
    }
}
