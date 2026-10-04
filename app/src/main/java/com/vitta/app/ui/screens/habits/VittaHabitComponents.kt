package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIcons
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSizes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * Burbuja circular con el icono del hábito (Icons8, a color). El icono se
 * centra con `contentAlignment` y ocupa una fracción fija del círculo, así
 * queda alineado a cualquier tamaño sin padding manual. Es decorativo
 * (contentDescription = null): el nombre del hábito ya se lee al lado.
 */
@Composable
fun VittaHabitIconBubble(
    icon: HabitIcon,
    modifier: Modifier = Modifier,
    bubbleSize: androidx.compose.ui.unit.Dp = VittaSizes.IconBubbleMedium,
    containerColor: androidx.compose.ui.graphics.Color = VittaColorRoles.surfaceVariant
) {
    Box(
        modifier = modifier
            .size(bubbleSize)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon.drawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.64f),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * A single habit row on the "Hoy" list, e.g. "Tomar agua · Vas 3 de 8 vasos".
 * [trailing] hosts the state control (a checkmark, a "+N" quick-add, or a
 * "Listo" pill) — kept as a slot instead of a fixed enum so callers can
 * compose whatever state control the screen needs.
 */
@Composable
fun VittaHabitRow(
    icon: HabitIcon,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .padding(VittaSpacing.Lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaHabitIconBubble(icon = icon)
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
            Text(subtitle, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        }
        Spacer(Modifier.width(VittaSpacing.Sm))
        trailing()
    }
}

/** The "done" trailing state: a filled circular checkmark. */
@Composable
fun VittaHabitDoneIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(VittaSizes.IconBubbleSmall)
            .background(VittaColorRoles.primary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Check,
            contentDescription = "Completado",
            tint = VittaColorRoles.onPrimary,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * The 7/8-glass water tracker row from the mockup (`agua-lleno`/`agua-vacio`
 * chips). [filled] is how many of [total] glasses are marked as drunk.
 */
@Composable
fun VittaWaterTracker(
    filled: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onGlassClick: ((index: Int) -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Xs)
    ) {
        repeat(total) { index ->
            val isFilled = index < filled
            Box(
                modifier = Modifier
                    .weight(1f)
                    .size(VittaSizes.TrackerChip)
                    .background(
                        if (isFilled) VittaColorRoles.surfaceVariant else VittaColorRoles.surfaceMuted,
                        VittaShapes.small
                    )
                    .then(
                        if (onGlassClick != null) {
                            Modifier.clickable { onGlassClick(index) }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                VittaAssetImage(
                    assetPath = VittaIconAssets.icon(if (isFilled) "agua-lleno" else "agua-vacio"),
                    contentDescription = null,
                    size = VittaSizes.TrackerChip * 0.6f
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF6FBF8)
@Composable
private fun VittaHabitRowPreview() {
    VittaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VittaHabitRow(
                icon = HabitIcons.Water,
                title = "Tomar agua",
                subtitle = "Vas 3 de 8 vasos",
                trailing = { VittaHabitDoneIndicator() }
            )
            VittaHabitRow(
                icon = HabitIcons.Yoga,
                title = "Hacer yoga",
                subtitle = "10 minutos · 07:00"
            )
        }
    }
}
