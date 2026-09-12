package com.vitta.app.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * Base surface every Vitta card is built on: the mockup's recurring
 * `background:#FFFCF8; border-radius:...; box-shadow:0 3px 14px rgba(45,64,62,.07)`
 * pattern. Prefer the specific *Card composables below; drop down to this
 * only for a one-off layout the others don't cover.
 */
@Composable
fun VittaSurfaceCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = VittaShapes.large,
    containerColor: Color = VittaColorRoles.surface,
    contentPadding: PaddingValues = PaddingValues(VittaSpacing.Lg),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** A stat tile like the "12 · días de racha" / "1.250 · VitaPuntos" pair on Home. */
@Composable
fun VittaStatTile(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .background(VittaColorRoles.surfaceMuted, VittaShapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column {
            Text(text = value, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
            Text(text = label, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
        }
    }
}

/** A progress summary card, e.g. the Home "1 de 4 hábitos listos" hero card. */
@Composable
fun VittaProgressCard(
    eyebrow: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null
) {
    VittaSurfaceCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(eyebrow, style = VittaTextStyles.label, color = VittaColors.OrangeAccent)
                Spacer(Modifier.height(VittaSpacing.Xxs))
                Text(title, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                Spacer(Modifier.height(VittaSpacing.Xs))
                Text(description, style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
            }
            if (illustration != null) {
                Spacer(Modifier.width(VittaSpacing.Md))
                illustration()
            }
        }
        if (footer != null) {
            Spacer(Modifier.height(VittaSpacing.Lg))
            footer()
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaCardsPreview() {
    VittaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            VittaProgressCard(
                eyebrow = "TU DÍA EMPIEZA AQUÍ",
                title = "Vitta te está esperando",
                description = "Cuatro hábitos listos para registrar. Empieza por el más fácil."
            )
        }
    }
}
