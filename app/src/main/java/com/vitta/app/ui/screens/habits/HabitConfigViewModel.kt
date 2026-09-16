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

data class HabitConfigUiState(
    val habits: List<PredefinedHabit> = emptyList(),
    val currentIndex: Int = 0,
    val metaValor: Int = 0,
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    val reminderTime: String = "07:00",
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

    private val _uiState = MutableStateFlow(
        HabitConfigUiState(habits = habits, metaValor = habits.firstOrNull()?.metaValorSugerido ?: 0)
    )
    val uiState: StateFlow<HabitConfigUiState> = _uiState.asStateFlow()

    private fun stepFor(habit: PredefinedHabit?): Int =
        if (habit?.metaUnidad == "pasos") 500 else 1

    fun onMetaIncrease() {
        val state = _uiState.value
        _uiState.value = state.copy(metaValor = state.metaValor + stepFor(state.currentHabit))
    }

    fun onMetaDecrease() {
        val state = _uiState.value
        val nuevo = (state.metaValor - stepFor(state.currentHabit)).coerceAtLeast(0)
        _uiState.value = state.copy(metaValor = nuevo)
    }

    fun onFrequencyModeChange(diasEspecificos: Boolean) {
        _uiState.value = _uiState.value.copy(diasEspecificos = diasEspecificos)
    }

    fun toggleDay(day: String) {
        val current = _uiState.value.selectedDays
        _uiState.value = _uiState.value.copy(
            selectedDays = if (day in current) current - day else current + day
        )
    }

    fun onReminderChange(time: String) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }

    fun saveCurrentAndAdvance(onAllSaved: () -> Unit) {
        val state = _uiState.value
        val habit = state.currentHabit ?: return

        if (state.metaValor <= 0) {
            _uiState.value = state.copy(errorMessage = "La meta debe ser mayor que 0")
            return
        }

        val frecuenciaTexto = if (state.diasEspecificos) {
            state.selectedDays.joinToString(",")
        } else "Diaria"

        val metaTexto = "${state.metaValor} ${habit.metaUnidad}"

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.createHabit(
                nombre = habit.nombre,
                meta = metaTexto,
                frecuencia = frecuenciaTexto,
                tipo = habit.tipo            )) {
                is CreateHabitResult.Success -> {
                    if (state.isLastHabit) {
                        _uiState.value = state.copy(isLoading = false, finished = true)
                        onAllSaved()
                    } else {
                        val nextIndex = state.currentIndex + 1
                        val nextHabit = state.habits[nextIndex]
                        _uiState.value = HabitConfigUiState(
                            habits = state.habits,
                            currentIndex = nextIndex,
                            metaValor = nextHabit.metaValorSugerido
                        )
                    }
                }
                is CreateHabitResult.Error -> {
                    _uiState.value = state.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}