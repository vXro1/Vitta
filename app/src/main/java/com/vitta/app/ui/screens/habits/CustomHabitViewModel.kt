package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.CreateHabitResult
import com.vitta.app.data.repository.HabitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomHabitUiState(
    val nombre: String = "",
    val icon: HabitIcon = HabitIcon.Yoga,
    val metaValor: Int = 10,
    val metaUnidad: String = "minutos al día",
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    val reminderTime: String = "07:00",
    val nombreError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CustomHabitViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomHabitUiState())
    val uiState: StateFlow<CustomHabitUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null)
    }

    fun onIconSelect(icon: HabitIcon) {
        _uiState.value = _uiState.value.copy(icon = icon)
    }

    fun onMetaUnidadChange(value: String) {
        _uiState.value = _uiState.value.copy(metaUnidad = value)
    }

    fun onMetaIncrease() {
        _uiState.value = _uiState.value.copy(metaValor = _uiState.value.metaValor + 1)
    }

    fun onMetaDecrease() {
        val nuevo = (_uiState.value.metaValor - 1).coerceAtLeast(0)
        _uiState.value = _uiState.value.copy(metaValor = nuevo)
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

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.nombre.isBlank()) {
            _uiState.value = state.copy(nombreError = "Ponle un nombre a tu hábito")
            return
        }

        val frecuenciaTexto = if (state.diasEspecificos) {
            state.selectedDays.joinToString(",")
        } else "Diaria"
        val metaTexto = "${state.metaValor} ${state.metaUnidad}"

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.createHabit(
                nombre = state.nombre,
                meta = metaTexto,
                frecuencia = frecuenciaTexto,
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