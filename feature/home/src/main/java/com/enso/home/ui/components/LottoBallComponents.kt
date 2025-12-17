package com.enso.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enso.home.ui.theme.getLottoBallColor

@Composable
fun LottoBall(
    number: Int,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    fontSize: TextUnit = 18.sp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(getLottoBallColor(number))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Composable
fun SmallLottoBall(
    number: Int,
    modifier: Modifier = Modifier
) {
    LottoBall(
        number = number,
        modifier = modifier,
        size = 24.dp,
        fontSize = 11.sp
    )
}

@Composable
fun MediumLottoBall(
    number: Int,
    modifier: Modifier = Modifier
) {
    LottoBall(
        number = number,
        modifier = modifier,
        size = 32.dp,
        fontSize = 14.sp
    )
}

@Composable
fun TinyLottoBall(
    number: Int,
    modifier: Modifier = Modifier
) {
    LottoBall(
        number = number,
        modifier = modifier,
        size = 18.dp,
        fontSize = 9.sp
    )
}

@Composable
fun HighlightedSmallLottoBall(
    number: Int,
    isMatched: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isMatched) {
        getLottoBallColor(number)
    } else {
        Color.LightGray.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = if (isMatched) 1.dp else 0.5.dp,
                color = if (isMatched) Color.White.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 11.sp,
                fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
                color = if (isMatched) Color.White else Color.DarkGray
            )
        )
    }
}
