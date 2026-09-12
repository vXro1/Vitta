package com.vitta.app.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * The dark "Tu saldo" balance hero card from the Store screen: a gold
 * circular icon, an uppercase label, and a big Caprasimo point value.
 */
@Composable
fun VittaBalanceCard(
    points: Int,
    modifier: Modifier = Modifier,
    label: String = "TU SALDO",
    unit: String = "VitaPuntos"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.toastSurface, VittaShapes.extraLarge)
            .padding(VittaSpacing.Xl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(VittaColors.GoldAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = VittaColorRoles.textPrimary)
        }
        Spacer(Modifier.width(VittaSpacing.Md))
        Column {
            Text(label, style = VittaTextStyles.label, color = VittaColors.GreenPale)
            Text(
                "$points $unit",
                style = VittaTextStyles.title,
                color = VittaColorRoles.toastOnSurface
            )
        }
    }
}

/**
 * A dark highlight card for a single achievement, e.g. "Última medalla ·
 * Constancia Vita". [trailing] is typically a small peeking mascot pose.
 */
@Composable
fun VittaHighlightCard(
    label: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.toastSurface, VittaShapes.extraLarge)
            .padding(VittaSpacing.Xl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(VittaSpacing.Lg))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = VittaTextStyles.label, color = VittaColors.GoldAccent)
            Spacer(Modifier.height(VittaSpacing.Xxs))
            Text(title, style = VittaTextStyles.title, color = VittaColorRoles.toastOnSurface)
            Spacer(Modifier.height(VittaSpacing.Xxs))
            Text(description, style = VittaTextStyles.bodySmall, color = VittaColors.GreenPale)
        }
        if (trailing != null) {
            Spacer(Modifier.width(VittaSpacing.Sm))
            trailing()
        }
    }
}

/**
 * A centered, light info card built around an illustration — e.g. "La más
 * cercana" (the nearest locked achievement) on the Logros screen.
 */
@Composable
fun VittaCenteredInfoCard(
    eyebrow: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .padding(VittaSpacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (illustration != null) {
            illustration()
            Spacer(Modifier.height(VittaSpacing.Lg))
        }
        Text(eyebrow, style = VittaTextStyles.label, color = VittaColorRoles.textDisabled)
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            title,
            style = VittaTextStyles.heading,
            color = VittaColorRoles.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            description,
            style = VittaTextStyles.body,
            color = VittaColorRoles.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (footer != null) {
            Spacer(Modifier.height(VittaSpacing.Lg))
            footer()
        }
    }
}

/**
 * The dashed-border empty state card, e.g. "Todavía no has canjeado nada"
 * on the Store screen — a small mascot pose next to a short message.
 */
@Composable
fun VittaEmptyStateCard(
    message: String,
    modifier: Modifier = Modifier,
    mascot: MascotState = MascotState.EmptyState
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.background, VittaShapes.extraLarge)
            .padding(VittaSpacing.Xl),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaMascot(state = mascot, size = MascotSize.Small)
        Spacer(Modifier.width(VittaSpacing.Md))
        Text(
            message,
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textMuted,
            modifier = Modifier.weight(1f)
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaRewardCardsPreview() {
    VittaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VittaBalanceCard(points = 1250)
            VittaHighlightCard(
                label = "ÚLTIMA MEDALLA",
                title = "Constancia Vita",
                description = "30 días seguidos · +100 VitaPuntos",
                trailing = { VittaMascot(state = MascotState.Peek, size = MascotSize.Small) }
            )
            VittaEmptyStateCard(message = "Todavía no has canjeado nada. Lo que desbloquees aparecerá aquí.")
        }
    }
}
