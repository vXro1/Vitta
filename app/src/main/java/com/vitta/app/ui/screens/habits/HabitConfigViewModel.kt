package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.data.repository.CreateHabitResult
import com.vitta.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

data class HabitConfigUiState(
    val habits: List<PredefinedHabit> = emptyList(),
    val currentIndex: Int = 0,
    val metaValor: Int = 0,
    /** Unidad sugerida por el hábito predefinido; el usuario puede cambiarla. */
    val metaUnidad: String = "",
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    /** Valor inicial; el usuario lo cambia con el selector de hora. */
    val reminderTime: LocalTime = LocalTime.of(7, 0),
    val metaError: String? = null,
    val unidadError: String? = null,
    val daysError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val finished: Boolean = false
) {
    val currentHabit: PredefinedHabit? get() = habits.getOrNull(currentIndex)
    val isLastHabit: Boolean get() = currentIndex == habits.lastIndex
}

class HabitConfigViewModel(
    habits: List<PredefinedHabit>,
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialStateFor(habits, 0))
    val uiState: StateFlow<HabitConfigUiState> = _uiState.asStateFlow()

    private fun initialStateFor(habits: List<PredefinedHabit>, index: Int): HabitConfigUiState {
        val habit = habits.getOrNull(index)
        return HabitConfigUiState(
            habits = habits,
            currentIndex = index,
            metaValor = habit?.metaValorSugerido ?: 0,
            metaUnidad = habit?.metaUnidad ?: ""
        )
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

    fun saveCurrentAndAdvance(onAllSaved: () -> Unit) {
        val state = _uiState.value
        val habit = state.currentHabit ?: return

        val metaError = if (state.metaValor <= 0) "La meta debe ser mayor que 0" else null
        val unidadError = if (state.metaUnidad.isBlank()) "Escribe una unidad, por ejemplo “minutos”" else null
        // Sin días el backend rechaza la frecuencia vacía (400); se avisa antes.
        val daysError = if (state.diasEspecificos && state.selectedDays.isEmpty()) "Elige al menos un día" else null
        if (metaError != null || unidadError != null || daysError != null) {
            _uiState.value = state.copy(metaError = metaError, unidadError = unidadError, daysError = daysError)
            return
        }

        // La hora del recordatorio no se envía: el backend no tiene ese campo.
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.createHabit(
                nombre = habit.nombre,
                meta = HabitTextFormat.buildMeta(state.metaValor, state.metaUnidad),
                frecuencia = HabitTextFormat.buildFrecuencia(state.diasEspecificos, state.selectedDays),
                tipo = habit.tipo
            )) {
                is CreateHabitResult.Success -> {
                    if (state.isLastHabit) {
                        _uiState.value = state.copy(isLoading = false, finished = true)
                        onAllSaved()
                    } else {
                        _uiState.value = initialStateFor(state.habits, state.currentIndex + 1)
                    }
                }
                is CreateHabitResult.Error -> {
                    _uiState.value = state.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
