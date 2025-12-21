package com.enso.domain.usecase

import app.cash.turbine.test
import com.enso.domain.model.GameType
import com.enso.domain.model.LottoGame
import com.enso.domain.model.LottoTicket
import com.enso.domain.model.WinningStatistics
import com.enso.domain.repository.LottoTicketRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class GetWinningStatisticsUseCaseTest {

    private lateinit var repository: LottoTicketRepository
    private lateinit var useCase: GetWinningStatisticsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetWinningStatisticsUseCase(repository)
    }

    // region calculateStatistics Tests

    @Test
    fun `빈 티켓 리스트일 때 EMPTY 통계를 반환한다`() {
        // Given
        val emptyTickets = emptyList<LottoTicket>()

        // When
        val result = useCase.calculateStatistics(emptyTickets)

        // Then
        assertEquals(WinningStatistics.EMPTY, result)
        assertEquals(0, result.totalGamesPlayed)
        assertEquals(0, result.checkedGamesCount)
        assertEquals(0, result.winningGamesCount)
        assertEquals(0f, result.winningRate, 0.001f)
        assertEquals(0, result.totalTickets)
    }

    @Test
    fun `체크되지 않은 티켓만 있을 때 checkedGamesCount는 0이다`() {
        // Given
        val uncheckedTickets = listOf(
            createTicket(round = 1000, isChecked = false, games = createGames(3)),
            createTicket(round = 1001, isChecked = false, games = createGames(2))
        )

        // When
        val result = useCase.calculateStatistics(uncheckedTickets)

        // Then
        assertEquals(5, result.totalGamesPlayed)
        assertEquals(0, result.checkedGamesCount)
        assertEquals(0, result.winningGamesCount)
        assertEquals(0f, result.winningRate, 0.001f)
        assertEquals(2, result.totalTickets)
    }

    @Test
    fun `모든 게임이 낙첨일 때 winningGamesCount는 0이다`() {
        // Given
        val losingGames = listOf(
            createGame("A", winningRank = 0),
            createGame("B", winningRank = 0),
            createGame("C", winningRank = 0)
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = losingGames)
        )

        // When
        val result = useCase.calculateStatistics(tickets)

        // Then
        assertEquals(3, result.totalGamesPlayed)
        assertEquals(3, result.checkedGamesCount)
        assertEquals(0, result.winningGamesCount)
        assertEquals(0f, result.winningRate, 0.001f)
        assertFalse(result.hasWins)
    }

    @Test
    fun `당첨이 있을 때 정확한 통계를 계산한다`() {
        // Given
        val games1 = listOf(
            createGame("A", winningRank = 5),  // 5등 당첨
            createGame("B", winningRank = 0),  // 낙첨
            createGame("C", winningRank = 3)   // 3등 당첨
        )
        val games2 = listOf(
            createGame("A", winningRank = 0),  // 낙첨
            createGame("B", winningRank = 1)   // 1등 당첨
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = games1),
            createTicket(round = 1001, isChecked = true, games = games2)
        )

        // When
        val result = useCase.calculateStatistics(tickets)

        // Then
        assertEquals(5, result.totalGamesPlayed)
        assertEquals(5, result.checkedGamesCount)
        assertEquals(3, result.winningGamesCount)
        assertTrue(result.hasWins)
        assertEquals(2, result.totalTickets)
    }

    @Test
    fun `등수별 카운트가 정확하다`() {
        // Given
        val games = listOf(
            createGame("A", winningRank = 1),  // 1등
            createGame("B", winningRank = 3),  // 3등
            createGame("C", winningRank = 3),  // 3등
            createGame("D", winningRank = 5),  // 5등
            createGame("E", winningRank = 0)   // 낙첨
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = games)
        )

        // When
        val result = useCase.calculateStatistics(tickets)

        // Then
        assertEquals(1, result.rankBreakdown[1])
        assertEquals(0, result.rankBreakdown[2])
        assertEquals(2, result.rankBreakdown[3])
        assertEquals(0, result.rankBreakdown[4])
        assertEquals(1, result.rankBreakdown[5])
        assertEquals(4, result.winningGamesCount)
    }

    @Test
    fun `당첨률 계산이 정확하다`() {
        // Given: 10개 게임 중 2개 당첨 = 20%
        val games = listOf(
            createGame("A", winningRank = 5),  // 당첨
            createGame("B", winningRank = 0),
            createGame("C", winningRank = 0),
            createGame("D", winningRank = 0),
            createGame("E", winningRank = 3)   // 당첨
        )
        val games2 = listOf(
            createGame("A", winningRank = 0),
            createGame("B", winningRank = 0),
            createGame("C", winningRank = 0),
            createGame("D", winningRank = 0),
            createGame("E", winningRank = 0)
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = games),
            createTicket(round = 1001, isChecked = true, games = games2)
        )

        // When
        val result = useCase.calculateStatistics(tickets)

        // Then
        assertEquals(10, result.checkedGamesCount)
        assertEquals(2, result.winningGamesCount)
        assertEquals(20.0f, result.winningRate, 0.001f)
    }

    @Test
    fun `유효하지 않은 등수는 당첨으로 카운트되지 않는다`() {
        // Given: rank 0, 6, -1은 유효하지 않음 (유효 범위: 1-5)
        val games = listOf(
            createGame("A", winningRank = 0),   // 낙첨
            createGame("B", winningRank = 6),   // 유효하지 않음
            createGame("C", winningRank = -1),  // 유효하지 않음
            createGame("D", winningRank = 100)  // 유효하지 않음
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = games)
        )

        // When
        val result = useCase.calculateStatistics(tickets)

        // Then
        assertEquals(4, result.checkedGamesCount)
        assertEquals(0, result.winningGamesCount)
        assertEquals(0f, result.winningRate, 0.001f)
    }

    @Test
    fun `체크된 티켓과 체크되지 않은 티켓이 혼합된 경우 올바르게 계산한다`() {
        // Given
        val checkedGames = listOf(
            createGame("A", winningRank = 5),
            createGame("B", winningRank = 0)
        )
        val uncheckedGames = listOf(
            createGame("A", winningRank = 1),  // 체크 안됨 - 카운트 안됨
            createGame("B", winningRank = 2),  // 체크 안됨 - 카운트 안됨
            createGame("C", winningRank = 3)   // 체크 안됨 - 카운트 안됨
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = checkedGames),
            createTicket(round = 1001, isChecked = false, games = uncheckedGames)
        )

        // When
        val result = useCase.calculateStatistics(tickets)

        // Then
        assertEquals(5, result.totalGamesPlayed)
        assertEquals(2, result.checkedGamesCount)  // 체크된 티켓의 게임만
        assertEquals(1, result.winningGamesCount)  // 체크된 티켓의 당첨만
        assertEquals(50.0f, result.winningRate, 0.001f)  // 2개 중 1개 = 50%
    }

    @Test
    fun `경계값 등수가 올바르게 처리된다`() {
        // Given: 1등과 5등은 유효, 0과 6은 무효
        val games = listOf(
            createGame("A", winningRank = 1),  // 유효
            createGame("B", winningRank = 5),  // 유효
            createGame("C", winningRank = 0),  // 무효
            createGame("D", winningRank = 6)   // 무효
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = games)
        )

        // When
        val result = useCase.calculateStatistics(tickets)

        // Then
        assertEquals(2, result.winningGamesCount)
        assertEquals(1, result.rankBreakdown[1])
        assertEquals(1, result.rankBreakdown[5])
    }

    // endregion

    // region invoke() Flow Tests

    @Test
    fun `invoke가 Flow를 올바르게 반환한다`() = runTest {
        // Given
        val games = listOf(
            createGame("A", winningRank = 5),
            createGame("B", winningRank = 0)
        )
        val tickets = listOf(
            createTicket(round = 1000, isChecked = true, games = games)
        )
        every { repository.getAllTickets() } returns flowOf(tickets)

        // When & Then
        useCase().test {
            val result = awaitItem()

            assertEquals(2, result.totalGamesPlayed)
            assertEquals(2, result.checkedGamesCount)
            assertEquals(1, result.winningGamesCount)
            assertEquals(50.0f, result.winningRate, 0.001f)

            awaitComplete()
        }
    }

    @Test
    fun `티켓 데이터가 변경되면 새로운 통계가 발행된다`() = runTest {
        // Given
        val initialGames = listOf(createGame("A", winningRank = 0))
        val updatedGames = listOf(
            createGame("A", winningRank = 0),
            createGame("B", winningRank = 5)
        )

        val ticketFlow = kotlinx.coroutines.flow.flow {
            emit(listOf(createTicket(round = 1000, isChecked = true, games = initialGames)))
            emit(listOf(createTicket(round = 1000, isChecked = true, games = updatedGames)))
        }
        every { repository.getAllTickets() } returns ticketFlow

        // When & Then
        useCase().test {
            // First emission
            val first = awaitItem()
            assertEquals(1, first.checkedGamesCount)
            assertEquals(0, first.winningGamesCount)

            // Second emission
            val second = awaitItem()
            assertEquals(2, second.checkedGamesCount)
            assertEquals(1, second.winningGamesCount)

            awaitComplete()
        }
    }

    // endregion

    // region Helper Functions

    private fun createTicket(
        ticketId: Long = System.nanoTime(),
        round: Int,
        isChecked: Boolean,
        games: List<LottoGame>
    ): LottoTicket {
        return LottoTicket(
            ticketId = ticketId,
            round = round,
            registeredDate = Date(),
            isChecked = isChecked,
            games = games,
            qrUrl = null
        )
    }

    private fun createGame(
        label: String,
        winningRank: Int = 0,
        numbers: List<Int> = listOf(1, 2, 3, 4, 5, 6)
    ): LottoGame {
        return LottoGame(
            gameId = System.nanoTime(),
            gameLabel = label,
            numbers = numbers,
            gameType = GameType.AUTO,
            winningRank = winningRank
        )
    }

    private fun createGames(count: Int): List<LottoGame> {
        val labels = listOf("A", "B", "C", "D", "E")
        return (0 until count).map { index ->
            createGame(label = labels[index % labels.size])
        }
    }

    // endregion
}
