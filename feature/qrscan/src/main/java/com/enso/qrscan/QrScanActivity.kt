package com.enso.qrscan

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QrScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge 활성화
        enableEdgeToEdge()

        // 화면 꺼짐 방지
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme {
                QrScanScreen(
                    onScanSuccess = { ticketInfo ->
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_ROUND, ticketInfo.round)
                            putExtra(EXTRA_GAME_COUNT, ticketInfo.games.size)

                            ticketInfo.games.forEachIndexed { index, gameInfo ->
                                putIntegerArrayListExtra(
                                    "$EXTRA_GAME_PREFIX$index",
                                    ArrayList(gameInfo.numbers)
                                )
                                putExtra("${EXTRA_GAME_TYPE_PREFIX}$index", gameInfo.isAuto)
                            }
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onBackClick = {
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_ROUND = "extra_round"
        const val EXTRA_GAME_COUNT = "extra_game_count"
        const val EXTRA_GAME_PREFIX = "extra_game_"
        const val EXTRA_GAME_TYPE_PREFIX = "extra_game_type_"
    }
}
