package com.vitta.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Every color Vitta uses.
 *
 * Paleta renovada (2026-10): el verde oliva y los beige/grises desaturados
 * del maquetado original se reemplazaron por una paleta fresca y viva
 * (verde esmeralda, naranja, ámbar y fondos blanco-menta). Los NOMBRES de
 * las constantes se mantienen para que todas las pantallas hereden el
 * cambio sin tocar cada una. Los contrastes de texto se verificaron con la
 * fórmula WCAG (ver README_COLORES.md).
 *
 * Screens and components must reference these constants (or the semantic
 * roles in [VittaColorRoles]) — never inline a `Color(0xFF...)` literal.
 */
object VittaColors {

    // ---- Brand green (primary actions, active nav, filled progress) ----
    val GreenPrimary = Color(0xFF0A8754)   // buttons, active chip, progress fill (4.6:1 con blanco)
    val GreenHover = Color(0xFF08774A)     // button hover
    val GreenPressed = Color(0xFF066B42)   // button pressed / texto verde sobre fondos claros
    val GreenMid = Color(0xFF2FB67C)       // secondary progress bars
    val GreenSoft = Color(0xFF6ED3A3)      // tertiary progress fill
    val GreenPale = Color(0xFFB5EBD0)      // light fill / toast secondary text
    val GreenTrack = Color(0xFFD6F2E4)     // unfilled progress track

    // ---- Accents (streaks, points, highlights) ----
    val OrangeAccent = Color(0xFFC2410C)   // racha, enlaces (5.2:1 sobre blanco)
    val OrangeAccentHover = Color(0xFF9A3412)
    val GoldAccent = Color(0xFFFFB020)     // puntos, logros, destacados (usar con texto oscuro)
    val SkyAccent = Color(0xFF1C6FC4)      // recordatorios / hora
    val SkySoft = Color(0xFFE4F0FB)        // fondo suave para recordatorios

    // ---- Neutral surfaces (claros y frescos, sin beige grisáceo) ----
    val Background = Color(0xFFF6FBF8)     // screen background
    val Surface = Color(0xFFFFFFFF)        // elevated cards
    val SurfaceVariant = Color(0xFFE3F5EC) // icon-bubble / soft chip background
    val SurfaceMuted = Color(0xFFEEF5F1)   // secondary chip / stat tile background
    val SurfaceTint = Color(0xFFDDF3E7)    // selected / green-tinted surface

    // ---- Borders ----
    val BorderSubtle = Color(0xFFC9DDD3)
    val BorderMuted = Color(0xFFDCE9E2)

    // ---- Text ----
    val TextPrimary = Color(0xFF14312A)    // headings, primary body text (13:1)
    val TextSecondary = Color(0xFF3F5C54)  // supporting body text (7:1)
    val TextMuted = Color(0xFF5A746C)      // captions, meta text (≥4.5:1 sobre fondo)
    val TextFaint = Color(0xFF93A79F)      // disabled / decorative only

    // ---- Overlays & feedback surfaces ----
    val ScrimBackdrop = Color(0xA314312A)  // modal backdrop
    val ToastSurface = Color(0xFF14312A)   // dark toast/snackbar background
    val SelectionTint = Color(0x470A8754)  // text selection

    // ---- Semantic status ----
    val Success = GreenPrimary
    val Warning = GoldAccent
    val Error = Color(0xFFC62828)          // 5.6:1 sobre blanco
    val Info = SkyAccent
}

/**
 * Semantic aliases over [VittaColors] so components read by role
 * ("primary button", "card surface") instead of by raw color name.
 */
object VittaColorRoles {
    val primary = VittaColors.GreenPrimary
    val onPrimary = VittaColors.Surface
    val primaryHover = VittaColors.GreenHover
    val primaryPressed = VittaColors.GreenPressed

    val background = VittaColors.Background
    val surface = VittaColors.Surface
    val surfaceVariant = VittaColors.SurfaceVariant
    val surfaceMuted = VittaColors.SurfaceMuted
    val surfaceTint = VittaColors.SurfaceTint

    val border = VittaColors.BorderSubtle

    val textPrimary = VittaColors.TextPrimary
    val textSecondary = VittaColors.TextSecondary
    val textMuted = VittaColors.TextMuted
    val textDisabled = VittaColors.TextFaint

    val accent = VittaColors.OrangeAccent
    val gold = VittaColors.GoldAccent
    val reminder = VittaColors.SkyAccent

    val scrim = VittaColors.ScrimBackdrop
    val toastSurface = VittaColors.ToastSurface
    val toastOnSurface = VittaColors.Background
    val toastSecondaryText = VittaColors.GreenPale
}
