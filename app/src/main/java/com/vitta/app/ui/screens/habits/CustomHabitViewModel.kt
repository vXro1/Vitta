package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.CreateHabitResult
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.UserRepository
import com.vitta.app.ui.common.HabitUnits
import com.vitta.app.ui.common.VittaValidation
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconStore
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
    val metaValor: Int = 1,
    /** Unidad de la lista cerrada del hábito (HAL-06/07); la primera es la preseleccionada. */
    val metaUnidad: String = HabitUnits.paraHabito(HabitIcons.Default).first().nombre,
    /** El usuario eligió la unidad a mano (si sigue siendo válida para el icono, se respeta). */
    val unidadChosenByUser: Boolean = false,
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    /** Valor inicial; el usuario lo cambia con el selector de hora. */
    val reminderTime: LocalTime = LocalTime.of(7, 0),
    /** El usuario cambió algo (para pedir confirmación al salir, HAL-04). */
    val touched: Boolean = false,
    val nombreError: String? = null,
    val metaError: String? = null,
    val unidadError: String? = null,
    val daysError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val saved: Boolean = false
) {
    val hasChanges: Boolean get() = touched && !saved

    /** HAL-05: "Guardar hábito" bloqueado mientras el título o la meta no cumplan. */
    val canSave: Boolean
        get() = !isLoading &&
            VittaValidation.tituloHabito(nombre) == null &&
            HabitUnits.validarCantidad(metaValor, metaUnidad) == null &&
            !(diasEspecificos && selectedDays.isEmpty())
}

class CustomHabitViewModel(
    private val repository: HabitRepository = HabitRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomHabitUiState())
    val uiState: StateFlow<CustomHabitUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        val state = _uiState.value
        // Mientras el usuario no elija un icono, se sugiere uno según el nombre.
        val suggested = if (state.iconChosenByUser) state.icon
        else HabitIcons.forHabitName(value) ?: HabitIcons.Default
        _uiState.value = withIcon(state, suggested).copy(
            nombre = value,
            nombreError = if (value.isEmpty()) null else VittaValidation.tituloHabito(value),
            touched = true
        )
    }

    fun onIconSelect(icon: HabitIcon) {
        _uiState.value = withIcon(_uiState.value, icon).copy(iconChosenByUser = true, touched = true)
    }

    /** Al cambiar el icono cambian las unidades permitidas: se conserva la elegida si sigue siendo válida. */
    private fun withIcon(state: CustomHabitUiState, icon: HabitIcon): CustomHabitUiState {
        val unidad = HabitUnits.normalizar(icon, if (state.unidadChosenByUser) state.metaUnidad else null)
        val valor = state.metaValor.coerceAtMost(unidad.max)
        return state.copy(icon = icon, metaUnidad = unidad.nombre, metaValor = valor, metaError = null, unidadError = null)
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
            unidadChosenByUser = true,
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

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val nombreError = VittaValidation.tituloHabito(state.nombre)
        val metaError = HabitUnits.validarCantidad(state.metaValor, state.metaUnidad)
        val unidadError = if (state.metaUnidad !in HabitUnits.paraHabito(state.icon).map { it.nombre })
            "Elige una unidad de la lista" else null
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

        // Se guardan la meta numérica, el icono y su área (para avance parcial y
        // logros) y la hora como recordatorio del hábito.
        _uiState.value = state.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.createHabit(
                nombre = state.nombre.trim(),
                meta = HabitTextFormat.buildMeta(state.metaValor, state.metaUnidad),
                frecuencia = HabitTextFormat.buildFrecuencia(state.diasEspecificos, state.selectedDays),
                tipo = "personalizado",
                metaValor = state.metaValor.toDouble(),
                unidad = HabitTextFormat.unidadParaBackend(state.metaUnidad),
                icono = state.icon.id,
                categoria = state.icon.category.label
            )) {
                is CreateHabitResult.Success -> {
                    HabitIconStore.save(result.habit.id, state.icon)
                    userRepository.saveReminder(result.habit.id, HabitTextFormat.hora(state.reminderTime))
                    _uiState.value = _uiState.value.copy(isLoading = false, saved = true)
                    onSaved()
                }
                is CreateHabitResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
