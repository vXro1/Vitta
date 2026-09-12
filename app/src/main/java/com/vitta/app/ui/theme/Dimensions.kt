package com.vitta.app.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale consolidated from the gap/padding values seen throughout the
 * mockup (4, 6, 8, 9, 10, 12, 14, 16, 18, 20, 22, 24, 26, 30px, ...) onto a
 * regular 4dp-based scale. Values were rounded to the nearest step in this
 * scale rather than kept as one-off pixels for every component — documented
 * here rather than silently invented (see README_ESTILOS.md).
 */
object VittaSpacing {
    val None = 0.dp
    val Xxs = 4.dp
    val Xs = 6.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 20.dp
    val Xxl = 24.dp
    val Xxxl = 32.dp
}

/** Reusable component sizing: icon bubbles, buttons, mascot presets, etc. */
object VittaSizes {
    // Touch targets / buttons
    val ButtonHeightSmall = 48.dp
    val ButtonHeightMedium = 54.dp
    val ButtonHeightLarge = 56.dp
    val MinTouchTarget = 48.dp

    // Icon bubbles (the circular background behind a habit/reward icon)
    val IconBubbleSmall = 34.dp
    val IconBubbleMedium = 42.dp
    val IconBubbleLarge = 54.dp

    // Icon glyphs themselves
    val IconXs = 15.dp
    val IconSmall = 22.dp
    val IconMedium = 26.dp
    val IconLarge = 30.dp

    // Habit "glass"/day-tracker chips
    val TrackerChip = 34.dp

    // Mascot illustration presets (see VittaMascot.kt for MascotSize)
    val MascotSmall = 72.dp
    val MascotMedium = 118.dp
    val MascotLarge = 196.dp
    val MascotXl = 220.dp

    // Badges / insignias
    val BadgeMedium = 58.dp
    val BadgeLarge = 92.dp

    // Bottom navigation
    val BottomNavHeight = 64.dp
}

/** Elevation levels expressed as shadow-equivalent dp, matched to the mockup's box-shadow blur radii. */
object VittaElevation {
    val None: Dp = 0.dp
    val Card: Dp = 3.dp
    val CardLarge: Dp = 8.dp
    val Modal: Dp = 20.dp
    val Toast: Dp = 12.dp
}
