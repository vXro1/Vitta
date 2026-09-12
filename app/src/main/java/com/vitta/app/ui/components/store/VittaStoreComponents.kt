package com.vitta.app.ui.components.store

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSizes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * The three states a "Para desbloquear" store item can be in (mirrors the
 * mockup's `t.esLibre` / `t.esBloq` / `t.esFaltan` flags).
 */
sealed interface StoreItemState {
    /** Unlockable right now with a tap. */
    data object Unlockable : StoreItemState
    /** Locked behind a non-point requirement, e.g. "Completa 3 días de yoga". */
    data class Locked(val requirement: String) : StoreItemState
    /** Locked only by points, e.g. "Faltan 40 VitaPuntos". */
    data class MissingPoints(val message: String) : StoreItemState
}

/**
 * One row of the Store's "Para desbloquear" list: mascot thumbnail, area,
 * name, description, and a bottom row that changes with [state].
 */
@Composable
fun VittaStoreItemCard(
    mascot: MascotState,
    area: String,
    name: String,
    description: String,
    state: StoreItemState,
    modifier: Modifier = Modifier,
    onUnlock: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .padding(VittaSpacing.Lg)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(VittaSizes.IconBubbleLarge)
                    .background(VittaColorRoles.surfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                VittaMascot(state = mascot, size = MascotSize.Small)
            }
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(modifier = Modifier.weight(1f)) {
                Text(area, style = VittaTextStyles.label, color = VittaColorRoles.textDisabled)
                Spacer(Modifier.height(VittaSpacing.Xxs))
                Text(name, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                Spacer(Modifier.height(VittaSpacing.Xxs))
                Text(description, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
        }
        Spacer(Modifier.height(VittaSpacing.Md))
        when (state) {
            is StoreItemState.Unlockable -> VittaPrimaryButton(
                text = "Desbloquear",
                onClick = { onUnlock?.invoke() }
            )
            is StoreItemState.Locked -> StorePillRow(icon = Icons.Filled.Lock, text = state.requirement)
            is StoreItemState.MissingPoints -> StorePillRow(icon = null, text = state.message)
        }
    }
}

@Composable
private fun StorePillRow(icon: androidx.compose.ui.graphics.vector.ImageVector?, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(VittaSizes.ButtonHeightSmall)
            .background(VittaColorRoles.surfaceMuted, VittaShapes.pill)
            .padding(horizontal = VittaSpacing.Lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = VittaColorRoles.textDisabled, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(VittaSpacing.Xs))
        }
        Text(text, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
    }
}

/**
 * A row in "Mis recompensas": an already-unlocked reward the user can open
 * again — circular mascot thumbnail, area/name/piece-count, and a chevron.
 */
@Composable
fun VittaRewardListItem(
    mascot: MascotState,
    area: String,
    name: String,
    meta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surfaceTint, VittaShapes.extraLarge)
            .clickable(onClick = onClick)
            .padding(VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(VittaSizes.IconBubbleLarge)
                .background(VittaColorRoles.background, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            VittaMascot(state = mascot, size = MascotSize.Small)
        }
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(modifier = Modifier.weight(1f)) {
            Text(area, style = VittaTextStyles.label, color = VittaColorRoles.textMuted)
            Spacer(Modifier.height(VittaSpacing.Xxs))
            Text(name, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
            Spacer(Modifier.height(VittaSpacing.Xxs))
            Text(meta, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = VittaColorRoles.primary)
    }
}

/** A horizontally scrollable row of category filter chips ("Todo", "Bienestar", …). */
@Composable
fun VittaFilterChipsRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)
    ) {
        items(options) { option ->
            val isSelected = option == selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) VittaColorRoles.toastSurface else VittaColorRoles.surfaceVariant,
                        VittaShapes.pill
                    )
                    .clickable { onSelect(option) }
                    .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm)
            ) {
                Text(
                    option,
                    style = VittaTextStyles.subtitle,
                    color = if (isSelected) VittaColorRoles.toastOnSurface else VittaColorRoles.textSecondary
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaStoreComponentsPreview() {
    VittaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VittaFilterChipsRow(
                options = listOf("Todo", "Bienestar", "Lectura", "Movimiento"),
                selected = "Todo",
                onSelect = {}
            )
            VittaRewardListItem(
                mascot = MascotState.Yoga,
                area = "BIENESTAR",
                name = "Rutina de yoga en 5 minutos",
                meta = "4 piezas · Abrir",
                onClick = {}
            )
            VittaStoreItemCard(
                mascot = MascotState.Strong,
                area = "MOVIMIENTO",
                name = "Caminatas de 20 minutos",
                description = "Seis recorridos con ritmo sugerido.",
                state = StoreItemState.MissingPoints("Faltan 40 VitaPuntos")
            )
            VittaStoreItemCard(
                mascot = MascotState.Reader,
                area = "LECTURA",
                name = "Diez cuentos cortos",
                description = "Historias de cinco minutos para antes de dormir.",
                state = StoreItemState.Locked("Completa 3 sesiones de lectura")
            )
        }
    }
}
