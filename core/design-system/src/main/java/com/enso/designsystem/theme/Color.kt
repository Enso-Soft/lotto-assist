package com.enso.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Light Theme Color Scheme
 * Material3 ColorScheme for light mode
 */
internal val LightColorScheme = lightColorScheme(
    // Primary Palette (Toss Blue)
    primary = Color(0xFF3182F6),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEAF2FF),
    onPrimaryContainer = Color(0xFF001B3D),

    // Secondary Palette
    secondary = Color(0xFF5A6C7D),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6EEF8),
    onSecondaryContainer = Color(0xFF13202B),

    // Tertiary Palette
    tertiary = Color(0xFF6B5778),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF3DAFF),
    onTertiaryContainer = Color(0xFF251432),

    // Background & Surface
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF191F28),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191F28),
    surfaceVariant = Color(0xFFF1F3F5),
    onSurfaceVariant = Color(0xFF6B7684),
    surfaceTint = Color(0xFF3182F6),
    inverseSurface = Color(0xFF2F343A),
    inverseOnSurface = Color(0xFFF5F6F8),
    inversePrimary = Color(0xFFA4C9FF),

    // Outline Colors
    outline = Color(0xFFDDE2E7),
    outlineVariant = Color(0xFFEEF1F4),

    // Error Colors
    error = Color(0xFFF44336),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Scrim
    scrim = Color(0xFF000000),
)

/**
 * Dark Theme Color Scheme
 * Material3 ColorScheme for dark mode
 */
internal val DarkColorScheme = darkColorScheme(
    // Primary Palette
    primary = Color(0xFFA4C9FF),
    onPrimary = Color(0xFF00315C),
    primaryContainer = Color(0xFF004A87),
    onPrimaryContainer = Color(0xFFD3E4FF),

    // Secondary Palette
    secondary = Color(0xFFB9C8D9),
    onSecondary = Color(0xFF243240),
    secondaryContainer = Color(0xFF3A4857),
    onSecondaryContainer = Color(0xFFD5E4F3),

    // Tertiary Palette
    tertiary = Color(0xFFD7BDE4),
    onTertiary = Color(0xFF3C2947),
    tertiaryContainer = Color(0xFF544060),
    onTertiaryContainer = Color(0xFFF3DAFF),

    // Background & Surface
    background = Color(0xFF0F1720),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1B2430),
    onSurface = Color(0xFFE3E3E3),
    surfaceVariant = Color(0xFF273140),
    onSurfaceVariant = Color(0xFF94A3B8),
    surfaceTint = Color(0xFFA4C9FF),
    inverseSurface = Color(0xFFE4E7EB),
    inverseOnSurface = Color(0xFF2B3138),
    inversePrimary = Color(0xFF3182F6),

    // Outline Colors
    outline = Color(0xFF3A4552),
    outlineVariant = Color(0xFF2A3441),

    // Error Colors
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Scrim
    scrim = Color(0xFF000000),
)
