package com.enso.designsystem.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Lotto Shapes
 * Material3 shapes with Toss-style corner radius
 */
internal val LottoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // Chips, badges
    small = RoundedCornerShape(12.dp),       // Small cards
    medium = RoundedCornerShape(16.dp),      // Cards
    large = RoundedCornerShape(20.dp),       // Large cards
    extraLarge = RoundedCornerShape(24.dp),  // Bottom sheets
)

/**
 * Extended app shapes
 */
@Immutable
object AppShapes {
    val Pill: Shape = CircleShape
    val Button: Shape = RoundedCornerShape(16.dp)
    val Card: Shape = RoundedCornerShape(16.dp)
    val CardLarge: Shape = RoundedCornerShape(24.dp)
    val Chip: Shape = RoundedCornerShape(8.dp)
    val Badge: Shape = RoundedCornerShape(12.dp)
    val BottomSheet: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val Dialog: Shape = RoundedCornerShape(24.dp)
}
