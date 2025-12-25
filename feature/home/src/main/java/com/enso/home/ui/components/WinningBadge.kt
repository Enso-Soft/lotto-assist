package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enso.designsystem.theme.LocalLottoColors

@Composable
fun WinningBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val lottoColors = LocalLottoColors.current
    val winningGreen = lottoColors.success
    val losingRed = Color(0xFFF44336) // Material Red 500

    val (text, backgroundColor, textColor) = when (rank) {
        1 -> Triple("1등", winningGreen, Color.White)
        2 -> Triple("2등", winningGreen, Color.White)
        3 -> Triple("3등", winningGreen, Color.White)
        4 -> Triple("4등", winningGreen.copy(alpha = 0.8f), Color.White)
        5 -> Triple("5등", winningGreen.copy(alpha = 0.6f), Color.White)
        else -> Triple("낙첨", losingRed.copy(alpha = 0.15f), losingRed)
    }

    Text(
        text = text,
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
    )
}
