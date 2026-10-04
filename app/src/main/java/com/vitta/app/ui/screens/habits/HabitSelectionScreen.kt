package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.ui.components.buttons.VittaOutlinedButton
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Elegir hábitos predefinidos. Seleccionar/deseleccionar solo cambia el
 * estado local del ViewModel: la navegación ocurre únicamente al tocar
 * "Continuar", "Crear un hábito propio" o el botón de regreso.
 */
@Composable
fun HabitSelectionScreen(
    onContinue: (List<PredefinedHabit>) -> Unit,
    onCreateCustom: (List<PredefinedHabit>) -> Unit,
    onBack: (() -> Unit)? = null
) {
    val viewModel: HabitSelectionViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val count = state.selectedIds.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
    ) {
        if (onBack != null) {
            HabitScreenHeader(title = "¿Qué quieres mejorar?", subtitle = "Elige uno o varios", onBack = onBack)
        } else {
            Column(Modifier.padding(VittaSpacing.Lg)) {
                Text("¿Qué quieres mejorar?", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                Text("Elige uno o varios", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
        }
        Text(
            "Después podrás ajustar la meta, los días y la hora de cada uno.",
            style = VittaTextStyles.body,
            color = VittaColorRoles.textSecondary,
            modifier = Modifier.padding(horizontal = VittaSpacing.Lg)
        )
        Spacer(Modifier.height(VittaSpacing.Md))

        LazyRow(
            contentPadding = PaddingValues(horizontal = VittaSpacing.Lg),
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)
        ) {
            lazyRowItems(state.categories) { category ->
                VittaFilterChip(category, selected = category == state.selectedFilter) {
                    viewModel.onFilterSelect(category)
                }
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(VittaSpacing.Lg),
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Md),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
        ) {
            items(state.filteredHabits, key = { it.id }) { habit ->
                HabitSelectableCard(
                    habit = habit,
                    selected = habit.id in state.selectedIds,
                    onToggle = { viewModel.toggleHabit(habit.id) }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                VittaOutlinedButton(
                    text = "Crear un hábito propio",
                    icon = Icons.Filled.Add,
                    onClick = { onCreateCustom(viewModel.selectedHabits()) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColorRoles.surface)
                .padding(VittaSpacing.Lg)
        ) {
            Text(
                when (count) {
                    0 -> "Ningún hábito elegido"
                    1 -> "1 hábito elegido"
                    else -> "$count hábitos elegidos"
                },
                style = VittaTextStyles.subtitle,
                color = VittaColorRoles.textPrimary
            )
            Text(
                if (count == 0) "Elige al menos uno para continuar. Recomendamos empezar con 3."
                else "Recomendamos empezar con 3.",
                style = VittaTextStyles.bodySmall,
                color = VittaColorRoles.textMuted
            )
            Spacer(Modifier.height(VittaSpacing.Sm))
            VittaPrimaryButton(
                text = "Continuar",
                enabled = count > 0,
                onClick = { onContinue(viewModel.selectedHabits()) }
            )
        }
    }
}

@Composable
private fun HabitSelectableCard(
    habit: PredefinedHabit,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(VittaShapes.large)
            .background(if (selected) VittaColorRoles.surfaceTint else VittaColorRoles.surface)
            .border(
                BorderStroke(if (selected) 2.dp else 1.dp, if (selected) VittaColorRoles.primary else VittaColorRoles.border),
                VittaShapes.large
            )
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(VittaSpacing.Md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            VittaHabitIconBubble(
                icon = habit.icon,
                bubbleSize = 48.dp,
                containerColor = if (selected) VittaColorRoles.surface else VittaColorRoles.surfaceVariant
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(if (selected) VittaColorRoles.primary else VittaColorRoles.surface, CircleShape)
                    .border(1.5.dp, if (selected) VittaColorRoles.primary else VittaColorRoles.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = VittaColorRoles.onPrimary, modifier = Modifier.size(16.dp))
                }
            }
        }
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(habit.nombre, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
        Text(
            "${habit.metaValorSugerido} ${habit.metaUnidad} · ${habit.categoria}",
            style = VittaTextStyles.caption,
            color = VittaColorRoles.textMuted
        )
    }
}
