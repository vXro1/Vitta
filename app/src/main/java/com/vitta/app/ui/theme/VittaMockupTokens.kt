package com.vitta.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.vitta.app.R

/**
 * Nombres del maquetado (`vitta-app-maquetado--main/index.html`) para las
 * pantallas que lo reproducen al pie de la letra (Progreso, Logros, Perfil).
 * Todos apuntan a la paleta de marca de [VittaColors]; los pocos tonos que
 * solo existen en el maquetado (pistas, contornos, fichas ámbar) se derivan
 * de ella. Los textos usan tonos con contraste WCAG ≥ 4.5:1 sobre marfil.
 */
object Mk {
    // Texto
    val Ink = VittaColors.TextPrimary
    val Body = VittaColors.TextSecondary
    val Muted = VittaColors.TextMuted          // el maquetado usaba #8A9283 (≈3:1)
    val Faint = VittaColors.TextMuted          // solo se usa en textos que deben leerse
    val Chevron = VittaColors.TextFaint

    // Superficies
    val Screen = VittaColors.Background
    val Paper = VittaColors.Surface
    val Sand = VittaColors.SurfaceVariant
    val SandSoft = VittaColors.SurfaceMuted
    val Track = Color(0xFFE9E1D2)        // píldora bloqueada (derivado del marfil)
    val HabitTrack = Color(0xFFEDE4D4)   // pista de barra por hábito
    val Dashed = VittaColors.BorderSubtle
    val Divider = VittaColors.TextPrimary.copy(alpha = 0.07f)
    val Shadow = VittaColors.TextPrimary.copy(alpha = 0.06f)

    // Verde oliva
    val Olive = VittaColors.GreenPrimary
    val OliveMid = VittaColors.GreenMid
    val OliveLight = VittaColors.GreenPale
    val Heat1 = VittaColors.GreenTrack
    val Heat2 = VittaColors.GreenSoft
    val OliveTile = VittaColors.SurfaceTint
    val OliveDeep = VittaColors.GreenPressed
    val OliveText = VittaColors.TextSecondary
    val SandText = VittaColors.TextMuted

    // Ámbar / terracota
    val AmberTile = Color(0xFFFDEBCF)    // amarillo de marca muy claro
    val AmberDeep = VittaColors.OrangeAccentHover
    val AmberText = VittaColors.OrangeAccentHover
    val AmberInk = VittaColors.OrangeAccent
    val Amber = VittaColors.GoldAccent

    // Tarjeta oscura
    val Night = VittaColors.ToastSurface
    val NightText = VittaColors.Background
    val NightSub = VittaColors.GreenPale
    val NightTrack = VittaColors.Background.copy(alpha = 0.18f)

    val Danger = VittaColors.OrangeAccentHover
}

val Caprasimo = FontFamily(Font(R.font.caprasimo_regular, FontWeight.Normal))

val Figtree = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium),
    Font(R.font.figtree_semibold, FontWeight.SemiBold),
    Font(R.font.figtree_bold, FontWeight.Bold)
)

/** `font-family:Caprasimo` con el tamaño, interlineado y tracking del mockup. */
fun caprasimo(size: Float, lineHeight: Float = 1.1f, tracking: Float = 0f) = TextStyle(
    fontFamily = Caprasimo,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = (size * lineHeight).sp,
    letterSpacing = tracking.em
)

/** `font: <weight> <size>/<lineHeight> Figtree`. */
fun figtree(weight: Int, size: Float, lineHeight: Float = 1.3f, tracking: Float = 0f) = TextStyle(
    fontFamily = Figtree,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = (size * lineHeight).sp,
    letterSpacing = tracking.em
)

/** `box-shadow:0 2px 10px rgba(45,64,62,.06)` del mockup. */
fun Modifier.mockShadow(shape: Shape): Modifier =
    this.shadow(elevation = 3.dp, shape = shape, ambientColor = Mk.Shadow, spotColor = Mk.Shadow)
