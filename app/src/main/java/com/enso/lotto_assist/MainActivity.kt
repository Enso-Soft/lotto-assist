package com.enso.lotto_assist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.enso.home.LottoResultEvent
import com.enso.home.LottoResultScreen
import com.enso.home.LottoResultViewModel
import com.enso.home.R
import com.enso.home.ui.theme.BackgroundLight
import com.enso.home.ui.theme.CardLight
import com.enso.mylotto.MyLottoScreen
import com.enso.qrscan.QrScanActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val qrScanLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data ?: return@registerForActivityResult
                val round = data.getIntExtra(QrScanActivity.EXTRA_ROUND, 0)
                val gameCount = data.getIntExtra(QrScanActivity.EXTRA_GAME_COUNT, 0)

                val games = (0 until gameCount).map { index ->
                    data.getIntegerArrayListExtra("${QrScanActivity.EXTRA_GAME_PREFIX}$index")
                        ?: emptyList<Int>()
                }

                val gameTypes = (0 until gameCount).map { index ->
                    data.getBooleanExtra("${QrScanActivity.EXTRA_GAME_TYPE_PREFIX}$index", true)
                }

                viewModel?.onEvent(LottoResultEvent.SaveQrTickets(round, games, gameTypes))
            }
        }

    private var viewModel: LottoResultViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                val vm: LottoResultViewModel = hiltViewModel()
                viewModel = vm

                MainScreen(
                    lottoResultViewModel = vm,
                    onQrScanClick = {
                        qrScanLauncher.launch(Intent(this, QrScanActivity::class.java))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    lottoResultViewModel: LottoResultViewModel = hiltViewModel(),
    onQrScanClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        },
        containerColor = BackgroundLight
    ) { paddingValues ->
        when (selectedTab) {
            0 -> LottoResultScreen(
                viewModel = lottoResultViewModel,
                onQrScanClick = onQrScanClick,
                modifier = Modifier.padding(paddingValues)
            )
            1 -> {
                Box(modifier = Modifier.padding(paddingValues)) {
                    MyLottoScreen()
                }
            }
            // 2, 3 - 히스토리, 판매점 탭 (추후 구현)
        }
    }
}

@Composable
private fun BottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = CardLight
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Default.Home, stringResource(R.string.home_nav_home)) },
            label = { Text(stringResource(R.string.home_nav_home), fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Default.List, stringResource(R.string.home_nav_my_lotto)) },
            label = { Text(stringResource(R.string.home_nav_my_lotto), fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = { Icon(Icons.Default.List, stringResource(R.string.home_nav_history)) },
            label = { Text(stringResource(R.string.home_nav_history), fontSize = 10.sp) }
        )
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = { Icon(Icons.Default.List, stringResource(R.string.home_nav_stores)) },
            label = { Text(stringResource(R.string.home_nav_stores), fontSize = 10.sp) }
        )
    }
}
