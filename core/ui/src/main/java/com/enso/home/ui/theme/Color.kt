package com.enso.home.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val Primary = Color(0xFF137FEC)

// Background Colors
val BackgroundLight = Color(0xFFF2F4F6)
val BackgroundDark = Color(0xFF101922)

// Card Colors
val CardLight = Color(0xFFFFFFFF)
val CardDark = Color(0xFF1B2631)

// Text Colors
val TextMainLight = Color(0xFF191F28)
val TextMainDark = Color(0xFFFFFFFF)
val TextSubLight = Color(0xFF8B95A1)
val TextSubDark = Color(0xFF92ADC9)

// Lotto Ball Colors
val BallYellow = Color(0xFFFBC400)
val BallBlue = Color(0xFF69C8F2)
val BallRed = Color(0xFFFF7272)
val BallGrey = Color(0xFFB0B0B0)
val BallGreen = Color(0xFFB0D840)

// Status Colors
val WinningGreen = Color(0xFF4CAF50)
val LosingRed = Color(0xFFF44336)

fun getLottoBallColor(number: Int): Color {
    return when (number) {
        in 1..10 -> BallYellow
        in 11..20 -> BallBlue
        in 21..30 -> BallRed
        in 31..40 -> BallGrey
        else -> BallGreen
    }
}
