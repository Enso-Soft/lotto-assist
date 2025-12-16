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
import com.enso.qrscan.QrScanActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : ComponentActivity() {

    private var onQrScanResult: ((round: Int, games: List<List<Int>>) -> Unit)? by mutableStateOf(null)

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

                onQrScanResult?.invoke(round, games)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                LottoResultScreen(
                    onQrScanClick = { callback ->
                        onQrScanResult = callback
                        qrScanLauncher.launch(Intent(this, QrScanActivity::class.java))
                    }
                )
            }
        }
    }
}