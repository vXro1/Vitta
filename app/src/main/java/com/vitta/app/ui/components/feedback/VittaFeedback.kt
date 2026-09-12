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
                        Text(area, style = VittaTextStyles.label, color = VittaColors.OrangeAccent)
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
