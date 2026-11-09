package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.data.repository.CreateHabitResult
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.UserRepository
import com.vitta.app.ui.common.HabitUnits
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

data class HabitConfigUiState(
    val habits: List<PredefinedHabit> = emptyList(),
    val currentIndex: Int = 0,
    /** Índices de los hábitos ya guardados en el backend (check en el stepper, HAL-08). */
    val savedIndices: Set<Int> = emptySet(),
    val metaValor: Int = 0,
    /** Unidad preseleccionada del hábito; el usuario elige otra de su lista cerrada (HAL-06/07). */
    val metaUnidad: String = "",
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    /** Valor inicial; el usuario lo cambia con el selector de hora. */
    val reminderTime: LocalTime = LocalTime.of(7, 0),
    /** El usuario cambió algo del hábito actual (HAL-04). */
    val touched: Boolean = false,
    val metaError: String? = null,
    val unidadError: String? = null,
    val daysError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val finished: Boolean = false
) {
    val currentHabit: PredefinedHabit? get() = habits.getOrNull(currentIndex)

    /** Es el último pendiente: al guardarlo termina el flujo. */
    val isLastHabit: Boolean get() = habits.indices.count { it !in savedIndices } <= 1

    val hasChanges: Boolean get() = !finished && (touched || savedIndices.isNotEmpty())

    val canSave: Boolean
        get() = !isLoading && HabitUnits.validarCantidad(metaValor, metaUnidad) == null &&
            !(diasEspecificos && selectedDays.isEmpty())
}

class HabitConfigViewModel(
    habits: List<PredefinedHabit>,
    private val repository: HabitRepository = HabitRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(stateFor(HabitConfigUiState(habits = habits), 0))
    val uiState: StateFlow<HabitConfigUiState> = _uiState.asStateFlow()

    /** Carga los valores sugeridos del hábito [index] conservando lo ya guardado. */
    private fun stateFor(base: HabitConfigUiState, index: Int): HabitConfigUiState {
        val habit = base.habits.getOrNull(index)
        val unidad = habit?.let { HabitUnits.normalizar(it.icon, it.metaUnidad) }
        return HabitConfigUiState(
            habits = base.habits,
            currentIndex = index,
            savedIndices = base.savedIndices,
            metaValor = (habit?.metaValorSugerido ?: 0).let { v -> unidad?.let { minOf(v, it.max) } ?: v },
            metaUnidad = unidad?.nombre ?: ""
        )
    }

    /** Stepper: ir directo a un hábito pendiente. */
    fun goTo(index: Int) {
        val state = _uiState.value
        if (state.isLoading || index == state.currentIndex || index in state.savedIndices || index !in state.habits.indices) return
        _uiState.value = stateFor(state, index)
    }

    fun onMetaValorChange(value: Int) {
        val state = _uiState.value
        val v = value.coerceAtLeast(0)
        _uiState.value = state.copy(metaValor = v, metaError = HabitUnits.validarCantidad(v, state.metaUnidad), touched = true)
    }

    fun onMetaUnidadChange(value: String) {
        val state = _uiState.value
        _uiState.value = state.copy(
            metaUnidad = value,
            unidadError = null,
            metaError = HabitUnits.validarCantidad(state.metaValor, value),
            touched = true
        )
    }

    fun onFrequencyModeChange(diasEspecificos: Boolean) {
        _uiState.value = _uiState.value.copy(diasEspecificos = diasEspecificos, daysError = null, touched = true)
    }

    fun toggleDay(day: String) {
        val current = _uiState.value.selectedDays
        _uiState.value = _uiState.value.copy(
            selectedDays = if (day in current) current - day else current + day,
            daysError = null,
            touched = true
        )
    }

    fun onReminderChange(time: LocalTime) {
        _uiState.value = _uiState.value.copy(reminderTime = time, touched = true)
    }

    fun saveCurrentAndAdvance(onAllSaved: () -> Unit) {
        val state = _uiState.value
        val habit = state.currentHabit ?: return

        val metaError = HabitUnits.validarCantidad(state.metaValor, state.metaUnidad)
        val unidadError = if (state.metaUnidad !in HabitUnits.paraHabito(habit.icon).map { it.nombre })
            "Elige una unidad de la lista" else null
        // Sin días el backend rechaza la frecuencia vacía (400); se avisa antes.
        val daysError = if (state.diasEspecificos && state.selectedDays.isEmpty()) "Elige al menos un día" else null
        if (metaError != null || unidadError != null || daysError != null) {
            _uiState.value = state.copy(metaError = metaError, unidadError = unidadError, daysError = daysError)
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.createHabit(
                nombre = habit.nombre,
                meta = HabitTextFormat.buildMeta(state.metaValor, state.metaUnidad),
                frecuencia = HabitTextFormat.buildFrecuencia(state.diasEspecificos, state.selectedDays),
                tipo = habit.tipo,
                metaValor = state.metaValor.toDouble(),
                unidad = HabitTextFormat.unidadParaBackend(state.metaUnidad),
                icono = habit.icon.id,
                categoria = habit.categoria
            )) {
                is CreateHabitResult.Success -> {
                    com.vitta.app.ui.components.icons.HabitIconStore.save(result.habit.id, habit.icon)
                    // La hora se guarda como recordatorio del hábito (`POST /api/recordatorios`).
                    userRepository.saveReminder(result.habit.id, HabitTextFormat.hora(state.reminderTime))
                    val saved = state.savedIndices + state.currentIndex
                    val next = state.habits.indices.firstOrNull { it > state.currentIndex && it !in saved }
                        ?: state.habits.indices.firstOrNull { it !in saved }
                    if (next == null) {
                        _uiState.value = state.copy(isLoading = false, savedIndices = saved, finished = true)
                        onAllSaved()
                    } else {
                        _uiState.value = stateFor(state.copy(savedIndices = saved), next)
                    }
                }
                is CreateHabitResult.Error -> {
                    _uiState.value = state.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
