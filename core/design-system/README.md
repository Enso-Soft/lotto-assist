# core:design-system

Material3 Design System module for Lotto Assist app.

## Overview

This module provides a centralized design system based on Material3, including:
- **Theme Tokens**: Colors, Typography, Shapes, Spacing, Elevation
- **Extended Semantic Colors**: Lotto ball colors, success/warning/info states
- **Reusable Components**: LottoBall, LottoCard, LottoButton

## Usage

### 1. Add Dependency

In your feature module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":core:design-system"))
}
```

### 2. Wrap Your App with LottoTheme

```kotlin
import com.enso.designsystem.theme.LottoTheme

@Composable
fun MyApp() {
    LottoTheme {
        // Your app content
    }
}
```

### 3. Access Theme Tokens

#### Material3 Tokens

```kotlin
import androidx.compose.material3.MaterialTheme

@Composable
fun MyScreen() {
    // Colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundColor = MaterialTheme.colorScheme.background

    // Typography
    Text(
        text = "Title",
        style = MaterialTheme.typography.titleLarge
    )

    // Shapes
    Card(
        shape = MaterialTheme.shapes.medium
    ) { /* ... */ }
}
```

#### Extended Lotto Tokens

```kotlin
import com.enso.designsystem.theme.LottoTheme

@Composable
fun MyScreen() {
    // Spacing
    val padding = LottoTheme.spacing.cardPadding
    val sectionGap = LottoTheme.spacing.sectionGap

    // Custom colors
    val successColor = LottoTheme.colors.success
    val ballColor = getLottoBallColor(7, LottoTheme.colors)

    // Number typography
    Text(
        text = "7",
        style = LottoTheme.numberTypography.lottoNumberLarge
    )
}
```

### 4. Use Components

#### LottoBall

```kotlin
import com.enso.designsystem.component.*

// Default size (40dp)
LottoBall(number = 7)

// Size variants
LottoBallTiny(number = 7)    // 24dp
LottoBallSmall(number = 7)   // 32dp
LottoBallMedium(number = 7)  // 40dp
LottoBallLarge(number = 7)   // 48dp

// Custom size
LottoBall(
    number = 7,
    size = 56.dp,
    textStyle = LottoTheme.numberTypography.lottoNumberLarge
)

// Display 6 numbers
Row {
    listOf(1, 15, 23, 31, 42).forEach { number ->
        LottoBall(
            number = number,
            modifier = Modifier.padding(horizontal = LottoTheme.spacing.xxs)
        )
    }
}
```

#### LottoCard

```kotlin
import com.enso.designsystem.component.LottoCard

// Basic card
LottoCard {
    Column(modifier = Modifier.padding(LottoTheme.spacing.cardPadding)) {
        Text("Card Title")
        Text("Card Content")
    }
}

// Clickable card
LottoCard(
    onClick = { /* Handle click */ }
) {
    // Content
}
```

#### LottoButton

```kotlin
import com.enso.designsystem.component.*

// Primary button
LottoButton(onClick = { /* ... */ }) {
    Text("Primary Action")
}

// Outlined button
LottoOutlinedButton(onClick = { /* ... */ }) {
    Text("Secondary Action")
}

// Text button
LottoTextButton(onClick = { /* ... */ }) {
    Text("Tertiary Action")
}

// Tonal button
LottoTonalButton(onClick = { /* ... */ }) {
    Text("Tonal Action")
}
```

## Design Tokens Reference

### Spacing

```kotlin
LottoTheme.spacing.none          // 0dp
LottoTheme.spacing.xxs           // 4dp
LottoTheme.spacing.xs            // 8dp
LottoTheme.spacing.sm            // 12dp
LottoTheme.spacing.md            // 16dp
LottoTheme.spacing.lg            // 20dp
LottoTheme.spacing.xl            // 24dp
LottoTheme.spacing.xxl           // 32dp
LottoTheme.spacing.xxxl          // 48dp
LottoTheme.spacing.xxxxl         // 64dp

// Semantic aliases
LottoTheme.spacing.screenHorizontalPadding  // 20dp
LottoTheme.spacing.screenVerticalPadding    // 24dp
LottoTheme.spacing.cardPadding              // 16dp
LottoTheme.spacing.sectionGap               // 32dp
LottoTheme.spacing.listItemSpacing          // 12dp
LottoTheme.spacing.ballSpacing              // 3dp
```

### Ball Colors

```kotlin
// Numbers 1-10: Yellow
LottoTheme.colors.ballYellow     // #FBC400

// Numbers 11-20: Blue
LottoTheme.colors.ballBlue       // #69C8F2

// Numbers 21-30: Red
LottoTheme.colors.ballRed        // #FF7272

// Numbers 31-40: Grey
LottoTheme.colors.ballGrey       // #B0B0B0

// Numbers 41-45: Green
LottoTheme.colors.ballGreen      // #B0D840

// Helper function
val color = getLottoBallColor(number, LottoTheme.colors)
```

### Semantic Colors

```kotlin
// Success (Winning)
LottoTheme.colors.success
LottoTheme.colors.onSuccess
LottoTheme.colors.successContainer
LottoTheme.colors.onSuccessContainer

// Warning
LottoTheme.colors.warning
LottoTheme.colors.onWarning
LottoTheme.colors.warningContainer
LottoTheme.colors.onWarningContainer

// Info
LottoTheme.colors.info
LottoTheme.colors.onInfo
LottoTheme.colors.infoContainer
LottoTheme.colors.onInfoContainer

// Text Hierarchy
LottoTheme.colors.textPrimary
LottoTheme.colors.textSecondary
LottoTheme.colors.textDisabled
LottoTheme.colors.textLink
```

### Elevation

```kotlin
LottoElevation.none      // 0dp
LottoElevation.low       // 1dp
LottoElevation.medium    // 2dp
LottoElevation.high      // 4dp
LottoElevation.higher    // 8dp
LottoElevation.highest   // 12dp
```

## Migration from feature/home Color.kt

| Old | New |
|-----|-----|
| `Primary` | `MaterialTheme.colorScheme.primary` |
| `BackgroundLight/Dark` | `MaterialTheme.colorScheme.background` |
| `CardLight/Dark` | `MaterialTheme.colorScheme.surface` |
| `TextMainLight/Dark` | `MaterialTheme.colorScheme.onSurface` or `LottoTheme.colors.textPrimary` |
| `TextSubLight/Dark` | `MaterialTheme.colorScheme.onSurfaceVariant` or `LottoTheme.colors.textSecondary` |
| `BallYellow/Blue/Red/Grey/Green` | `LottoTheme.colors.ballYellow`, etc. |
| `WinningGreen` | `LottoTheme.colors.success` |
| `LosingRed` | `MaterialTheme.colorScheme.error` |
| `getLottoBallColor(number)` | `getLottoBallColor(number, LottoTheme.colors)` |

## Preview Support

All components include comprehensive previews:
- ✅ Light/Dark modes
- ✅ Multiple device sizes (Portrait/Landscape)
- ✅ Small screens (320dp width)
- ✅ Font scaling (1.5x)

To view previews in Android Studio:
1. Open any component file (e.g., `LottoBall.kt`)
2. Click "Split" or "Design" tab
3. See all preview variants

## Build

```bash
./gradlew :core:design-system:build
```

## Files

```
core/design-system/
├── build.gradle.kts
└── src/main/java/com/enso/designsystem/
    ├── theme/
    │   ├── Color.kt              # Material3 ColorScheme (Light/Dark)
    │   ├── LottoColors.kt        # Extended semantic colors
    │   ├── Typography.kt         # Material3 Typography
    │   ├── Shape.kt              # Material3 Shapes
    │   ├── Spacing.kt            # 4dp grid spacing system
    │   ├── Elevation.kt          # Elevation tokens
    │   └── LottoTheme.kt         # Theme wrapper + CompositionLocal
    └── component/
        ├── LottoBall.kt          # Lotto ball components
        ├── LottoCard.kt          # Card wrapper
        └── LottoButton.kt        # Button variants
```

## Design Principles

1. **Material3 First**: All base tokens follow Material3 guidelines
2. **Light/Dark Support**: Full theme switching support
3. **Responsive**: Components handle all screen sizes gracefully
4. **Accessible**: WCAG AA contrast ratios, minimum 48dp touch targets
5. **Korean Market**: Toss-style UX with bold weights and generous spacing
6. **State Hoisting**: Components are stateless by default
7. **Modifier First**: All components accept Modifier as first optional parameter

## References

- [Material3 Design Guide](https://m3.material.io/)
- [Jetpack Compose Material3](https://developer.android.com/jetpack/compose/designsystems/material3)
- Design Token Specification: `/docs/DESIGN_SYSTEM_TOKEN_SPEC.md`
