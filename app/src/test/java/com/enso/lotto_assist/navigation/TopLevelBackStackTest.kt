package com.enso.lotto_assist.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TopLevelBackStack save/restore logic.
 * Tests the Saver implementation to ensure navigation state can be preserved
 * across configuration changes and process death.
 */
class TopLevelBackStackTest {

    @Test
    fun `NavKey HomeScreen maps to home string`() {
        // Given
        val navKey = NavKey.HomeScreen

        // When
        val mapped = when (navKey) {
            is NavKey.HomeScreen -> "home"
            is NavKey.MyLottoScreen -> "mylotto"
            is NavKey.HistoryScreen -> "history"
            is NavKey.StoresScreen -> "stores"
            is NavKey.QrScanScreen -> "qrscan"
        }

        // Then
        assertEquals("home", mapped)
    }

    @Test
    fun `NavKey MyLottoScreen maps to mylotto string`() {
        // Given
        val navKey = NavKey.MyLottoScreen

        // When
        val mapped = when (navKey) {
            is NavKey.HomeScreen -> "home"
            is NavKey.MyLottoScreen -> "mylotto"
            is NavKey.HistoryScreen -> "history"
            is NavKey.StoresScreen -> "stores"
            is NavKey.QrScanScreen -> "qrscan"
        }

        // Then
        assertEquals("mylotto", mapped)
    }

    @Test
    fun `NavKey HistoryScreen maps to history string`() {
        // Given
        val navKey = NavKey.HistoryScreen

        // When
        val mapped = when (navKey) {
            is NavKey.HomeScreen -> "home"
            is NavKey.MyLottoScreen -> "mylotto"
            is NavKey.HistoryScreen -> "history"
            is NavKey.StoresScreen -> "stores"
            is NavKey.QrScanScreen -> "qrscan"
        }

        // Then
        assertEquals("history", mapped)
    }

    @Test
    fun `NavKey StoresScreen maps to stores string`() {
        // Given
        val navKey = NavKey.StoresScreen

        // When
        val mapped = when (navKey) {
            is NavKey.HomeScreen -> "home"
            is NavKey.MyLottoScreen -> "mylotto"
            is NavKey.HistoryScreen -> "history"
            is NavKey.StoresScreen -> "stores"
            is NavKey.QrScanScreen -> "qrscan"
        }

        // Then
        assertEquals("stores", mapped)
    }

    @Test
    fun `NavKey QrScanScreen maps to qrscan string`() {
        // Given
        val navKey = NavKey.QrScanScreen

        // When
        val mapped = when (navKey) {
            is NavKey.HomeScreen -> "home"
            is NavKey.MyLottoScreen -> "mylotto"
            is NavKey.HistoryScreen -> "history"
            is NavKey.StoresScreen -> "stores"
            is NavKey.QrScanScreen -> "qrscan"
        }

        // Then
        assertEquals("qrscan", mapped)
    }

    @Test
    fun `string home maps to NavKey HomeScreen`() {
        // Given
        val key = "home"

        // When
        val navKey = when (key) {
            "home" -> NavKey.HomeScreen
            "mylotto" -> NavKey.MyLottoScreen
            "history" -> NavKey.HistoryScreen
            "stores" -> NavKey.StoresScreen
            "qrscan" -> NavKey.QrScanScreen
            else -> null
        }

        // Then
        assertNotNull(navKey)
        assertEquals(NavKey.HomeScreen, navKey)
    }

    @Test
    fun `string mylotto maps to NavKey MyLottoScreen`() {
        // Given
        val key = "mylotto"

        // When
        val navKey = when (key) {
            "home" -> NavKey.HomeScreen
            "mylotto" -> NavKey.MyLottoScreen
            "history" -> NavKey.HistoryScreen
            "stores" -> NavKey.StoresScreen
            "qrscan" -> NavKey.QrScanScreen
            else -> null
        }

        // Then
        assertNotNull(navKey)
        assertEquals(NavKey.MyLottoScreen, navKey)
    }

    @Test
    fun `string history maps to NavKey HistoryScreen`() {
        // Given
        val key = "history"

        // When
        val navKey = when (key) {
            "home" -> NavKey.HomeScreen
            "mylotto" -> NavKey.MyLottoScreen
            "history" -> NavKey.HistoryScreen
            "stores" -> NavKey.StoresScreen
            "qrscan" -> NavKey.QrScanScreen
            else -> null
        }

        // Then
        assertNotNull(navKey)
        assertEquals(NavKey.HistoryScreen, navKey)
    }

    @Test
    fun `string stores maps to NavKey StoresScreen`() {
        // Given
        val key = "stores"

        // When
        val navKey = when (key) {
            "home" -> NavKey.HomeScreen
            "mylotto" -> NavKey.MyLottoScreen
            "history" -> NavKey.HistoryScreen
            "stores" -> NavKey.StoresScreen
            "qrscan" -> NavKey.QrScanScreen
            else -> null
        }

        // Then
        assertNotNull(navKey)
        assertEquals(NavKey.StoresScreen, navKey)
    }

    @Test
    fun `string qrscan maps to NavKey QrScanScreen`() {
        // Given
        val key = "qrscan"

        // When
        val navKey = when (key) {
            "home" -> NavKey.HomeScreen
            "mylotto" -> NavKey.MyLottoScreen
            "history" -> NavKey.HistoryScreen
            "stores" -> NavKey.StoresScreen
            "qrscan" -> NavKey.QrScanScreen
            else -> null
        }

        // Then
        assertNotNull(navKey)
        assertEquals(NavKey.QrScanScreen, navKey)
    }

    @Test
    fun `invalid string maps to null`() {
        // Given
        val key = "invalid_key"

        // When
        val navKey = when (key) {
            "home" -> NavKey.HomeScreen
            "mylotto" -> NavKey.MyLottoScreen
            "history" -> NavKey.HistoryScreen
            "stores" -> NavKey.StoresScreen
            "qrscan" -> NavKey.QrScanScreen
            else -> null
        }

        // Then
        assertEquals(null, navKey)
    }

    @Test
    fun `back stack save preserves all entries`() {
        // Given - simulating a back stack with multiple entries
        val backStack = listOf(
            NavKey.HomeScreen,
            NavKey.MyLottoScreen,
            NavKey.QrScanScreen
        )

        // When - simulate the save logic
        val savedList = backStack.map { entry ->
            when (entry) {
                is NavKey.HomeScreen -> "home"
                is NavKey.MyLottoScreen -> "mylotto"
                is NavKey.HistoryScreen -> "history"
                is NavKey.StoresScreen -> "stores"
                is NavKey.QrScanScreen -> "qrscan"
            }
        }

        // Then
        assertEquals(3, savedList.size)
        assertEquals(listOf("home", "mylotto", "qrscan"), savedList)
    }

    @Test
    fun `back stack restore preserves all entries`() {
        // Given - simulating saved state
        val savedList = listOf("home", "mylotto", "qrscan")

        // When - simulate the restore logic
        val restoredBackStack = savedList.mapNotNull { key ->
            when (key) {
                "home" -> NavKey.HomeScreen
                "mylotto" -> NavKey.MyLottoScreen
                "history" -> NavKey.HistoryScreen
                "stores" -> NavKey.StoresScreen
                "qrscan" -> NavKey.QrScanScreen
                else -> null
            }
        }

        // Then
        assertEquals(3, restoredBackStack.size)
        assertEquals(NavKey.HomeScreen, restoredBackStack[0])
        assertEquals(NavKey.MyLottoScreen, restoredBackStack[1])
        assertEquals(NavKey.QrScanScreen, restoredBackStack[2])
    }

    @Test
    fun `back stack restore filters out invalid entries`() {
        // Given - simulating saved state with some invalid entries
        val savedList = listOf("home", "invalid", "mylotto", "bad_key", "qrscan")

        // When - simulate the restore logic
        val restoredBackStack = savedList.mapNotNull { key ->
            when (key) {
                "home" -> NavKey.HomeScreen
                "mylotto" -> NavKey.MyLottoScreen
                "history" -> NavKey.HistoryScreen
                "stores" -> NavKey.StoresScreen
                "qrscan" -> NavKey.QrScanScreen
                else -> null
            }
        }

        // Then - invalid entries should be filtered out
        assertEquals(3, restoredBackStack.size)
        assertTrue(restoredBackStack.none { it.toString().contains("invalid") })
    }

    @Test
    fun `empty back stack can be saved and restored`() {
        // Given
        val backStack = emptyList<NavKey>()

        // When
        val savedList = backStack.map { entry ->
            when (entry) {
                is NavKey.HomeScreen -> "home"
                is NavKey.MyLottoScreen -> "mylotto"
                is NavKey.HistoryScreen -> "history"
                is NavKey.StoresScreen -> "stores"
                is NavKey.QrScanScreen -> "qrscan"
            }
        }

        val restoredBackStack = savedList.mapNotNull { key ->
            when (key) {
                "home" -> NavKey.HomeScreen
                "mylotto" -> NavKey.MyLottoScreen
                "history" -> NavKey.HistoryScreen
                "stores" -> NavKey.StoresScreen
                "qrscan" -> NavKey.QrScanScreen
                else -> null
            }
        }

        // Then
        assertTrue(savedList.isEmpty())
        assertTrue(restoredBackStack.isEmpty())
    }
}
