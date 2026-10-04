package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.CreateHabitResult
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIcons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

data class CustomHabitUiState(
    val nombre: String = "",
    val icon: HabitIcon = HabitIcons.Default,
    /** true cuando el usuario eligió el icono a mano (deja de sugerirse por nombre). */
    val iconChosenByUser: Boolean = false,
    val metaValor: Int = 10,
    val metaUnidad: String = "minutos",
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    /** Valor inicial; el usuario lo cambia con el selector de hora. */
    val reminderTime: LocalTime = LocalTime.of(7, 0),
    val nombreError: String? = null,
    val metaError: String? = null,
    val unidadError: String? = null,
    val daysError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CustomHabitViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomHabitUiState())
    val uiState: StateFlow<CustomHabitUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        val state = _uiState.value
        // Mientras el usuario no elija un icono, se sugiere uno según el nombre.
        val suggested = if (state.iconChosenByUser) state.icon
        else HabitIcons.forHabitName(value) ?: HabitIcons.Default
        _uiState.value = state.copy(nombre = value, nombreError = null, icon = suggested)
    }

    fun onIconSelect(icon: HabitIcon) {
        _uiState.value = _uiState.value.copy(icon = icon, iconChosenByUser = true)
    }

    fun onMetaValorChange(value: Int) {
        _uiState.value = _uiState.value.copy(metaValor = value.coerceAtLeast(0), metaError = null)
    }

    fun onMetaUnidadChange(value: String) {
        _uiState.value = _uiState.value.copy(metaUnidad = value, unidadError = null)
    }

    fun onFrequencyModeChange(diasEspecificos: Boolean) {
        _uiState.value = _uiState.value.copy(diasEspecificos = diasEspecificos, daysError = null)
    }

    fun toggleDay(day: String) {
        val current = _uiState.value.selectedDays
        _uiState.value = _uiState.value.copy(
            selectedDays = if (day in current) current - day else current + day,
            daysError = null
        )
    }

    fun onReminderChange(time: LocalTime) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val nombreError = if (state.nombre.isBlank()) "Ponle un nombre a tu hábito" else null
        val metaError = if (state.metaValor <= 0) "La cantidad debe ser mayor que 0" else null
        val unidadError = if (state.metaUnidad.isBlank()) "Escribe una unidad, por ejemplo “minutos”" else null
        // Sin días el backend rechaza la frecuencia vacía (400); se avisa antes.
        val daysError = if (state.diasEspecificos && state.selectedDays.isEmpty()) "Elige al menos un día" else null

        if (nombreError != null || metaError != null || unidadError != null || daysError != null) {
            _uiState.value = state.copy(
                nombreError = nombreError,
                metaError = metaError,
                unidadError = unidadError,
                daysError = daysError
            )
            return
        }

        // Nota: el icono y la hora del recordatorio NO se envían: el backend
        // no tiene columnas para ellos (ver informe / README_RECURSOS.md).
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.createHabit(
                nombre = state.nombre.trim(),
                meta = HabitTextFormat.buildMeta(state.metaValor, state.metaUnidad),
                frecuencia = HabitTextFormat.buildFrecuencia(state.diasEspecificos, state.selectedDays),
                tipo = "personalizado"
            )) {
                is CreateHabitResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSaved()
                }
                is CreateHabitResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
