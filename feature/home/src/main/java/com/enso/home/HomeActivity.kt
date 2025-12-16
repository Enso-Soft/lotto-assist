package com.enso.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.enso.qrscan.QrScanActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

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

                // ViewModel에 저장 이벤트 전달
                viewModel?.onEvent(LottoResultEvent.SaveQrTickets(round, games))
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

                LottoResultScreen(
                    viewModel = vm,
                    onQrScanClick = {
                        qrScanLauncher.launch(Intent(this, QrScanActivity::class.java))
                    }
                )
            }
        }
    }
}