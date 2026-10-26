package com.vitta.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Every color Vitta uses.
 *
 * Paleta de marca (especificación UX v2.0): verde oliva #778661, amarillo
 * #F1AA52, verde oscuro #2D403E, terracota #A55729 y marfil #F9F4EE, con los
 * tonos derivados del maquetado original. Contrastes calculados con la
 * fórmula WCAG 2.1 sobre el fondo marfil (ver README_COLORES.md).
 *
 * Screens and components must reference these constants (or the semantic
 * roles in [VittaColorRoles]) — never inline a `Color(0xFF...)` literal.
 */
object VittaColors {

    // ---- Brand green (primary actions, active nav, filled progress) ----
    val GreenPrimary = Color(0xFF778661)   // verde oliva: buttons, active chip, progress fill (3.9:1 con blanco: solo texto en negrita grande)
    val GreenHover = Color(0xFF66754F)     // button hover (5.0:1 con blanco)
    val GreenPressed = Color(0xFF56633F)   // button pressed / texto verde sobre fondos claros (5.9:1)
    val GreenMid = Color(0xFF9CAA86)       // secondary progress bars
    val GreenSoft = Color(0xFFA9B893)      // tertiary progress fill
    val GreenPale = Color(0xFFC3CDB1)      // light fill / toast secondary text
    val GreenTrack = Color(0xFFD4DCC2)     // unfilled progress track

    // ---- Accents (streaks, points, highlights) ----
    val OrangeAccent = Color(0xFFA55729)   // terracota: racha, enlaces, recordatorios (4.8:1)
    val OrangeAccentHover = Color(0xFF7E3F1C)
    val GoldAccent = Color(0xFFF1AA52)     // amarillo: puntos, logros (usar con texto oscuro, 5.5:1)
    val SkyAccent = OrangeAccent           // recordatorios / hora: la paleta de marca no tiene azul
    val SkySoft = Color(0xFFF6E7DC)        // fondo suave terracota para recordatorios

    // ---- Neutral surfaces (marfil / papel) ----
    val Background = Color(0xFFF9F4EE)     // screen background (marfil)
    val Surface = Color(0xFFFFFCF8)        // elevated cards
    val SurfaceVariant = Color(0xFFEFE6D6) // icon-bubble / soft chip background
    val SurfaceMuted = Color(0xFFF4EFE4)   // secondary chip / stat tile background
    val SurfaceTint = Color(0xFFEEF0E3)    // selected / green-tinted surface

    // ---- Borders ----
    val BorderSubtle = Color(0xFFDFD4C2)
    val BorderMuted = Color(0xFFE3D9C8)

    // ---- Text ----
    val TextPrimary = Color(0xFF2D403E)    // verde oscuro: headings, primary body text (10:1)
    val TextSecondary = Color(0xFF5C6A5B)  // supporting body text (5.2:1)
    val TextMuted = Color(0xFF66705F)      // captions, meta text (4.7:1; #8A9283 daba 3:1)
    val TextFaint = Color(0xFFA9A493)      // disabled / decorative only

    // ---- Overlays & feedback surfaces ----
    val ScrimBackdrop = Color(0xA32D403E)  // modal backdrop
    val ToastSurface = Color(0xFF2D403E)   // dark toast/snackbar background
    val SelectionTint = Color(0x47A55729)  // text selection

    // ---- Semantic status ----
    val Success = GreenPrimary
    val Warning = GoldAccent
    val Error = Color(0xFFB3453A)          // 5.0:1 sobre marfil
    val Info = OrangeAccent
}

/**
 * Semantic aliases over [VittaColors] so components read by role
 * ("primary button", "card surface") instead of by raw color name.
 */
object VittaColorRoles {
    val primary = VittaColors.GreenPrimary
    val onPrimary = VittaColors.Surface
    /** Fondo relleno que lleva texto blanco normal (botones, píldoras): 5.0:1, el oliva base solo da 3.9:1. */
    val primaryStrong = VittaColors.GreenHover
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

    val accent = VittaColors.GreenPressed
    val gold = VittaColors.GoldAccent
    val reminder = VittaColors.SkyAccent

    val scrim = VittaColors.ScrimBackdrop
    val toastSurface = VittaColors.ToastSurface
    val toastOnSurface = VittaColors.Background
    val toastSecondaryText = VittaColors.GreenPale
}
