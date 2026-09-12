package com.vitta.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Vitta is designed as a single warm, cream-and-green light theme in the
 * mockup — there is no dark palette to reverse-engineer. [VittaDarkColorScheme]
 * below is a reasonable derived-dark variant (documented, not sourced from
 * the mockup) so the app still respects system dark mode instead of forcing
 * a jarring light screen; swap it out if/when a real dark mode is designed.
 */
private val VittaLightColorScheme = lightColorScheme(
    primary = VittaColors.GreenPrimary,
    onPrimary = VittaColors.Background,
    primaryContainer = VittaColors.SurfaceVariant,
    onPrimaryContainer = VittaColors.TextPrimary,
    secondary = VittaColors.OrangeAccent,
    onSecondary = VittaColors.Background,
    secondaryContainer = VittaColors.SurfaceMuted,
    onSecondaryContainer = VittaColors.TextPrimary,
    tertiary = VittaColors.GoldAccent,
    background = VittaColors.Background,
    onBackground = VittaColors.TextPrimary,
    surface = VittaColors.Surface,
    onSurface = VittaColors.TextPrimary,
    surfaceVariant = VittaColors.SurfaceVariant,
    onSurfaceVariant = VittaColors.TextSecondary,
    outline = VittaColors.BorderSubtle,
    outlineVariant = VittaColors.BorderMuted,
    error = VittaColors.Error,
    onError = VittaColors.Background
)

private val VittaDarkColorScheme = darkColorScheme(
    primary = VittaColors.GreenMid,
    onPrimary = VittaColors.TextPrimary,
    primaryContainer = VittaColors.GreenPressed,
    onPrimaryContainer = VittaColors.Background,
    secondary = VittaColors.GoldAccent,
    onSecondary = VittaColors.TextPrimary,
    background = VittaColors.TextPrimary,
    onBackground = VittaColors.Background,
    // Not part of the Vitta palette: a slightly lighter tone than
    // TextPrimary so dark-mode surfaces read as elevated above the background.
    surface = androidx.compose.ui.graphics.Color(0xFF243330),
    onSurface = VittaColors.Background,
    error = VittaColors.Error,
    onError = VittaColors.Background
)

/**
 * Applies the full Vitta design system (color, typography, shapes) to its
 * content. Every screen and preview should be wrapped in this instead of
 * the raw `MaterialTheme`.
 *
 * @param useDarkTheme whether to use the derived dark scheme; defaults to
 *   the system setting. Vitta has no designed dark mode yet — see the class
 *   doc above.
 */
@Composable
fun VittaTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) VittaDarkColorScheme else VittaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = VittaTypography,
        shapes = VittaMaterialShapes,
        content = content
    )
}
