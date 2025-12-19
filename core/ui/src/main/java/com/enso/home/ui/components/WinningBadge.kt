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
import com.enso.home.ui.theme.LosingRed
import com.enso.home.ui.theme.WinningGreen

@Composable
fun WinningBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val (text, backgroundColor, textColor) = when (rank) {
        1 -> Triple("1등", WinningGreen, Color.White)
        2 -> Triple("2등", WinningGreen, Color.White)
        3 -> Triple("3등", WinningGreen, Color.White)
        4 -> Triple("4등", WinningGreen.copy(alpha = 0.8f), Color.White)
        5 -> Triple("5등", WinningGreen.copy(alpha = 0.6f), Color.White)
        else -> Triple("낙첨", LosingRed.copy(alpha = 0.15f), LosingRed)
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
