package com.enso.qrscan

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QrScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                QrScanScreen(
                    onScanSuccess = { ticketInfo ->
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_ROUND, ticketInfo.round)
                            putExtra(EXTRA_GAME_COUNT, ticketInfo.games.size)

                            ticketInfo.games.forEachIndexed { index, numbers ->
                                putIntegerArrayListExtra(
                                    "$EXTRA_GAME_PREFIX$index",
                                    ArrayList(numbers)
                                )
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
    }
}
