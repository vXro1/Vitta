package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun HabitConfigScreen(
    selectedHabits: List<PredefinedHabit>,
    onAllSaved: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: HabitConfigViewModel = viewModel(
        factory = HabitConfigViewModelFactory(selectedHabits)
    )
    val state by viewModel.uiState.collectAsState()
    val habit = state.currentHabit ?: return

    HabitFormScaffold(
        title = "Configura tu hábito",
        subtitle = "Hábito ${state.currentIndex + 1} de ${state.habits.size}",
        onBack = onBack,
        hasChanges = state.hasChanges,
        bottomBar = {
            state.errorMessage?.let {
                VittaAuthErrorBanner(it)
                Spacer(Modifier.height(VittaSpacing.Sm))
            }
            VittaPrimaryButton(
                text = when {
                    state.isLoading -> "Guardando…"
                    state.isLastHabit -> "Guardar hábito"
                    else -> "Guardar y siguiente"
                },
                loading = state.isLoading,
                enabled = state.canSave,
                onClick = { viewModel.saveCurrentAndAdvance(onAllSaved) }
            )
        }
    ) {
        // HAL-08: stepper con un paso por hábito (guardados con check, actual resaltado, pendientes en gris).
        if (state.habits.size > 1) {
            HabitStepper(
                names = state.habits.map { it.nombre },
                currentIndex = state.currentIndex,
                completed = state.savedIndices,
                onSelect = viewModel::goTo,
                icons = state.habits.map { it.icon }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColorRoles.surface, VittaShapes.large)
                .border(1.dp, VittaColorRoles.border, VittaShapes.large)
                .padding(VittaSpacing.Lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VittaHabitIconBubble(icon = habit.icon, bubbleSize = 56.dp)
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(habit.nombre, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                Text(habit.categoria, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
        }

        HabitFormSection(title = "Meta", step = 1, supportingText = "¿Cuánto quieres lograr cada vez?") {
            HabitGoalInput(
                cantidad = state.metaValor,
                onCantidadChange = viewModel::onMetaValorChange,
                unidad = state.metaUnidad,
                onUnidadChange = viewModel::onMetaUnidadChange,
                units = com.vitta.app.ui.common.HabitUnits.paraHabito(habit.icon),
                cantidadError = state.metaError,
                unidadError = state.unidadError
            )
        }

        HabitFormSection(title = "Frecuencia", step = 2) {
            HabitFrequencySelector(
                diasEspecificos = state.diasEspecificos,
                onModeChange = viewModel::onFrequencyModeChange,
                selectedDays = state.selectedDays,
                onToggleDay = viewModel::toggleDay,
                error = state.daysError
            )
        }

        HabitFormSection(title = "¿A qué hora?", step = 3) {
            HabitReminderTimeField(time = state.reminderTime, onTimeChange = viewModel::onReminderChange)
            Spacer(Modifier.height(VittaSpacing.Sm))
            HabitInfoNote(ReminderNote)
        }

        HabitInfoNote("Cada vez que cumplas este hábito ganarás 10 VitaPuntos.")
        HabitInfoNote(
            "Al completarlo cada día podrás describir lo que hiciste. Esas notas quedan en tu bitácora " +
                "y te ayudan a comprobar que de verdad cumples tu hábito."
        )
    }
}
