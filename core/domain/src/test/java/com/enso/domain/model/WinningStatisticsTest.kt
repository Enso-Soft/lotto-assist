package com.enso.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WinningStatisticsTest {

    // region hasData Tests

    @Test
    fun `hasData는 checkedGamesCount가 0보다 크면 true다`() {
        // Given
        val statistics = createStatistics(checkedGamesCount = 1)

        // When & Then
        assertTrue(statistics.hasData)
    }

    @Test
    fun `hasData는 checkedGamesCount가 0이면 false다`() {
        // Given
        val statistics = createStatistics(checkedGamesCount = 0)

        // When & Then
        assertFalse(statistics.hasData)
    }

    @Test
    fun `hasData는 checkedGamesCount가 큰 값이어도 true다`() {
        // Given
        val statistics = createStatistics(checkedGamesCount = 1000)

        // When & Then
        assertTrue(statistics.hasData)
    }

    // endregion

    // region hasWins Tests

    @Test
    fun `hasWins는 winningGamesCount가 0보다 크면 true다`() {
        // Given
        val statistics = createStatistics(winningGamesCount = 1)

        // When & Then
        assertTrue(statistics.hasWins)
    }

    @Test
    fun `hasWins는 winningGamesCount가 0이면 false다`() {
        // Given
        val statistics = createStatistics(winningGamesCount = 0)

        // When & Then
        assertFalse(statistics.hasWins)
    }

    @Test
    fun `hasWins는 winningGamesCount가 큰 값이어도 true다`() {
        // Given
        val statistics = createStatistics(winningGamesCount = 500)

        // When & Then
        assertTrue(statistics.hasWins)
    }

    // endregion

    // region formattedWinningRate Tests

    @Test
    fun `formattedWinningRate는 소수점 첫째자리까지 포맷된다`() {
        // Given
        val statistics = createStatistics(winningRate = 12.567f)

        // When
        val formatted = statistics.formattedWinningRate

        // Then
        assertEquals("12.6%", formatted)
    }

    @Test
    fun `formattedWinningRate는 정수 값도 소수점으로 표시한다`() {
        // Given
        val statistics = createStatistics(winningRate = 50.0f)

        // When
        val formatted = statistics.formattedWinningRate

        // Then
        assertEquals("50.0%", formatted)
    }

    @Test
    fun `formattedWinningRate는 0일 때 0점0%를 반환한다`() {
        // Given
        val statistics = createStatistics(winningRate = 0f)

        // When
        val formatted = statistics.formattedWinningRate

        // Then
        assertEquals("0.0%", formatted)
    }

    @Test
    fun `formattedWinningRate는 100일 때 100점0%를 반환한다`() {
        // Given
        val statistics = createStatistics(winningRate = 100f)

        // When
        val formatted = statistics.formattedWinningRate

        // Then
        assertEquals("100.0%", formatted)
    }

    @Test
    fun `formattedWinningRate는 소수점 둘째자리에서 반올림한다`() {
        // Given: Float 정밀도 문제를 피하기 위해 명확하게 반올림되는 값 사용
        val statisticsRoundUp = createStatistics(winningRate = 33.36f)    // .36은 확실히 .4로 반올림
        val statisticsRoundDown = createStatistics(winningRate = 33.32f)  // .32는 확실히 .3으로 내림

        // When & Then
        assertEquals("33.4%", statisticsRoundUp.formattedWinningRate)
        assertEquals("33.3%", statisticsRoundDown.formattedWinningRate)
    }

    @Test
    fun `formattedWinningRate는 US 로케일을 사용하여 점을 소수점으로 사용한다`() {
        // Given
        val statistics = createStatistics(winningRate = 12.5f)

        // When
        val formatted = statistics.formattedWinningRate

        // Then - 점(.)이 소수점으로 사용됨 (쉼표가 아님)
        assertTrue(formatted.contains("."))
        assertFalse(formatted.contains(","))
    }

    // endregion

    // region EMPTY Companion Object Tests

    @Test
    fun `EMPTY 객체의 totalGamesPlayed는 0이다`() {
        assertEquals(0, WinningStatistics.EMPTY.totalGamesPlayed)
    }

    @Test
    fun `EMPTY 객체의 checkedGamesCount는 0이다`() {
        assertEquals(0, WinningStatistics.EMPTY.checkedGamesCount)
    }

    @Test
    fun `EMPTY 객체의 winningGamesCount는 0이다`() {
        assertEquals(0, WinningStatistics.EMPTY.winningGamesCount)
    }

    @Test
    fun `EMPTY 객체의 winningRate는 0이다`() {
        assertEquals(0f, WinningStatistics.EMPTY.winningRate, 0.001f)
    }

    @Test
    fun `EMPTY 객체의 totalTickets는 0이다`() {
        assertEquals(0, WinningStatistics.EMPTY.totalTickets)
    }

    @Test
    fun `EMPTY 객체의 rankBreakdown은 1부터 5까지 모두 0이다`() {
        val rankBreakdown = WinningStatistics.EMPTY.rankBreakdown

        assertEquals(5, rankBreakdown.size)
        assertEquals(0, rankBreakdown[1])
        assertEquals(0, rankBreakdown[2])
        assertEquals(0, rankBreakdown[3])
        assertEquals(0, rankBreakdown[4])
        assertEquals(0, rankBreakdown[5])
    }

    @Test
    fun `EMPTY 객체의 hasData는 false다`() {
        assertFalse(WinningStatistics.EMPTY.hasData)
    }

    @Test
    fun `EMPTY 객체의 hasWins는 false다`() {
        assertFalse(WinningStatistics.EMPTY.hasWins)
    }

    @Test
    fun `EMPTY 객체의 formattedWinningRate는 0점0%다`() {
        assertEquals("0.0%", WinningStatistics.EMPTY.formattedWinningRate)
    }

    // endregion

    // region VALID_WINNING_RANKS Tests

    @Test
    fun `VALID_WINNING_RANKS는 1부터 5까지의 범위다`() {
        val validRanks = WinningStatistics.VALID_WINNING_RANKS

        assertEquals(1, validRanks.first)
        assertEquals(5, validRanks.last)
        assertTrue(1 in validRanks)
        assertTrue(5 in validRanks)
        assertFalse(0 in validRanks)
        assertFalse(6 in validRanks)
    }

    // endregion

    // region Data Class Equality Tests

    @Test
    fun `동일한 속성을 가진 두 WinningStatistics는 같다`() {
        // Given
        val stats1 = createStatistics(
            totalGamesPlayed = 10,
            checkedGamesCount = 8,
            winningGamesCount = 2,
            winningRate = 25.0f,
            totalTickets = 3
        )
        val stats2 = createStatistics(
            totalGamesPlayed = 10,
            checkedGamesCount = 8,
            winningGamesCount = 2,
            winningRate = 25.0f,
            totalTickets = 3
        )

        // When & Then
        assertEquals(stats1, stats2)
        assertEquals(stats1.hashCode(), stats2.hashCode())
    }

    // endregion

    // region Helper Functions

    private fun createStatistics(
        totalGamesPlayed: Int = 0,
        checkedGamesCount: Int = 0,
        winningGamesCount: Int = 0,
        winningRate: Float = 0f,
        rankBreakdown: Map<Int, Int> = WinningStatistics.VALID_WINNING_RANKS.associateWith { 0 },
        totalTickets: Int = 0
    ): WinningStatistics {
        return WinningStatistics(
            totalGamesPlayed = totalGamesPlayed,
            checkedGamesCount = checkedGamesCount,
            winningGamesCount = winningGamesCount,
            winningRate = winningRate,
            rankBreakdown = rankBreakdown,
            totalTickets = totalTickets
        )
    }

    // endregion
}
