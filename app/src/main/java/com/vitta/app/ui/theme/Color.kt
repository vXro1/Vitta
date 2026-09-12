package com.vitta.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Every color Vitta uses, extracted directly from the inline styles of the
 * original HTML mockup (`Vitta App.html`). Nothing here is invented — each
 * value below is annotated with where it came from so it can be re-verified
 * against the mockup. See README_COLORES.md for the full documented palette.
 *
 * Screens and components must reference these constants (or the semantic
 * roles in [VittaColorRoles]) — never inline a `Color(0xFF...)` literal.
 */
object VittaColors {

    // ---- Brand green (primary actions, active nav, filled progress) ----
    val GreenPrimary = Color(0xFF778661)   // buttons, active chip, progress fill
    val GreenHover = Color(0xFF66754F)     // button :hover
    val GreenPressed = Color(0xFF56633F)   // button :active
    val GreenMid = Color(0xFF9CAA86)       // secondary progress bars (day 3/4)
    val GreenSoft = Color(0xFFA9B893)      // tertiary progress fill
    val GreenPale = Color(0xFFC3CDB1)      // light fill / toast secondary text
    val GreenTrack = Color(0xFFD4DCC2)     // unfilled progress track

    // ---- Accent orange (streaks, points, brand highlight) ----
    val OrangeAccent = Color(0xFFA55729)   // streak flame text, link, time picker
    val OrangeAccentHover = Color(0xFF7E3F1C)
    val GoldAccent = Color(0xFFF1AA52)     // points badge circle, toast icon bg

    // ---- Neutral surfaces (cream / paper, never pure white or gray) ----
    val Background = Color(0xFFF9F4EE)     // screen background
    val Surface = Color(0xFFFFFCF8)        // elevated cards
    val SurfaceVariant = Color(0xFFEFE6D6) // soft chip / icon-bubble background
    val SurfaceMuted = Color(0xFFF4EFE4)   // secondary chip / stat tile background
    val SurfaceTint = Color(0xFFEEF0E3)    // faint green-tinted surface

    // ---- Borders ----
    val BorderSubtle = Color(0xFFDFD4C2)
    val BorderMuted = Color(0xFFE3D9C8)

    // ---- Text ----
    val TextPrimary = Color(0xFF2D403E)    // headings, primary body text
    val TextSecondary = Color(0xFF5C6A5B)  // supporting body text
    val TextMuted = Color(0xFF8A9283)      // captions, meta text
    val TextFaint = Color(0xFFA9A493)      // disabled / least-emphasis labels

    // ---- Overlays & feedback surfaces ----
    val ScrimBackdrop = Color(0xA32D403E)  // rgba(45,64,62,.62) modal backdrop
    val ToastSurface = Color(0xFF2D403E)   // dark toast/snackbar background
    val SelectionTint = Color(0x47A55729)  // rgba(165,87,41,.28) text selection

    // ---- Semantic status (not present as literal HTML colors; derived from
    //      the closest role already used for that meaning in the mockup —
    //      documented here since the source has no dedicated error palette) ----
    val Success = GreenPrimary
    val Warning = GoldAccent
    val Error = Color(0xFFB3453A)          // adapted from OrangeAccent's hue family
    val Info = OrangeAccent
}

/**
 * Semantic aliases over [VittaColors] so components read by role
 * ("primary button", "card surface") instead of by raw color name.
 */
object VittaColorRoles {
    val primary = VittaColors.GreenPrimary
    val onPrimary = VittaColors.Background
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

    val scrim = VittaColors.ScrimBackdrop
    val toastSurface = VittaColors.ToastSurface
    val toastOnSurface = VittaColors.Background
    val toastSecondaryText = VittaColors.GreenPale
}
