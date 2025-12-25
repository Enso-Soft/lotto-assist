package com.enso.lotto_assist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enso.designsystem.theme.LottoTheme
import com.enso.home.LottoResultViewModel
import com.enso.home.R
import com.enso.lotto_assist.navigation.LottoNavDisplay
import com.enso.lotto_assist.navigation.NavKey
import com.enso.lotto_assist.navigation.TopLevelBackStackSaver
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable EdgeToEdge for full-screen experience
        enableEdgeToEdge()

        setContent {
            LottoTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(
    lottoResultViewModel: LottoResultViewModel = hiltViewModel()
) {
    // Use immutable state management for back stack
    var currentBackStack by rememberSaveable(stateSaver = TopLevelBackStackSaver) {
        mutableStateOf(listOf(NavKey.HomeScreen))
    }

    // Hide bottom nav for QR scan screen and Manual Input screen (full screen experience)
    val currentKey = currentBackStack.lastOrNull() ?: NavKey.HomeScreen
    val showBottomNav = currentKey !is NavKey.QrScanScreen && currentKey !is NavKey.ManualInputScreen
    val isQrScreen = currentKey is NavKey.QrScanScreen

    // Get status bar height
    val statusBarHeight = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    // NavigationBar height measurement
    var navBarHeight by remember { mutableStateOf(80.dp) }
    val density = LocalDensity.current

    // Animate top padding (QR screen: 0dp, normal screen: statusBar height)
    val animatedTopPadding by animateDpAsState(
        targetValue = if (isQrScreen) 0.dp else statusBarHeight,
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        label = "topPadding"
    )

    // Animate bottom padding (synchronized with NavigationBar visibility)
    val animatedBottomPadding by animateDpAsState(
        targetValue = if (showBottomNav) navBarHeight else 0.dp,
        animationSpec = tween(
            durationMillis = 200,
            delayMillis = if (showBottomNav) 100 else 0,
            easing = FastOutSlowInEasing
        ),
        label = "bottomPadding"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main content
        LottoNavDisplay(
            backStack = currentBackStack,
            onNavigate = { navKey ->
                // Immutable update: add new destination to back stack
                currentBackStack = currentBackStack + navKey
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = animatedTopPadding,
                    bottom = animatedBottomPadding
                ),
            lottoResultViewModel = lottoResultViewModel,
            onBack = {
                if (currentBackStack.size > 1) {
                    // Immutable update: remove last destination
                    currentBackStack = currentBackStack.dropLast(1)
                }
            }
        )

        // Overlay NavigationBar (animate entire bar including background)
        AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = 100,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = 100,
                    easing = FastOutSlowInEasing
                )
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = FastOutSlowInEasing
                )
            )
        ) {
            BottomNavigationBar(
                currentKey = currentKey,
                onTabSelected = { key ->
                    // Replace current destination with the selected tab
                    currentBackStack = listOf(key)
                },
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        // Only update when visible and has valid height
                        val height = coordinates.size.height
                        if (height > 0) {
                            navBarHeight = with(density) { height.toDp() }
                        }
                    }
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentKey: NavKey,
    onTabSelected: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        // Home Tab
        NavigationBarItem(
            selected = currentKey is NavKey.HomeScreen,
            onClick = { onTabSelected(NavKey.HomeScreen) },
            icon = {
                Icon(
                    imageVector = if (currentKey is NavKey.HomeScreen) {
                        Icons.Filled.Home
                    } else {
                        Icons.Outlined.Home
                    },
                    contentDescription = stringResource(R.string.home_nav_home)
                )
            },
            label = { Text(stringResource(R.string.home_nav_home), fontSize = 10.sp) }
        )

        // My Lotto Tab
        NavigationBarItem(
            selected = currentKey is NavKey.MyLottoScreen,
            onClick = { onTabSelected(NavKey.MyLottoScreen) },
            icon = {
                Icon(
                    imageVector = if (currentKey is NavKey.MyLottoScreen) {
                        Icons.Filled.List
                    } else {
                        Icons.Outlined.List
                    },
                    contentDescription = stringResource(R.string.home_nav_my_lotto)
                )
            },
            label = { Text(stringResource(R.string.home_nav_my_lotto), fontSize = 10.sp) }
        )

        // History Tab
        NavigationBarItem(
            selected = currentKey is NavKey.HistoryScreen,
            onClick = { onTabSelected(NavKey.HistoryScreen) },
            icon = {
                Icon(
                    imageVector = if (currentKey is NavKey.HistoryScreen) {
                        Icons.Filled.List
                    } else {
                        Icons.Outlined.List
                    },
                    contentDescription = stringResource(R.string.home_nav_history)
                )
            },
            label = { Text(stringResource(R.string.home_nav_history), fontSize = 10.sp) }
        )

        // Stores Tab
        NavigationBarItem(
            selected = currentKey is NavKey.StoresScreen,
            onClick = { onTabSelected(NavKey.StoresScreen) },
            icon = {
                Icon(
                    imageVector = if (currentKey is NavKey.StoresScreen) {
                        Icons.Filled.List
                    } else {
                        Icons.Outlined.List
                    },
                    contentDescription = stringResource(R.string.home_nav_stores)
                )
            },
            label = { Text(stringResource(R.string.home_nav_stores), fontSize = 10.sp) }
        )
    }
}
