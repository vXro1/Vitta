package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
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
import com.vitta.app.ui.components.icons.HabitIcons
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaSpacing

/** Ancho máximo del formulario para que en tablets/horizontal no se estire. */
internal val HabitFormMaxWidth = 560.dp

@Composable
fun CustomHabitScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: CustomHabitViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    HabitFormScaffold(
        title = "Nuevo hábito",
        subtitle = "Créalo a tu medida en 5 pasos",
        onBack = onBack,
        bottomBar = {
            state.errorMessage?.let {
                VittaAuthErrorBanner(it)
                Spacer(Modifier.height(VittaSpacing.Sm))
            }
            VittaPrimaryButton(
                text = if (state.isLoading) "Guardando…" else "Guardar hábito",
                loading = state.isLoading,
                onClick = {
                    focusManager.clearFocus()
                    viewModel.save(onSaved)
                }
            )
        }
    ) {
        HabitFormSection(title = "Nombre", step = 1) {
            VittaAuthTextField(
                value = state.nombre,
                onValueChange = viewModel::onNombreChange,
                label = "¿Qué hábito quieres construir?",
                placeholder = "Ej: Meditar, Correr, Tomar vitaminas",
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
            supportingText = "Te sugerimos uno según el nombre. Toca “Más” para ver todo el catálogo."
        ) {
            HabitIconPicker(selected = state.icon, onSelect = viewModel::onIconSelect)
            // El servidor no guarda el icono: al recargar se deduce del nombre.
            val shownLater = HabitIcons.forHabitName(state.nombre) ?: HabitIcons.Default
            if (state.nombre.isNotBlank() && shownLater.id != state.icon.id) {
                Spacer(Modifier.height(VittaSpacing.Sm))
                HabitInfoNote(
                    "El servidor aún no guarda el icono elegido. En tu inicio este hábito " +
                        "se verá con el icono “${shownLater.label}”, que es el que coincide con su nombre."
                )
            }
        }

        HabitFormSection(title = "Meta", step = 3, supportingText = "¿Cuánto quieres lograr cada vez?") {
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

        HabitInfoNote("Crear un hábito no otorga VitaPuntos. Los puntos llegan cuando registras cumplimiento.")
    }
}

/** Aviso honesto: el backend todavía no guarda icono ni hora (ver informe). */
internal const val ReminderNotSavedNote =
    "Por ahora la hora no se guarda en tu cuenta ni genera notificaciones: el servidor aún no tiene dónde almacenarla."

/**
 * Esqueleto común de los formularios de hábito: cabecera con regreso,
 * contenido con scroll (centrado y con ancho máximo) y barra inferior fija
 * con la acción principal, que sube con el teclado.
 */
@Composable
fun HabitFormScaffold(
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null,
    bottomBar: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
            .imePadding()
    ) {
        HabitScreenHeader(title = title, subtitle = subtitle, onBack = onBack)

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = HabitFormMaxWidth)
                    .fillMaxWidth()
                    .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(VittaSpacing.Xxl),
                content = content
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColorRoles.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = HabitFormMaxWidth)
                    .fillMaxWidth()
                    .padding(VittaSpacing.Lg),
                content = bottomBar
            )
        }
    }
}
