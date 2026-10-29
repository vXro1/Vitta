package com.vitta.app.ui.components.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.components.common.VittaIconAssets
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.buttons.VittaIconCircleButton
import com.vitta.app.ui.components.buttons.VittaOutlinedButton
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * A bottom toast/snackbar for a completed action, e.g. "Hábito completado ·
 * Racha de 7 días". Mirrors the mockup's `hayToast` overlay: an absolutely
 * positioned dark pill anchored to the bottom of the *screen*. Place this as
 * the last child of the screen's own `Box`, not of some app-wide root, so it
 * never escapes onto content outside that screen.
 */
@Composable
fun VittaToast(
    visible: Boolean,
    points: Int,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColorRoles.toastSurface, VittaShapes.large)
                .padding(horizontal = VittaSpacing.Xl, vertical = VittaSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(VittaColors.GoldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("+$points", style = VittaTextStyles.caption, color = VittaColorRoles.textPrimary)
            }
            Spacer(Modifier.width(VittaSpacing.Md))
            Column {
                Text(title, style = VittaTextStyles.subtitle, color = VittaColorRoles.toastOnSurface)
                Text(description, style = VittaTextStyles.bodySmall, color = VittaColorRoles.toastSecondaryText)
            }
        }
    }
}

/**
 * The shared full-screen scrim behind [VittaConfirmDialog] and
 * [VittaRewardSheet]. Tapping the dimmed backdrop dismisses; tapping the
 * sheet itself does not (the inner click is intercepted separately below).
 * Like [VittaToast], this must be layered inside the *screen's* own Box so
 * the scrim only ever covers that screen, never the whole app.
 */
@Composable
private fun VittaModalScrim(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    alignment: Alignment,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VittaColorRoles.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = alignment
        ) {
            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // absorbs the click so it doesn't bubble to the scrim above
                )
            ) {
                content()
            }
        }
    }
}

/**
 * The confirmation dialog for spending points on a habit-locked reward
 * (mirrors `hayDialogo`): icon, area label, name, description, a cost row,
 * and cancel/confirm actions.
 */
@Composable
fun VittaConfirmDialog(
    visible: Boolean,
    area: String,
    name: String,
    description: String,
    cost: String,
    balance: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null
) {
    VittaModalScrim(visible = visible, onDismissRequest = onCancel, alignment = Alignment.BottomCenter) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(VittaColorRoles.background, RoundedCornerShape(VittaShapes.RadiusSheet))
                .padding(VittaSpacing.Xl)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(VittaColorRoles.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { icon() }
                    Spacer(Modifier.width(VittaSpacing.Md))
                }
                Column {
                    Text(area, style = VittaTextStyles.label, color = VittaColorRoles.textDisabled)
                    Text(name, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                }
            }
            Spacer(Modifier.height(VittaSpacing.Md))
            Text(description, style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
            Spacer(Modifier.height(VittaSpacing.Lg))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VittaColorRoles.surfaceMuted, VittaShapes.large)
                    .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(cost, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                Text(balance, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            }
            Spacer(Modifier.height(VittaSpacing.Xl))
            Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
                VittaOutlinedButton(text = "Cancelar", onClick = onCancel, fillWidth = false)
                VittaPrimaryButton(text = "Confirmar", onClick = onConfirm, modifier = Modifier.weight(1f))
            }
        }
    }
}

/**
 * The unlocked-reward sheet (mirrors `hayRecompensa`): a header with the
 * mascot, a list of unlocked "piezas" ([content]), and a close button.
 */
@Composable
fun VittaRewardSheet(
    visible: Boolean,
    area: String,
    title: String,
    description: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    mascot: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    VittaModalScrim(visible = visible, onDismissRequest = onDismiss, alignment = Alignment.BottomCenter) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(VittaColorRoles.background, RoundedCornerShape(VittaShapes.RadiusSheet))
        ) {
            Column(modifier = Modifier.padding(VittaSpacing.Xl)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    VittaIconCircleButton(icon = Icons.Filled.Close, contentDescription = "Cerrar", onClick = onDismiss)
                }
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(area, style = VittaTextStyles.label, color = VittaColors.GreenPressed)
                        Spacer(Modifier.height(VittaSpacing.Xs))
                        Text(title, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                        Spacer(Modifier.height(VittaSpacing.Xs))
                        Text(description, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textSecondary)
                    }
                    if (mascot != null) {
                        Spacer(Modifier.width(VittaSpacing.Sm))
                        mascot()
                    }
                }
            }
            Column(
                modifier = Modifier.padding(horizontal = VittaSpacing.Xl, vertical = VittaSpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(VittaSpacing.Sm),
                content = content
            )
            Spacer(Modifier.height(VittaSpacing.Lg))
        }
    }
}

/**
 * Celebración "¡Felicitaciones!" al sumar un nuevo día de racha: tarjeta
 * centrada sobre el mismo scrim de [VittaConfirmDialog] / [VittaRewardSheet],
 * con la mascota y confeti arriba, la píldora "¡NUEVO LOGRO!", el contador
 * real de días de racha (llama de Vitta) y el botón "¡Genial!" para cerrarla.
 */
@Composable
fun VittaStreakDialog(
    visible: Boolean,
    days: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    mascot: @Composable () -> Unit
) {
    VittaModalScrim(visible = visible, onDismissRequest = onDismiss, alignment = Alignment.Center) {
        Column(
            modifier = modifier
                .padding(horizontal = VittaSpacing.Xl)
                .fillMaxWidth()
                .background(VittaColorRoles.surface, RoundedCornerShape(28.dp))
                .padding(horizontal = VittaSpacing.Xl, vertical = VittaSpacing.Lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mascota con confeti y la llama de racha a los lados.
            Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Confeti(Modifier.matchParentSize())
                VittaAssetImage(
                    assetPath = VittaIconAssets.streakFlame,
                    contentDescription = null,
                    size = 26.dp,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 22.dp, bottom = 10.dp)
                )
                mascot()
            }
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(
                "¡NUEVO LOGRO!",
                style = VittaTextStyles.label.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = VittaColors.OrangeAccent,
                modifier = Modifier
                    .background(VittaColors.OrangeAccent.copy(alpha = 0.08f), RoundedCornerShape(50))
                    .border(1.dp, VittaColors.OrangeAccent.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Xs)
            )
            Spacer(Modifier.height(VittaSpacing.Md))
            Text(
                "¡Felicitaciones!",
                style = VittaTextStyles.displayLarge,
                color = VittaColorRoles.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(
                "Acabas de desbloquear un nuevo día de racha",
                style = VittaTextStyles.body,
                color = VittaColorRoles.textSecondary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(VittaSpacing.Lg))
            Row(
                modifier = Modifier
                    .background(VittaColorRoles.surfaceVariant, RoundedCornerShape(50))
                    .padding(horizontal = VittaSpacing.Xl, vertical = VittaSpacing.Sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VittaAssetImage(assetPath = VittaIconAssets.streakFlame, contentDescription = null, size = 26.dp)
                Spacer(Modifier.width(VittaSpacing.Sm))
                Text("$days", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                Spacer(Modifier.width(VittaSpacing.Xs))
                Text(
                    if (days == 1) "día de racha" else "días de racha",
                    style = VittaTextStyles.body,
                    color = VittaColorRoles.textSecondary
                )
            }
            Spacer(Modifier.height(VittaSpacing.Lg))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(50))
                    .background(VittaColorRoles.toastSurface)
                    .clickable(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "¡Genial!",
                    style = VittaTextStyles.button.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    color = VittaColorRoles.toastOnSurface
                )
            }
        }
    }
}

/** Confeti decorativo alrededor de la mascota (colores de la marca). */
@Composable
private fun Confeti(modifier: Modifier) {
    val piezas = remember {
        listOf(
            // x, y (fracción del área), ángulo, color
            Triple(0.16f, 0.22f, -30f) to VittaColors.GoldAccent,
            Triple(0.26f, 0.62f, 25f) to VittaColors.GreenMid,
            Triple(0.08f, 0.46f, 60f) to VittaColors.OrangeAccent,
            Triple(0.80f, 0.18f, 35f) to VittaColors.GreenMid,
            Triple(0.88f, 0.42f, -50f) to VittaColors.GoldAccent,
            Triple(0.74f, 0.70f, 15f) to VittaColors.OrangeAccent,
            Triple(0.34f, 0.08f, 70f) to VittaColors.OrangeAccent,
            Triple(0.64f, 0.06f, -15f) to VittaColors.GoldAccent
        )
    }
    androidx.compose.foundation.Canvas(modifier) {
        val w = 5.dp.toPx()
        val h = 14.dp.toPx()
        piezas.forEach { (pos, color) ->
            val (fx, fy, angulo) = pos
            val cx = size.width * fx
            val cy = size.height * fy
            rotate(angulo, pivot = androidx.compose.ui.geometry.Offset(cx, cy)) {
                drawRoundRect(
                    color = color,
                    topLeft = androidx.compose.ui.geometry.Offset(cx - w / 2, cy - h / 2),
                    size = androidx.compose.ui.geometry.Size(w, h),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w / 2)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE, heightDp = 500)
@Composable
private fun VittaToastPreview() {
    VittaTheme {
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            VittaToast(visible = true, points = 10, title = "Hábito completado", description = "Racha de 7 días")
        }
    }
}
