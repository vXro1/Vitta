package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitResult
import com.vitta.app.ui.components.icons.HabitIcons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditHabitUiState(
    val isLoadingHabit: Boolean = true,
    val loadError: String? = null,
    val nombre: String = "",
    val metaValor: Int = 0,
    val metaUnidad: String = "",
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    /** Frecuencia tal como vino del servidor, por si no es "Diaria" ni días (L..D). */
    val frecuenciaOriginal: String = "",
    val frequencyTouched: Boolean = false,
    val nombreError: String? = null,
    val metaError: String? = null,
    val unidadError: String? = null,
    val daysError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val icon get() = HabitIcons.forHabitName(nombre) ?: HabitIcons.Default
}

/** Edita nombre, meta (cantidad + unidad) y frecuencia: los campos que acepta PUT /api/habits/:id. */
class EditHabitViewModel(
    private val habitId: Int,
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditHabitUiState())
    val uiState: StateFlow<EditHabitUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoadingHabit = true, loadError = null)
        viewModelScope.launch {
            when (val result = repository.getHabit(habitId)) {
                is HabitResult.Success -> {
                    val habit = result.habit
                    val (cantidad, unidad) = HabitTextFormat.parseMeta(habit.meta)
                    val (especificos, days) = HabitTextFormat.parseFrecuencia(habit.frecuencia)
                    _uiState.value = EditHabitUiState(
                        isLoadingHabit = false,
                        nombre = habit.nombre,
                        metaValor = cantidad ?: 1,
                        metaUnidad = unidad,
                        diasEspecificos = especificos,
                        selectedDays = days,
                        frecuenciaOriginal = habit.frecuencia
                    )
                }
                is HabitResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingHabit = false, loadError = result.message)
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null)
    }

    fun onMetaValorChange(value: Int) {
        _uiState.value = _uiState.value.copy(metaValor = value.coerceAtLeast(0), metaError = null)
    }

    fun onMetaUnidadChange(value: String) {
        _uiState.value = _uiState.value.copy(metaUnidad = value, unidadError = null)
    }

    fun onFrequencyModeChange(diasEspecificos: Boolean) {
        _uiState.value = _uiState.value.copy(diasEspecificos = diasEspecificos, frequencyTouched = true, daysError = null)
    }

    fun toggleDay(day: String) {
        val current = _uiState.value.selectedDays
        _uiState.value = _uiState.value.copy(
            selectedDays = if (day in current) current - day else current + day,
            frequencyTouched = true,
            daysError = null
        )
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val nombreError = if (state.nombre.isBlank()) "Ponle un nombre a tu hábito" else null
        val metaError = if (state.metaValor <= 0) "La cantidad debe ser mayor que 0" else null
        val unidadError = if (state.metaUnidad.isBlank()) "Escribe una unidad, por ejemplo “minutos”" else null
        val daysError = if (state.diasEspecificos && state.selectedDays.isEmpty()) "Elige al menos un día" else null
        if (nombreError != null || metaError != null || unidadError != null || daysError != null) {
            _uiState.value = state.copy(
                nombreError = nombreError, metaError = metaError,
                unidadError = unidadError, daysError = daysError
            )
            return
        }

        // Si el usuario no tocó la frecuencia se reenvía la original sin cambios.
        val frecuencia = if (state.frequencyTouched) {
            HabitTextFormat.buildFrecuencia(state.diasEspecificos, state.selectedDays)
        } else state.frecuenciaOriginal

        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.updateHabit(
                habitId = habitId,
                nombre = state.nombre.trim(),
                meta = HabitTextFormat.buildMeta(state.metaValor, state.metaUnidad),
                frecuencia = frecuencia
            )) {
                is HabitResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onSaved()
                }
                is HabitResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                }
            }
        }
    }
}

class EditHabitViewModelFactory(private val habitId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EditHabitViewModel(habitId) as T
    }
}
