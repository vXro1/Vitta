package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.ui.components.buttons.VittaOutlinedButton
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun HabitSelectionScreen(
    onContinue: (List<PredefinedHabit>) -> Unit,
    onCreateCustom: (List<PredefinedHabit>) -> Unit
) {
    val viewModel: HabitSelectionViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
    ) {
        Column(modifier = Modifier.padding(VittaSpacing.Lg)) {
            Text("¿Qué quieres mejorar?", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(
                "Elige uno o varios. Podrás ajustar meta, días y horario después.",
                style = VittaTextStyles.body,
                color = VittaColorRoles.textSecondary
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = VittaSpacing.Lg),
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)
        ) {
            lazyRowItems(state.categories) { category ->
                val selected = category == state.selectedFilter
                Box(
                    modifier = Modifier
                        .background(
                            if (selected) VittaColorRoles.textPrimary else VittaColors.SurfaceMuted,
                            VittaShapes.pill
                        )
                        .clickable { viewModel.onFilterSelect(category) }
                        .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm)
                ) {
                    Text(
                        category,
                        style = VittaTextStyles.button,
                        color = if (selected) VittaColorRoles.background else VittaColorRoles.textSecondary
                    )
                }
            }
        }

        Spacer(Modifier.height(VittaSpacing.Md))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(VittaSpacing.Lg),
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)
        ) {
            items(state.filteredHabits, key = { it.id }) { habit ->
                HabitSelectableCard(
                    habit = habit,
                    selected = habit.id in state.selectedIds,
                    onClick = { viewModel.toggleHabit(habit.id) }
                )
            }
            item(span = { GridItemSpan(2) }) {
                VittaOutlinedButton(
                    text = "+  Crear un hábito propio",
                    onClick = { onCreateCustom(viewModel.selectedHabits()) }
                )
            }
        }

        Column(
            modifier = Modifier
                .background(VittaColors.Surface)
                .padding(VittaSpacing.Lg)
        ) {
            Text(
                "${state.selectedIds.size} hábitos elegidos",
                style = VittaTextStyles.subtitle,
                color = VittaColorRoles.textPrimary
            )
            Text(
                "Recomendamos empezar con 3.",
                style = VittaTextStyles.caption,
                color = VittaColorRoles.textMuted
            )
            Spacer(Modifier.height(VittaSpacing.Sm))
            VittaPrimaryButton(
                text = "Continuar",
                enabled = state.selectedIds.isNotEmpty(),
                onClick = { onContinue(viewModel.selectedHabits()) }
            )
        }
    }
}

@Composable
private fun HabitSelectableCard(
    habit: PredefinedHabit,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(
                if (selected) VittaColors.SurfaceTint else VittaColors.Surface,
                VittaShapes.large
            )
            .clickable(onClick = onClick)
            .padding(VittaSpacing.Md)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            VittaHabitIconBubble(
                icon = habit.icon,
                bubbleSize = 42.dp
            )
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(
                        if (selected) VittaColorRoles.primary else VittaColors.SurfaceVariant,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Text("✓", style = VittaTextStyles.caption, color = VittaColorRoles.background)
                }
            }
        }
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(habit.nombre, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
        Text(habit.categoria, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
    }
}