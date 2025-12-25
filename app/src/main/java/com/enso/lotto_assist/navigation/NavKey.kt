package com.enso.lotto_assist.navigation

import kotlinx.serialization.Serializable

/**
 * Navigation destinations for the Lotto Assist app.
 * These are type-safe navigation keys using kotlinx-serialization.
 */
@Serializable
sealed interface NavKey {
    /**
     * Home screen showing latest lotto results
     */
    @Serializable
    data object HomeScreen : NavKey

    /**
     * My Lotto screen showing user's saved tickets
     */
    @Serializable
    data object MyLottoScreen : NavKey

    /**
     * History screen showing past lotto results (Coming Soon)
     */
    @Serializable
    data object HistoryScreen : NavKey

    /**
     * Stores screen showing lotto retail locations (Coming Soon)
     */
    @Serializable
    data object StoresScreen : NavKey

    /**
     * QR scan screen for scanning lotto tickets
     */
    @Serializable
    data object QrScanScreen : NavKey

    /**
     * Manual input screen for entering lotto numbers
     */
    @Serializable
    data class ManualInputScreen(
        val currentRound: Int
    ) : NavKey
}
