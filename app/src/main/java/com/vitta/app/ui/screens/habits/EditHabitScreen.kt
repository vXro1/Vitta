package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
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
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

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
        subtitle = state.nombre.ifBlank { null },
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
        HabitFormSection(title = "Nombre", step = 1) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                VittaHabitIconBubble(icon = state.icon, bubbleSize = 56.dp)
                Spacer(Modifier.width(VittaSpacing.Md))
                Text(
                    "El icono se elige automáticamente según el nombre (el servidor aún no guarda iconos).",
                    style = VittaTextStyles.bodySmall,
                    color = VittaColorRoles.textSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(VittaSpacing.Md))
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

        HabitFormSection(title = "Meta", step = 2) {
            HabitGoalInput(
                cantidad = state.metaValor,
                onCantidadChange = viewModel::onMetaValorChange,
                unidad = state.metaUnidad,
                onUnidadChange = viewModel::onMetaUnidadChange,
                unitSuggestions = suggestedUnits,
                step = HabitTextFormat.stepFor(state.metaUnidad),
                cantidadError = state.metaError,
                unidadError = state.unidadError
            )
        }

        HabitFormSection(title = "Frecuencia", step = 3) {
            HabitFrequencySelector(
                diasEspecificos = state.diasEspecificos,
                onModeChange = viewModel::onFrequencyModeChange,
                selectedDays = state.selectedDays,
                onToggleDay = viewModel::toggleDay,
                error = state.daysError
            )
        }

        HabitInfoNote(
            "La hora del recordatorio no aparece aquí porque el servidor todavía no la guarda; " +
                "por eso no hay una hora previa que mostrar ni editar."
        )
    }
}
