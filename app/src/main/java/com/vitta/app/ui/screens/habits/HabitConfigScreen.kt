package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.forms.VittaTextField
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun HabitConfigScreen(
    selectedHabits: List<PredefinedHabit>,
    onAllSaved: () -> Unit
) {
    val viewModel: HabitConfigViewModel = viewModel(
        factory = HabitConfigViewModelFactory(selectedHabits)
    )
    val state by viewModel.uiState.collectAsState()
    val habit = state.currentHabit ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
            .padding(VittaSpacing.Lg)
    ) {
        Text(
            "Hábito ${state.currentIndex + 1} de ${state.habits.size}",
            style = VittaTextStyles.caption,
            color = VittaColorRoles.textMuted
        )
        Spacer(Modifier.height(VittaSpacing.Xs))
        Text(habit.nombre, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)

        Spacer(Modifier.height(VittaSpacing.Lg))
        VittaTextField(value = state.meta, onValueChange = viewModel::onMetaChange, label = "Meta")

        Spacer(Modifier.height(VittaSpacing.Xl))
        Text("Frecuencia", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        Spacer(Modifier.height(VittaSpacing.Sm))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColors.SurfaceMuted, VittaShapes.pill)
                .padding(4.dp)
        ) {
            FrequencyToggle("Todos los días", !state.diasEspecificos, Modifier.weight(1f)) {
                viewModel.onFrequencyModeChange(false)
            }
            FrequencyToggle("Días específicos", state.diasEspecificos, Modifier.weight(1f)) {
                viewModel.onFrequencyModeChange(true)
            }
        }

        if (state.diasEspecificos) {
            Spacer(Modifier.height(VittaSpacing.Md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                    val selected = day in state.selectedDays
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                if (selected) VittaColorRoles.primary else VittaColors.SurfaceVariant,
                                CircleShape
                            )
                            .clickable { viewModel.toggleDay(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day,
                            style = VittaTextStyles.bodySmall,
                            color = if (selected) VittaColorRoles.background else VittaColorRoles.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(VittaSpacing.Xl))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColors.Surface, VittaShapes.large)
                .padding(VittaSpacing.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Recordatorio", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                Text("Vitta te avisa a esta hora", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            }
            Text(state.reminderTime, style = VittaTextStyles.title, color = VittaColors.OrangeAccent)
            // TODO: abrir un TimePickerDialog al tocar esto y llamar
            // viewModel.onReminderChange(hh:mm). Lo dejo solo visual por
            // ahora porque el backend no tiene dónde guardarlo todavía.
        }

        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            "Crear un hábito no otorga VitaPuntos. Los puntos llegan cuando registras cumplimiento.",
            style = VittaTextStyles.caption,
            color = VittaColorRoles.textMuted
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(state.errorMessage ?: "", style = VittaTextStyles.bodySmall, color = VittaColors.Error)
        }

        Spacer(Modifier.weight(1f))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VittaColorRoles.primary)
            }
        } else {
            VittaPrimaryButton(
                text = if (state.isLastHabit) "Guardar hábito" else "Guardar y siguiente",
                onClick = { viewModel.saveCurrentAndAdvance(onAllSaved) }
            )
        }
    }
}

@Composable
private fun FrequencyToggle(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .background(if (selected) VittaColors.Surface else VittaColors.SurfaceMuted, VittaShapes.pill)
            .clickable(onClick = onClick)
            .padding(vertical = VittaSpacing.Sm),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = VittaTextStyles.button,
            color = if (selected) VittaColorRoles.textPrimary else VittaColorRoles.textMuted
        )
    }
}