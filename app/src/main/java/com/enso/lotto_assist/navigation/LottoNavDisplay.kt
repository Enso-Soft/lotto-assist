package com.enso.lotto_assist.navigation

import android.view.WindowManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.enso.home.LottoResultEvent
import com.enso.home.LottoResultScreen
import com.enso.home.LottoResultViewModel
import com.enso.mylotto.MyLottoScreen
import com.enso.qrscan.QrScanScreen
import com.enso.qrscan.parser.LottoTicketInfo

/**
 * Navigation display for the Lotto Assist app with Toss-style tab animations.
 *
 * Implements crossfade with subtle scale animation (0.98f → 1.0f) for smooth tab transitions.
 * Each destination is mapped to its corresponding screen composable.
 *
 * Uses proper Compose state management with immutable back stack and callbacks for navigation.
 *
 * @param backStack The navigation back stack (list of NavKeys) - immutable
 * @param onNavigate Callback for navigation events (push new destination)
 * @param modifier Modifier for this composable
 * @param lottoResultViewModel Shared ViewModel for home screen and QR scan results
 * @param onBack Callback for back navigation
 */
@Composable
fun LottoNavDisplay(
    backStack: List<NavKey>,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    lottoResultViewModel: LottoResultViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        modifier = modifier,
        transitionSpec = {
            // Special handling for QR screen to avoid SurfaceView alpha issues
            when {
                // When exiting QR Scan screen - use slide down animation
                initialState is NavKey.QrScanScreen -> {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    ) togetherWith slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                // When entering QR Scan screen - use slide up animation
                targetState is NavKey.QrScanScreen -> {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(
                            durationMillis = 250,
                            easing = FastOutSlowInEasing
                        )
                    ) togetherWith fadeOut(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
                // Default Toss-style animation for tab transitions
                else -> {
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleIn(
                        initialScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) togetherWith fadeOut(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    ) + scaleOut(
                        targetScale = 0.98f,
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = FastOutSlowInEasing
                        )
                    )
                }
            }
        },
        entryProvider = { navKey ->
            NavEntry(navKey) {
                when (navKey) {
                    is NavKey.HomeScreen -> {
                        LottoResultScreen(
                            viewModel = lottoResultViewModel,
                            onQrScanClick = {
                                onNavigate(NavKey.QrScanScreen)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is NavKey.MyLottoScreen -> {
                        MyLottoScreen()
                    }

                    is NavKey.HistoryScreen -> {
                        ComingSoonScreen(
                            title = "히스토리",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is NavKey.StoresScreen -> {
                        ComingSoonScreen(
                            title = "판매점",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    is NavKey.QrScanScreen -> {
                        QrScanScreenWrapper(
                            onScanSuccess = { ticketInfo ->
                                // Pass scanned QR data back to home screen
                                lottoResultViewModel.onEvent(
                                    LottoResultEvent.SaveQrTickets(
                                        round = ticketInfo.round,
                                        games = ticketInfo.games.map { it.numbers },
                                        gameTypes = ticketInfo.games.map { it.isAuto }
                                    )
                                )
                                // Navigate back to home
                                onBack()
                            },
                            onBackClick = {
                                onBack()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    )
}

/**
 * Wrapper for QR scan screen that handles screen-on flag lifecycle.
 */
@Composable
private fun QrScanScreenWrapper(
    onScanSuccess: (LottoTicketInfo) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Handle FLAG_KEEP_SCREEN_ON lifecycle
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    QrScanScreen(
        onScanSuccess = onScanSuccess,
        onBackClick = onBackClick
    )
}

/**
 * Placeholder screen for features that are not yet implemented.
 */
@Composable
private fun ComingSoonScreen(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$title 기능은 곧 출시됩니다",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
