package com.vitta.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vitta.app.R

/**
 * Vitta's two typefaces, exactly as declared in the mockup's `@font-face`
 * rules:
 *  - "Caprasimo" (weight 400 only) for display headings and big numbers.
 *  - "Figtree" (400/500/600/700) for everything else.
 */
val CaprasimoFamily = FontFamily(
    Font(R.font.caprasimo_regular, FontWeight.Normal)
)

val FigtreeFamily = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
    Font(R.font.figtree_bold, FontWeight.Bold)
)

/**
 * Named text styles mirroring the sizes actually used in the HTML mockup
 * (e.g. the 22px Caprasimo screen title, the 14px/1.5 Figtree body copy).
 * Prefer these over building ad-hoc `TextStyle`s in a screen or component.
 */
object VittaTextStyles {
    // Display — big Caprasimo numbers/moments (e.g. streak count, hero title)
    val displayLarge = TextStyle(
        fontFamily = CaprasimoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.015).sp
    )

    // Heading — screen/section titles ("Vitta te está esperando")
    val heading = TextStyle(
        fontFamily = CaprasimoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.015).sp
    )

    // Title — card/dialog titles ("Diez desayunos sencillos")
    val title = TextStyle(
        fontFamily = CaprasimoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 19.sp,
        lineHeight = 22.sp
    )

    // Subtitle — semibold Figtree row headlines ("Tomar agua")
    val subtitle = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 18.sp
    )

    // Body — default paragraph copy
    val body = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )

    // BodySmall — secondary/meta copy ("8 de 8 vasos")
    val bodySmall = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 17.sp
    )

    // Label — uppercase eyebrow labels ("BIENESTAR", "TU SALDO")
    val label = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 11.sp,
        letterSpacing = 0.08.sp
    )

    // Caption — smallest supporting text
    val caption = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    // Button — pill button label
    val button = TextStyle(
        fontFamily = FigtreeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 18.sp
    )

    // ButtonDisplay — Caprasimo CTA label ("Guardar hábito")
    val buttonDisplay = TextStyle(
        fontFamily = CaprasimoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 20.sp
    )
}

/**
 * Material 3 [Typography] built from [VittaTextStyles] so any stock M3
 * component (e.g. default TopAppBar title) also picks up Vitta's fonts
 * instead of the Material default.
 */
val VittaTypography = Typography(
    displayLarge = VittaTextStyles.displayLarge,
    headlineLarge = VittaTextStyles.heading,
    headlineMedium = VittaTextStyles.heading,
    titleLarge = VittaTextStyles.title,
    titleMedium = VittaTextStyles.subtitle,
    bodyLarge = VittaTextStyles.body,
    bodyMedium = VittaTextStyles.bodySmall,
    labelLarge = VittaTextStyles.button,
    labelMedium = VittaTextStyles.label,
    labelSmall = VittaTextStyles.caption
)
