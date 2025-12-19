package com.enso.qrscan

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
                    onExit = { finish() }
                )
            }
        }
    }

}
