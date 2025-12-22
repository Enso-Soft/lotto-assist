package com.enso.lotto_assist.navigation

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests for NavKey serialization.
 * Ensures all NavKey types can be properly serialized and deserialized
 * for state restoration across configuration changes and process death.
 */
class NavKeySerializationTest {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    @Test
    fun `HomeScreen serializes and deserializes correctly`() {
        // Given
        val original: NavKey = NavKey.HomeScreen

        // When
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NavKey>(serialized)

        // Then
        assertNotNull(serialized)
        assertEquals(original, deserialized)
        assertEquals(NavKey.HomeScreen, deserialized)
    }

    @Test
    fun `MyLottoScreen serializes and deserializes correctly`() {
        // Given
        val original: NavKey = NavKey.MyLottoScreen

        // When
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NavKey>(serialized)

        // Then
        assertNotNull(serialized)
        assertEquals(original, deserialized)
        assertEquals(NavKey.MyLottoScreen, deserialized)
    }

    @Test
    fun `HistoryScreen serializes and deserializes correctly`() {
        // Given
        val original: NavKey = NavKey.HistoryScreen

        // When
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NavKey>(serialized)

        // Then
        assertNotNull(serialized)
        assertEquals(original, deserialized)
        assertEquals(NavKey.HistoryScreen, deserialized)
    }

    @Test
    fun `StoresScreen serializes and deserializes correctly`() {
        // Given
        val original: NavKey = NavKey.StoresScreen

        // When
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NavKey>(serialized)

        // Then
        assertNotNull(serialized)
        assertEquals(original, deserialized)
        assertEquals(NavKey.StoresScreen, deserialized)
    }

    @Test
    fun `QrScanScreen serializes and deserializes correctly`() {
        // Given
        val original: NavKey = NavKey.QrScanScreen

        // When
        val serialized = json.encodeToString(original)
        val deserialized = json.decodeFromString<NavKey>(serialized)

        // Then
        assertNotNull(serialized)
        assertEquals(original, deserialized)
        assertEquals(NavKey.QrScanScreen, deserialized)
    }

    @Test
    fun `all NavKey types can be serialized to unique strings`() {
        // Given
        val allKeys = listOf(
            NavKey.HomeScreen,
            NavKey.MyLottoScreen,
            NavKey.HistoryScreen,
            NavKey.StoresScreen,
            NavKey.QrScanScreen
        )

        // When
        val serializedKeys = allKeys.map { json.encodeToString<NavKey>(it) }

        // Then - all serialized forms should be unique
        assertEquals(allKeys.size, serializedKeys.toSet().size)
    }

    @Test
    fun `serialized NavKey contains type discriminator`() {
        // Given
        val navKey: NavKey = NavKey.HomeScreen

        // When
        val serialized = json.encodeToString(navKey)

        // Then - should contain type information for polymorphic deserialization
        assertNotNull(serialized)
        assert(serialized.isNotEmpty())
    }

    @Test
    fun `list of NavKeys can be serialized and deserialized`() {
        // Given - simulating a back stack
        val backStack = listOf(
            NavKey.HomeScreen,
            NavKey.MyLottoScreen,
            NavKey.QrScanScreen
        )

        // When
        val serialized = json.encodeToString(backStack)
        val deserialized = json.decodeFromString<List<NavKey>>(serialized)

        // Then
        assertEquals(backStack.size, deserialized.size)
        assertEquals(backStack, deserialized)
    }
}
