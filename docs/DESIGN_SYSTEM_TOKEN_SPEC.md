# Design System Token Specification

> Phase 2: Design System - Token Definition
> Created: 2025-12-22
> Status: Specification Complete

## Overview

This document defines the design tokens for the `core:design-system` module. The design system follows Material3 guidelines while incorporating Toss-style UX principles:
- **Clear hierarchy**: Bold weights and size contrast
- **Generous spacing**: Comfortable breathing room
- **Soft corners**: Appropriate corner radius for friendly feel
- **High contrast**: Excellent readability

---

## Color Tokens

### Light Theme

#### Primary Palette
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| primary | #137FEC | 19, 127, 236 | Main CTA, links, active states |
| onPrimary | #FFFFFF | 255, 255, 255 | Text/icons on primary |
| primaryContainer | #D6E9FF | 214, 233, 255 | Backgrounds for primary elements |
| onPrimaryContainer | #001F3B | 0, 31, 59 | Text on primary container |

#### Secondary Palette
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| secondary | #526070 | 82, 96, 112 | Less prominent actions |
| onSecondary | #FFFFFF | 255, 255, 255 | Text on secondary |
| secondaryContainer | #D5E4F3 | 213, 228, 243 | Secondary backgrounds |
| onSecondaryContainer | #0F1D2A | 15, 29, 42 | Text on secondary container |

#### Tertiary Palette
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| tertiary | #6B5778 | 107, 87, 120 | Accents, badges |
| onTertiary | #FFFFFF | 255, 255, 255 | Text on tertiary |
| tertiaryContainer | #F3DAFF | 243, 218, 255 | Tertiary backgrounds |
| onTertiaryContainer | #251432 | 37, 20, 50 | Text on tertiary container |

#### Background & Surface
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| background | #F2F4F6 | 242, 244, 246 | App background |
| onBackground | #191F28 | 25, 31, 40 | Text on background |
| surface | #FFFFFF | 255, 255, 255 | Cards, sheets |
| onSurface | #191F28 | 25, 31, 40 | Text on surface |
| surfaceVariant | #E1E5E9 | 225, 229, 233 | Elevated surfaces |
| onSurfaceVariant | #44484D | 68, 72, 77 | Text on surface variant |
| surfaceTint | #137FEC | 19, 127, 236 | Surface tint color |
| inverseSurface | #2E3133 | 46, 49, 51 | Inverse surface |
| inverseOnSurface | #F0F0F3 | 240, 240, 243 | Text on inverse surface |
| inversePrimary | #A4C9FF | 164, 201, 255 | Inverse primary |

#### Outline Colors
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| outline | #757780 | 117, 119, 128 | Borders, dividers |
| outlineVariant | #C4C6CF | 196, 198, 207 | Subtle dividers |

#### Error Colors
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| error | #F44336 | 244, 67, 54 | Error states (losing) |
| onError | #FFFFFF | 255, 255, 255 | Text on error |
| errorContainer | #FFDAD6 | 255, 218, 214 | Error backgrounds |
| onErrorContainer | #410002 | 65, 0, 2 | Text on error container |

#### Scrim
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| scrim | #000000 | 0, 0, 0 | Modal overlay (typically 32% alpha) |

---

### Dark Theme

#### Primary Palette
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| primary | #A4C9FF | 164, 201, 255 | Brighter blue for dark |
| onPrimary | #00315C | 0, 49, 92 | Text on primary |
| primaryContainer | #004A87 | 0, 74, 135 | Primary container |
| onPrimaryContainer | #D3E4FF | 211, 228, 255 | Text on primary container |

#### Secondary Palette
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| secondary | #B9C8D9 | 185, 200, 217 | Muted secondary |
| onSecondary | #243240 | 36, 50, 64 | Text on secondary |
| secondaryContainer | #3A4857 | 58, 72, 87 | Secondary container |
| onSecondaryContainer | #D5E4F3 | 213, 228, 243 | Text on secondary container |

#### Tertiary Palette
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| tertiary | #D7BDE4 | 215, 189, 228 | Softer tertiary |
| onTertiary | #3C2947 | 60, 41, 71 | Text on tertiary |
| tertiaryContainer | #544060 | 84, 64, 96 | Tertiary container |
| onTertiaryContainer | #F3DAFF | 243, 218, 255 | Text on tertiary container |

#### Background & Surface
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| background | #101922 | 16, 25, 34 | App background |
| onBackground | #FFFFFF | 255, 255, 255 | Text on background |
| surface | #1B2631 | 27, 38, 49 | Cards, sheets |
| onSurface | #E3E3E3 | 227, 227, 227 | Text on surface |
| surfaceVariant | #44474E | 68, 71, 78 | Elevated surfaces |
| onSurfaceVariant | #C4C6CF | 196, 198, 207 | Text on surface variant |
| surfaceTint | #A4C9FF | 164, 201, 255 | Surface tint |
| inverseSurface | #E2E2E5 | 226, 226, 229 | Inverse surface |
| inverseOnSurface | #2E3133 | 46, 49, 51 | Text on inverse surface |
| inversePrimary | #137FEC | 19, 127, 236 | Inverse primary |

#### Outline Colors
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| outline | #8E9099 | 142, 144, 153 | Borders |
| outlineVariant | #44474E | 68, 71, 78 | Subtle dividers |

#### Error Colors
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| error | #FFB4AB | 255, 180, 171 | Softer error for dark |
| onError | #690005 | 105, 0, 5 | Text on error |
| errorContainer | #93000A | 147, 0, 10 | Error container |
| onErrorContainer | #FFDAD6 | 255, 218, 214 | Text on error container |

#### Scrim
| Token | Hex | RGB | Description |
|-------|-----|-----|-------------|
| scrim | #000000 | 0, 0, 0 | Modal overlay |

---

### Extended Semantic Colors

These are custom colors exposed via `LottoTheme.colors` (not part of standard M3 ColorScheme):

#### Success (Winning)
| Token | Light | Dark | Description |
|-------|-------|------|-------------|
| success | #4CAF50 | #9CCC9C | Winning state |
| onSuccess | #FFFFFF | #0F3815 | Text on success |
| successContainer | #D4EDDA | #1B5E20 | Success background |
| onSuccessContainer | #0F5323 | #C8E6C9 | Text on success container |

#### Warning
| Token | Light | Dark | Description |
|-------|-------|------|-------------|
| warning | #FFC107 | #FFCC80 | Warning state |
| onWarning | #000000 | #3D2E00 | Text on warning |
| warningContainer | #FFF8E1 | #5D4200 | Warning background |
| onWarningContainer | #3D2E00 | #FFECB3 | Text on warning container |

#### Info
| Token | Light | Dark | Description |
|-------|-------|------|-------------|
| info | #2196F3 | #90CAF9 | Informational |
| onInfo | #FFFFFF | #0D3C61 | Text on info |
| infoContainer | #E3F2FD | #1565C0 | Info background |
| onInfoContainer | #0D3C61 | #BBDEFB | Text on info container |

#### Text Hierarchy (Semantic aliases)
| Token | Light | Dark | Description |
|-------|-------|------|-------------|
| textPrimary | #191F28 | #FFFFFF | Main text |
| textSecondary | #8B95A1 | #92ADC9 | Secondary text |
| textDisabled | #B0B5BC | #6B7280 | Disabled text |
| textLink | #137FEC | #A4C9FF | Hyperlinks |

---

### Lotto Ball Colors

Ball colors are consistent across light/dark themes as they are domain-specific:

| Range | Token | Hex | RGB | Description |
|-------|-------|-----|-----|-------------|
| 1-10 | ballYellow | #FBC400 | 251, 196, 0 | Warm golden yellow |
| 11-20 | ballBlue | #69C8F2 | 105, 200, 242 | Sky blue |
| 21-30 | ballRed | #FF7272 | 255, 114, 114 | Coral red |
| 31-40 | ballGrey | #B0B0B0 | 176, 176, 176 | Neutral grey |
| 41-45 | ballGreen | #B0D840 | 176, 216, 64 | Lime green |

#### Ball Styling
| Token | Value | Description |
|-------|-------|-------------|
| ballTextColor | #FFFFFF | Always white |
| ballBorderColor | #FFFFFF @ 20% | White gloss effect |
| ballShadow | 2dp | Subtle 3D effect |
| ballUnmatchedOverlay | #D3D3D3 @ 30% | Greyed-out unmatched balls |

---

## Typography Tokens

Based on Material3 type scale with Toss-style boldness adjustments.

### Type Scale

| Token | Size | Line Height | Weight | Letter Spacing | Usage |
|-------|------|-------------|--------|----------------|-------|
| displayLarge | 57sp | 64sp | Normal (400) | -0.25sp | Hero numbers |
| displayMedium | 45sp | 52sp | Normal (400) | 0sp | Large headings |
| displaySmall | 36sp | 44sp | Normal (400) | 0sp | Section titles |
| headlineLarge | 32sp | 40sp | Normal (400) | 0sp | Screen titles |
| headlineMedium | 28sp | 36sp | Normal (400) | 0sp | Card headers |
| headlineSmall | 24sp | 32sp | Normal (400) | 0sp | Subsections |
| titleLarge | 22sp | 28sp | SemiBold (600) | 0sp | Widget titles |
| titleMedium | 16sp | 24sp | SemiBold (600) | 0.15sp | List headers |
| titleSmall | 14sp | 20sp | Medium (500) | 0.1sp | Small titles |
| bodyLarge | 16sp | 24sp | Normal (400) | 0.5sp | Main content |
| bodyMedium | 14sp | 20sp | Normal (400) | 0.25sp | Secondary text |
| bodySmall | 12sp | 16sp | Normal (400) | 0.4sp | Captions |
| labelLarge | 14sp | 20sp | Medium (500) | 0.1sp | Buttons |
| labelMedium | 12sp | 16sp | Medium (500) | 0.5sp | Chips, badges |
| labelSmall | 11sp | 16sp | Medium (500) | 0.5sp | Small labels |

### Lotto Number Typography (Custom Extension)

| Token | Size | Weight | Description |
|-------|------|--------|-------------|
| lottoNumberLarge | 18sp | Bold (700) | Large ball display |
| lottoNumberMedium | 14sp | Bold (700) | Medium ball display |
| lottoNumberSmall | 12sp | Bold (700) | Small ball display |
| lottoNumberTiny | 9sp | Bold (700) | Tiny ball display |

---

## Shape Tokens

Using `RoundedCornerShape` for all shapes.

| Token | Value | Usage |
|-------|-------|-------|
| extraSmall | 4dp | Badges, rank indicators |
| small | 8dp | Chips, small buttons |
| medium | 12dp | Standard cards, inputs |
| large | 16dp | Large cards, dialogs |
| extraLarge | 24dp | Bottom sheets, modals |
| full | CircleShape (50%) | Lotto balls, avatars, FABs |

### Usage Examples

```kotlin
// Badges
RoundedCornerShape(LottoShapes.extraSmall) // 4dp

// Cards
Card(shape = RoundedCornerShape(LottoShapes.medium)) // 12dp

// Bottom sheets
ModalBottomSheet(shape = RoundedCornerShape(topStart = LottoShapes.extraLarge, topEnd = LottoShapes.extraLarge))

// Lotto balls
Box(modifier = Modifier.clip(CircleShape))
```

---

## Spacing Tokens

Based on 4dp grid system, following Toss-style generous spacing.

| Token | Value | Usage |
|-------|-------|-------|
| none | 0dp | No spacing |
| xxs | 4dp | Tightest spacing (between ball numbers) |
| xs | 8dp | Compact elements (chip gaps) |
| sm | 12dp | Between related items |
| md | 16dp | Standard padding, card internal |
| lg | 20dp | Section gaps, screen horizontal padding |
| xl | 24dp | Large section gaps, screen vertical padding |
| xxl | 32dp | Major section dividers |
| xxxl | 48dp | Screen-level sections |
| xxxxl | 64dp | Large screen spacing |

### Semantic Spacing Aliases

| Token | Value | Description |
|-------|-------|-------------|
| screenHorizontalPadding | 20dp (lg) | Left/right screen padding |
| screenVerticalPadding | 24dp (xl) | Top/bottom screen padding |
| cardPadding | 16dp (md) | Internal card padding |
| sectionGap | 32dp (xxl) | Between major sections |
| listItemSpacing | 12dp (sm) | Between list items |
| ballSpacing | 3dp | Between lotto balls |

---

## Elevation Tokens

| Token | Value | Usage |
|-------|-------|-------|
| none | 0dp | Flat elements |
| low | 1dp | Subtle cards |
| medium | 2dp | Standard cards |
| high | 4dp | Floating elements |
| higher | 8dp | Bottom sheets, dialogs |
| highest | 12dp | Modals, overlays |

---

## Implementation Structure

```
core/design-system/
└── src/main/java/com/enso/designsystem/
    ├── theme/
    │   ├── Color.kt              // M3 color scheme definitions
    │   ├── LottoColors.kt        // Extended colors (balls, status)
    │   ├── Typography.kt         // Typography definitions
    │   ├── Shape.kt              // Shape definitions
    │   ├── Spacing.kt            // Spacing object
    │   ├── Elevation.kt          // Elevation definitions
    │   └── LottoTheme.kt         // CompositionLocal + Theme wrapper
    ├── component/
    │   ├── LottoBall.kt          // Ball composables (Tiny, Small, Medium, Large)
    │   ├── LottoCard.kt          // Card composables
    │   ├── LottoButton.kt        // Button composables
    │   └── WinningBadge.kt       // Winning rank badge
    └── foundation/
        ├── LottoIcon.kt          // Icon wrapper
        └── Surface.kt            // Surface extensions
```

---

## Theme Usage Example

```kotlin
@Composable
fun LottoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val lottoColors = if (darkTheme) DarkLottoColors else LightLottoColors

    CompositionLocalProvider(
        LocalLottoColors provides lottoColors,
        LocalLottoSpacing provides LottoSpacing,
        LocalLottoShapes provides LottoShapes,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LottoTypography,
            shapes = Shapes(
                extraSmall = RoundedCornerShape(4.dp),
                small = RoundedCornerShape(8.dp),
                medium = RoundedCornerShape(12.dp),
                large = RoundedCornerShape(16.dp),
                extraLarge = RoundedCornerShape(24.dp),
            ),
            content = content
        )
    }
}

// Access tokens
object LottoTheme {
    val colors: LottoColors
        @Composable
        get() = LocalLottoColors.current

    val spacing: LottoSpacing
        @Composable
        get() = LocalLottoSpacing.current

    val shapes: LottoShapes
        @Composable
        get() = LocalLottoShapes.current
}
```

---

## Migration Notes

### From Current Color.kt

| Old | New (M3) | New (Extended) |
|-----|----------|----------------|
| Primary | colorScheme.primary | - |
| BackgroundLight/Dark | colorScheme.background | - |
| CardLight/Dark | colorScheme.surface | - |
| TextMainLight/Dark | colorScheme.onSurface | LottoTheme.colors.textPrimary |
| TextSubLight/Dark | colorScheme.onSurfaceVariant | LottoTheme.colors.textSecondary |
| BallYellow/Blue/Red/Grey/Green | - | LottoTheme.colors.ballYellow, etc. |
| WinningGreen | - | LottoTheme.colors.success |
| LosingRed | colorScheme.error | - |

### Feature Module Migration

1. Replace direct color references with `MaterialTheme.colorScheme.*`
2. Replace ball colors with `LottoTheme.colors.getBallColor(number)`
3. Replace `isSystemInDarkTheme()` checks with theme-aware colors
4. Remove local Color.kt files after migration

---

## Accessibility Checklist

- [ ] All text/background combinations meet WCAG AA contrast (4.5:1 for normal text)
- [ ] Touch targets minimum 48dp x 48dp
- [ ] Ball colors tested for color blindness (protanopia, deuteranopia, tritanopia)
- [ ] Error states have icon + color (not color alone)
- [ ] Focus indicators visible in both themes

---

## Next Steps

1. **ui-component-builder**: Implement token files in `core:design-system`
2. **ui-component-builder**: Create LottoBall, LottoCard, LottoButton components
3. **code-writer**: Migrate feature modules to use new design system
4. **test-engineer**: Create preview tests for all components
