package com.vitta.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vitta.app.R

/**
 * Tipografía única de Vitta: **Nunito** (Google Fonts, licencia SIL Open
 * Font License 1.1), en 5 pesos empaquetados en `res/font/nunito_*.ttf`.
 * Toda la app —desde el Login— usa esta familia; los títulos usan los
 * pesos ExtraBold/Bold y el cuerpo Regular/Medium/SemiBold.
 */
val NunitoFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold)
)

/**
 * Named text styles mirroring the sizes actually used in the HTML mockup
 * (e.g. the 22px Nunito screen title, the 14px/1.5 Nunito body copy).
 * Prefer these over building ad-hoc `TextStyle`s in a screen or component.
 */
object VittaTextStyles {
    // Display — big Nunito numbers/moments (e.g. streak count, hero title)
    val displayLarge = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.015).sp
    )

    // Heading — screen/section titles ("Vitta te está esperando")
    val heading = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.015).sp
    )

    // Title — card/dialog titles ("Diez desayunos sencillos")
    val title = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 22.sp
    )

    // Subtitle — semibold Nunito row headlines ("Tomar agua")
    val subtitle = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 18.sp
    )

    // Body — default paragraph copy
    val body = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )

    // BodySmall — secondary/meta copy ("8 de 8 vasos")
    val bodySmall = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.5.sp,
        lineHeight = 17.sp
    )

    // Label — uppercase eyebrow labels ("BIENESTAR", "TU SALDO")
    val label = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 11.sp,
        letterSpacing = 0.08.sp
    )

    // Caption — smallest supporting text
    val caption = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    // Button — pill button label
    val button = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.5.sp,
        lineHeight = 18.sp
    )

    // ButtonDisplay — Nunito CTA label ("Guardar hábito")
    val buttonDisplay = TextStyle(
        fontFamily = NunitoFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 17.sp,
        lineHeight = 20.sp
    )
}

/**
 * Material 3 [Typography] built from [VittaTextStyles] so any stock M3
 * component (e.g. default TopAppBar title) also picks up Vitta's fonts
 * instead of the Material default.
 */
private val MaterialDefaults = Typography()

val VittaTypography = Typography(
    displayLarge = VittaTextStyles.displayLarge,
    // Los estilos que Vitta no define también usan Nunito (si no, los
    // componentes de Material —selector de hora, chips, diálogos— caían en Roboto).
    displayMedium = MaterialDefaults.displayMedium.copy(fontFamily = NunitoFamily, fontWeight = FontWeight.ExtraBold),
    displaySmall = MaterialDefaults.displaySmall.copy(fontFamily = NunitoFamily, fontWeight = FontWeight.ExtraBold),
    headlineSmall = MaterialDefaults.headlineSmall.copy(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold),
    titleSmall = MaterialDefaults.titleSmall.copy(fontFamily = NunitoFamily, fontWeight = FontWeight.SemiBold),
    bodySmall = MaterialDefaults.bodySmall.copy(fontFamily = NunitoFamily),
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
