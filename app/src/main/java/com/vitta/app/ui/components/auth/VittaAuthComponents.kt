package com.vitta.app.ui.components.auth

import android.provider.Settings
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.brand.VittaLogo
import com.vitta.app.ui.components.buttons.VittaBackButton
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Piezas compartidas por todo el flujo de autenticación (Login, Registro)
 * para que se vean como una sola experiencia: mismo fondo con burbujas,
 * mismo botón de regreso, mismo encabezado y mismos mensajes.
 *
 * Todo usa colores explícitos de la paleta clara de Vitta: estas pantallas
 * no dependen del esquema de Material, así el texto nunca sale claro sobre
 * fondo claro aunque el teléfono esté en modo oscuro.
 */

/** Ancho máximo del formulario: en tablets/horizontal no se estira de borde a borde. */
private val AuthMaxContentWidth = 440.dp

/**
 * Esqueleto de una pantalla de autenticación: fondo crema + burbujas
 * animadas, barra superior con regreso opcional y contenido centrado con
 * scroll (para pantallas pequeñas o con teclado abierto).
 */
@Composable
fun VittaAuthScaffold(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = "Volver",
    overlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
    ) {
        VittaAuthBubbles(Modifier.matchParentSize())

        CompositionLocalProvider(LocalContentColor provides VittaColorRoles.textPrimary) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                val minHeight = maxHeight
                // SpaceBetween + espaciador inferior de igual alto que la barra
                // superior: el formulario queda centrado cuando cabe y hace
                // scroll cuando no (pantallas pequeñas o teclado abierto).
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .heightIn(min = minHeight)
                        .padding(horizontal = VittaSpacing.Xxl, vertical = VittaSpacing.Lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = AuthMaxContentWidth)
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (onBack != null) {
                            VittaAuthBackButton(onClick = onBack, contentDescription = backContentDescription)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .widthIn(max = AuthMaxContentWidth)
                            .fillMaxWidth()
                            .padding(vertical = VittaSpacing.Lg),
                        content = content
                    )

                    Spacer(Modifier.height(48.dp))
                }
            }
        }

        overlay()
    }
}

/** Botón circular de regreso, mismo estilo en todas las pantallas del flujo. */
@Composable
fun VittaAuthBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Volver"
) = VittaBackButton(onClick = onClick, modifier = modifier, contentDescription = contentDescription)

/** Logo + título + subtítulo, la cabecera común de Login y Registro. */
@Composable
fun VittaAuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(VittaColorRoles.surface, CircleShape)
                .border(1.dp, VittaColorRoles.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            VittaLogo(size = 40.dp)
        }
        Spacer(Modifier.height(VittaSpacing.Xl))
        Text(
            text = title,
            style = VittaTextStyles.displayLarge,
            color = VittaColorRoles.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            text = subtitle,
            style = VittaTextStyles.body.copy(fontSize = VittaTextStyles.subtitle.fontSize),
            color = VittaColorRoles.textSecondary
        )
    }
}

/** Aviso general (p. ej. sin conexión) con fondo tenue, icono y texto legible. */
@Composable
fun VittaAuthErrorBanner(message: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColors.Error.copy(alpha = 0.08f), VittaShapes.small)
            .border(1.dp, VittaColors.Error.copy(alpha = 0.35f), VittaShapes.small)
            .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Md)
            .semantics { liveRegion = LiveRegionMode.Polite },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = VittaColors.Error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(VittaSpacing.Sm))
        Text(message, style = VittaTextStyles.bodySmall, color = VittaColors.Error)
    }
}

/** Pie con pregunta + enlace ("¿No tienes cuenta? Crea una"). */
@Composable
fun VittaAuthFooterLink(
    question: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(question, style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
        TextButton(
            onClick = onClick,
            modifier = Modifier.heightIn(min = 48.dp),
            contentPadding = PaddingValues(horizontal = VittaSpacing.Sm),
            colors = ButtonDefaults.textButtonColors(contentColor = VittaColorRoles.accent)
        ) {
            Text(action, style = VittaTextStyles.button)
        }
    }
}

// ---------------------------------------------------------------------------
// Requisitos de contraseña
// ---------------------------------------------------------------------------

/** Un requisito de la contraseña y si el texto actual ya lo cumple. */
data class VittaPasswordRequirement(val label: String, val met: Boolean)

/**
 * Lista visual de los requisitos de contraseña, evaluados sobre lo que
 * el usuario va escribiendo. Solo presenta las reglas; la validación que
 * bloquea el envío sigue viviendo en el ViewModel.
 */
fun vittaPasswordRequirements(password: String): List<VittaPasswordRequirement> = listOf(
    VittaPasswordRequirement("Mínimo 8 caracteres", password.length >= 8),
    VittaPasswordRequirement("Una letra mayúscula", password.any { it.isUpperCase() }),
    VittaPasswordRequirement("Una letra minúscula", password.any { it.isLowerCase() }),
    VittaPasswordRequirement("Un número", password.any { it.isDigit() }),
    VittaPasswordRequirement("Un carácter especial (!@#\$…)", password.any { !it.isLetterOrDigit() && !it.isWhitespace() })
)

@Composable
fun VittaPasswordRequirementsList(
    requirements: List<VittaPasswordRequirement>,
    modifier: Modifier = Modifier,
    title: String = "Tu contraseña debe tener:"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surfaceTint, VittaShapes.small)
            .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Md)
    ) {
        Text(title, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textPrimary)
        Spacer(Modifier.height(VittaSpacing.Xs))
        requirements.forEach { VittaRequirementRow(it.label, it.met) }
    }
}

/** Fila de checklist: check verde si se cumple, círculo vacío si falta. */
@Composable
fun VittaRequirementRow(label: String, met: Boolean, modifier: Modifier = Modifier) {
    val iconColor by animateColorAsState(
        if (met) VittaColorRoles.primaryPressed else VittaColorRoles.textSecondary,
        label = "requirementIcon"
    )
    val textColor by animateColorAsState(
        if (met) VittaColorRoles.textPrimary else VittaColorRoles.textSecondary,
        label = "requirementText"
    )
    Row(
        modifier = modifier
            .padding(vertical = 3.dp)
            .clearAndSetSemantics {
                contentDescription = "$label: ${if (met) "cumplido" else "pendiente"}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (met) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(VittaSpacing.Sm))
        Text(label, style = VittaTextStyles.bodySmall, color = textColor)
    }
}

// ---------------------------------------------------------------------------
// Burbujas animadas de fondo
// ---------------------------------------------------------------------------

private data class AuthBubble(
    val xFraction: Float,
    val yFraction: Float,
    val radius: Dp,
    val color: Color,
    val driftX: Dp,
    val driftY: Dp,
    /** Vueltas completas por ciclo: debe ser entero para que el bucle no "salte". */
    val speed: Int,
    val phase: Float
)

private val AuthBubbles = listOf(
    AuthBubble(1.02f, -0.02f, 130.dp, VittaColors.SurfaceVariant, 10.dp, 14.dp, 1, 0f),
    AuthBubble(-0.06f, 0.30f, 64.dp, VittaColors.SurfaceTint, 14.dp, 10.dp, 1, 1.6f),
    AuthBubble(0.90f, 0.46f, 24.dp, VittaColors.GreenTrack.copy(alpha = 0.55f), 10.dp, 16.dp, 2, 0.8f),
    AuthBubble(0.08f, 1.04f, 120.dp, VittaColors.SurfaceMuted, 12.dp, 10.dp, 1, 3.1f),
    AuthBubble(0.84f, 0.90f, 18.dp, VittaColors.GoldAccent.copy(alpha = 0.22f), 8.dp, 14.dp, 2, 2.2f),
    AuthBubble(0.20f, 0.10f, 12.dp, VittaColors.GreenPale.copy(alpha = 0.6f), 6.dp, 12.dp, 2, 4.4f)
)

/** Duración de una vuelta completa de las burbujas y cada cuánto se actualizan. */
private const val BubbleCycleMillis = 22_000L
private const val BubbleFrameMillis = 100L

/** True si el usuario desactivó/redujo animaciones en Ajustes > Accesibilidad. */
@Composable
private fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/**
 * Burbujas decorativas que flotan lentamente (desplazamiento + leve cambio
 * de escala). Se dibujan en un Canvas leyendo la animación solo en la fase
 * de dibujo, así no recomponen nada cada frame. Si el sistema tiene las
 * animaciones desactivadas quedan quietas.
 */
@Composable
fun VittaAuthBubbles(modifier: Modifier = Modifier) {
    val reduceMotion = rememberReduceMotion()
    val density = LocalDensity.current

    // Antes: rememberInfiniteTransition → redibujaba a 60 fps sin parar
    // (medido en emulador: ~69 % de CPU en el Login). Ahora el tiempo
    // avanza ~10 veces por segundo, que a esta velocidad (≈10 dp en 22 s)
    // se ve igual de fluido, y se detiene si la pantalla no está activa.
    val time = remember { mutableFloatStateOf(0f) }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(reduceMotion, lifecycleOwner) {
        if (reduceMotion) return@LaunchedEffect
        val cycleNanos = BubbleCycleMillis * 1_000_000L
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                val phase = (System.nanoTime() % cycleNanos).toFloat() / cycleNanos
                time.floatValue = phase * (2 * PI).toFloat()
                delay(BubbleFrameMillis)
            }
        }
    }

    // graphicsLayer aísla las burbujas en su propia capa: al moverse solo se
    // vuelve a dibujar esta capa, no el formulario que está encima.
    Canvas(modifier = modifier.graphicsLayer().clearAndSetSemantics { }) {
        val t = time.floatValue
        AuthBubbles.forEach { bubble ->
            val angle = t * bubble.speed + bubble.phase
            val dx = with(density) { bubble.driftX.toPx() } * sin(angle)
            val dy = with(density) { bubble.driftY.toPx() } * cos(t * bubble.speed + bubble.phase * 1.7f)
            val scale = 1f + 0.04f * sin(angle + 1f)
            drawCircle(
                color = bubble.color,
                radius = with(density) { bubble.radius.toPx() } * scale,
                center = Offset(size.width * bubble.xFraction + dx, size.height * bubble.yFraction + dy)
            )
        }
    }
}

/** Tarjeta flotante de confirmación (p. ej. "¡Cuenta creada!"). */
@Composable
fun VittaAuthSuccessCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .widthIn(max = 360.dp)
            .semantics { liveRegion = LiveRegionMode.Assertive },
        shape = VittaShapes.extraLarge,
        color = VittaColorRoles.surface,
        contentColor = VittaColorRoles.textPrimary,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(VittaSpacing.Xxl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(VittaColorRoles.surfaceTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = VittaColorRoles.primaryPressed,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(Modifier.height(VittaSpacing.Lg))
            Text(title, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(message, style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
        }
    }
}
