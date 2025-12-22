package com.enso.lotto_assist.navigation

import androidx.compose.runtime.saveable.Saver
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Saver for top-level back stack navigation.
 * Uses kotlinx-serialization for type-safe NavKey serialization.
 *
 * This allows navigation state to persist across configuration changes (e.g., rotation).
 */
val TopLevelBackStackSaver = Saver<List<NavKey>, List<String>>(
    save = { backStack ->
        // Serialize each NavKey using kotlinx-serialization
        backStack.map { Json.encodeToString(it) }
    },
    restore = { savedList ->
        // Deserialize each NavKey
        savedList.map { Json.decodeFromString<NavKey>(it) }
    }
)
