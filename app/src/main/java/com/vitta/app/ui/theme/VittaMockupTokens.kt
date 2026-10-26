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
 * Colores y tipografía tomados literalmente del maquetado
 * (`vitta-app-maquetado--main/index.html`). Los usan las pantallas que
 * reproducen el mockup al pie de la letra (Yo y Progreso) sin cambiar el
 * tema global ni el resto de pantallas.
 */
object Mk {
    // Texto
    val Ink = Color(0xFF2D403E)          // títulos y texto principal
    val Body = Color(0xFF5C6A5B)         // párrafos / títulos de tarjeta
    val Muted = Color(0xFF8A9283)        // subtítulos, condiciones
    val Faint = Color(0xFFA9A493)        // etiquetas inactivas
    val Chevron = Color(0xFFB8B0A0)

    // Superficies
    val Screen = Color(0xFFF9F4EE)       // fondo de pantalla
    val Paper = Color(0xFFFFFCF8)        // tarjetas
    val Sand = Color(0xFFEFE6D6)         // avatar, selector, celda vacía
    val SandSoft = Color(0xFFF4EFE4)     // medalla bloqueada / detalle
    val Track = Color(0xFFE9E1D2)        // píldora bloqueada
    val HabitTrack = Color(0xFFEDE4D4)   // pista de barra por hábito
    val Dashed = Color(0xFFDDD2BF)       // contornos punteados (vacío / futuro)
    val Divider = Color(0x122D403E)      // rgba(45,64,62,.07)
    val Shadow = Color(0x0F2D403E)       // rgba(45,64,62,.06)

    // Verde oliva
    val Olive = Color(0xFF778661)        // íconos y barra completa
    val OliveMid = Color(0xFF9CAA86)
    val OliveLight = Color(0xFFC9D2B7)
    val Heat1 = Color(0xFFD4DCC2)        // mapa de calor, nivel 1
    val Heat2 = Color(0xFFA9B893)        // mapa de calor, nivel 2
    val OliveTile = Color(0xFFEEF0E3)
    val OliveDeep = Color(0xFF56633F)
    val OliveText = Color(0xFF6B7768)
    val SandText = Color(0xFF77826F)

    // Ámbar
    val AmberTile = Color(0xFFFDEBCF)
    val AmberDeep = Color(0xFF8C491A)
    val AmberText = Color(0xFF8C6B3E)
    val AmberInk = Color(0xFFA55729)
    val Amber = Color(0xFFF1AA52)

    // Tarjeta oscura
    val Night = Color(0xFF2D403E)
    val NightText = Color(0xFFF9F4EE)
    val NightSub = Color(0xFFC3CDB1)
    val NightTrack = Color(0x2EF9F4EE)   // rgba(249,244,238,.18)

    /** Cerrar sesión no existe en el mockup: se usa su marrón de advertencia más oscuro. */
    val Danger = Color(0xFF7E3F1C)
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
