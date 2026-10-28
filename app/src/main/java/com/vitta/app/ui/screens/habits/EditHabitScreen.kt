package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.mock.suggestedUnits
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.forms.VittaAuthTextField
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Editar un hábito existente con los mismos bloques que "Nuevo hábito"
 * (nombre, icono, meta, frecuencia y recordatorio). Todo se carga y se
 * guarda en el backend; arriba se ve cómo quedará la tarjeta del hábito.
 */
@Composable
fun EditHabitScreen(
    habitId: Int,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: EditHabitViewModel = viewModel(factory = EditHabitViewModelFactory(habitId))
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    when {
        state.isLoadingHabit -> {
            Column(Modifier.fillMaxSize().background(VittaColorRoles.background)) {
                HabitScreenHeader(title = "Editar hábito", onBack = onBack)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VittaColorRoles.primary)
                }
            }
            return
        }
        state.loadError != null -> {
            Column(Modifier.fillMaxSize().background(VittaColorRoles.background)) {
                HabitScreenHeader(title = "Editar hábito", onBack = onBack)
                Column(Modifier.padding(VittaSpacing.Lg)) {
                    VittaAuthErrorBanner(state.loadError ?: "")
                    Spacer(Modifier.height(VittaSpacing.Lg))
                    VittaPrimaryButton(text = "Reintentar", onClick = viewModel::load)
                }
            }
            return
        }
    }

    HabitFormScaffold(
        title = "Editar hábito",
        subtitle = "Los cambios se guardan en tu cuenta",
        onBack = onBack,
        bottomBar = {
            state.errorMessage?.let {
                VittaAuthErrorBanner(it)
                Spacer(Modifier.height(VittaSpacing.Sm))
            }
            VittaPrimaryButton(
                text = if (state.isSaving) "Guardando…" else "Guardar cambios",
                loading = state.isSaving,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.save(onSaved)
                }
            )
        }
    ) {
        // Vista previa con los datos actuales del formulario (igual que en "Configura tu hábito").
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColorRoles.surface, VittaShapes.large)
                .border(1.dp, VittaColorRoles.border, VittaShapes.large)
                .padding(VittaSpacing.Lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VittaHabitIconBubble(icon = state.icon, bubbleSize = 56.dp)
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(
                    state.nombre.ifBlank { "Tu hábito" },
                    style = VittaTextStyles.title,
                    color = VittaColorRoles.textPrimary
                )
                Text(
                    "${HabitTextFormat.buildMeta(state.metaValor, state.metaUnidad)} · " +
                        "${frequencySummary(state.diasEspecificos, state.selectedDays)} · " +
                        formatReminderTime(state.reminderTime),
                    style = VittaTextStyles.bodySmall,
                    color = VittaColorRoles.textMuted
                )
            }
        }

        HabitFormSection(title = "Nombre", step = 1) {
            VittaAuthTextField(
                value = state.nombre,
                onValueChange = viewModel::onNombreChange,
                label = "Nombre del hábito",
                leadingIcon = Icons.Outlined.Edit,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                errorText = state.nombreError
            )
        }

        HabitFormSection(
            title = "Icono",
            step = 2,
            supportingText = "Toca “Más” para ver todo el catálogo."
        ) {
            HabitIconPicker(selected = state.icon, onSelect = viewModel::onIconSelect)
        }

        HabitFormSection(title = "Meta", step = 3, supportingText = "¿Cuánto quieres lograr cada vez?") {
            HabitGoalInput(
                cantidad = state.metaValor,
                onCantidadChange = viewModel::onMetaValorChange,
                unidad = state.metaUnidad,
                onUnidadChange = viewModel::onMetaUnidadChange,
                unitSuggestions = (listOf(state.metaUnidad) + suggestedUnits).filter { it.isNotBlank() }.distinct(),
                step = HabitTextFormat.stepFor(state.metaUnidad),
                cantidadError = state.metaError,
                unidadError = state.unidadError
            )
        }

        HabitFormSection(title = "Frecuencia", step = 4) {
            HabitFrequencySelector(
                diasEspecificos = state.diasEspecificos,
                onModeChange = viewModel::onFrequencyModeChange,
                selectedDays = state.selectedDays,
                onToggleDay = viewModel::toggleDay,
                error = state.daysError
            )
        }

        HabitFormSection(title = "Recordatorio", step = 5) {
            HabitReminderTimeField(time = state.reminderTime, onTimeChange = viewModel::onReminderChange)
            Spacer(Modifier.height(VittaSpacing.Sm))
            HabitInfoNote(ReminderNotSavedNote)
        }

        HabitInfoNote("Cambiar la meta no borra lo que ya registraste: tu historial, racha y puntos se conservan.")
    }
}
