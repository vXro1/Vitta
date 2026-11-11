package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitResult
import com.vitta.app.data.repository.DeleteHabitResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.data.repository.RemindersResult
import com.vitta.app.data.repository.SaveReminderResult
import com.vitta.app.data.repository.UserRepository
import com.vitta.app.ui.common.HabitUnits
import com.vitta.app.ui.common.VittaValidation
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconStore
import com.vitta.app.ui.components.icons.HabitIcons
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime

data class EditHabitUiState(
    val isLoadingHabit: Boolean = true,
    val loadError: String? = null,
    val nombre: String = "",
    val icon: HabitIcon = HabitIcons.Default,
    /** Área guardada en el backend; se conserva si el usuario no cambia el icono. */
    val categoriaOriginal: String? = null,
    val iconOriginalId: String? = null,
    val metaValor: Int = 0,
    val metaUnidad: String = "",
    val diasEspecificos: Boolean = false,
    val selectedDays: Set<String> = setOf("L", "M", "X", "J", "V"),
    /** Frecuencia tal como vino del servidor, por si no es "Diaria" ni días (L..D). */
    val frecuenciaOriginal: String = "",
    val frequencyTouched: Boolean = false,
    /** Hora del recordatorio guardada en `/api/recordatorios`; null si el hábito no tiene. */
    val reminderOriginal: LocalTime? = null,
    val reminderTime: LocalTime = LocalTime.of(7, 0),
    val nombreError: String? = null,
    val metaError: String? = null,
    val unidadError: String? = null,
    val daysError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    /** true si ya tiene registros: al "eliminarlo" el backend lo archiva y conserva su historial. */
    val tieneHistorial: Boolean = false,
    /** El usuario cambió algo (para pedir confirmación al salir, HAL-04). */
    val touched: Boolean = false
) {
    val hasChanges: Boolean get() = touched && !isSaving

    /** HAL-05/06/07: "Guardar cambios" bloqueado mientras el título, la unidad o la cantidad no cumplan. */
    val canSave: Boolean
        get() = !isSaving && VittaValidation.tituloHabito(nombre) == null &&
            metaUnidad in HabitUnits.paraHabito(icon).map { it.nombre } &&
            HabitUnits.validarCantidad(metaValor, metaUnidad) == null &&
            !(diasEspecificos && selectedDays.isEmpty())
}

/**
 * Edita un hábito real: nombre, icono, meta (cantidad + unidad, también como
 * `meta_valor`), frecuencia (`PUT /api/habits/:id`) y la hora del
 * recordatorio (`/api/recordatorios`). Todo se carga del backend.
 */
class EditHabitViewModel(
    private val habitId: Int,
    private val repository: HabitRepository = HabitRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditHabitUiState())
    val uiState: StateFlow<EditHabitUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        _uiState.value = _uiState.value.copy(isLoadingHabit = true, loadError = null)
        viewModelScope.launch {
            val remindersCall = async { userRepository.listReminders() }
            val recordsCall = async { repository.listRecords(habitId) }
            when (val result = repository.getHabit(habitId)) {
                is HabitResult.Success -> {
                    val habit = result.habit
                    val (cantidad, unidad) = HabitTextFormat.parseMeta(habit.meta)
                    val (especificos, days) = HabitTextFormat.parseFrecuencia(habit.frecuencia)
                    val hora = (remindersCall.await() as? RemindersResult.Success)
                        ?.reminders?.firstOrNull { it.habito_id == habitId }
                        ?.let { HabitTextFormat.parseHora(it.hora) }
                    // Icono del backend; si el hábito es antiguo y no lo tiene, el guardado en el teléfono o el del nombre.
                    val icon = HabitIcons.byId(habit.icono) ?: HabitIconStore.iconFor(habit.id, habit.nombre)
                    val tieneHistorial = (recordsCall.await() as? RecordsResult.Success)?.records?.isNotEmpty() == true
                    val metaValor = cantidad ?: habit.meta_valor?.toInt() ?: 1
                    val metaUnidad = unidad.ifBlank { habit.unidad.orEmpty() }
                    // Datos guardados antes de las nuevas reglas: se marcan para que el usuario los corrija.
                    val unidadValida = metaUnidad in HabitUnits.paraHabito(icon).map { it.nombre }
                    _uiState.value = EditHabitUiState(
                        nombreError = VittaValidation.tituloHabito(habit.nombre),
                        unidadError = if (unidadValida) null else "Elige una unidad de la lista",
                        metaError = if (unidadValida) HabitUnits.validarCantidad(metaValor, metaUnidad) else null,
                        isLoadingHabit = false,
                        nombre = habit.nombre,
                        icon = icon,
                        categoriaOriginal = habit.categoria,
                        iconOriginalId = habit.icono,
                        metaValor = metaValor,
                        metaUnidad = metaUnidad,
                        diasEspecificos = especificos,
                        selectedDays = days,
                        frecuenciaOriginal = habit.frecuencia,
                        reminderOriginal = hora,
                        reminderTime = hora ?: LocalTime.of(7, 0),
                        tieneHistorial = tieneHistorial
                    )
                }
                is HabitResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoadingHabit = false, loadError = result.message)
                }
            }
        }
    }

    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = VittaValidation.tituloHabito(value), touched = true)
    }

    /** Al cambiar el icono cambian las unidades permitidas: se conserva la actual si sigue siendo válida. */
    fun onIconSelect(icon: HabitIcon) {
        val state = _uiState.value
        val unidad = HabitUnits.normalizar(icon, state.metaUnidad)
        val valor = state.metaValor.coerceIn(0, unidad.max)
        _uiState.value = state.copy(
            icon = icon,
            metaUnidad = unidad.nombre,
            metaValor = valor,
            unidadError = null,
            metaError = HabitUnits.validarCantidad(valor, unidad.nombre),
            touched = true
        )
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
        _uiState.value = _uiState.value.copy(diasEspecificos = diasEspecificos, frequencyTouched = true, daysError = null, touched = true)
    }

    fun toggleDay(day: String) {
        val current = _uiState.value.selectedDays
        _uiState.value = _uiState.value.copy(
            selectedDays = if (day in current) current - day else current + day,
            frequencyTouched = true,
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
        val unidadError = if (state.metaUnidad !in HabitUnits.paraHabito(state.icon).map { it.nombre })
            "Elige una unidad de la lista" else null
        val metaError = HabitUnits.validarCantidad(state.metaValor, state.metaUnidad)
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
        val iconChanged = state.icon.id != state.iconOriginalId
        val categoria = if (iconChanged || state.categoriaOriginal.isNullOrBlank()) state.icon.category.label
        else state.categoriaOriginal

        _uiState.value = state.copy(isSaving = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.updateHabit(
                habitId = habitId,
                nombre = state.nombre.trim(),
                meta = HabitTextFormat.buildMeta(state.metaValor, state.metaUnidad),
                frecuencia = frecuencia,
                metaValor = state.metaValor.toDouble(),
                unidad = HabitTextFormat.unidadParaBackend(state.metaUnidad),
                icono = state.icon.id,
                categoria = categoria
            )
            if (result is HabitResult.Error) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
                return@launch
            }
            HabitIconStore.save(habitId, state.icon)
            if (state.reminderOriginal != state.reminderTime) {
                val reminder = userRepository.saveReminder(habitId, HabitTextFormat.hora(state.reminderTime))
                if (reminder is SaveReminderResult.Error) {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = reminder.message)
                    return@launch
                }
            }
            _uiState.value = _uiState.value.copy(isSaving = false)
            onSaved()
        }
    }

    /** DELETE /api/habits/:id — sin registros lo borra; con historial el backend lo archiva. */
    fun delete(onDeleted: () -> Unit) {
        if (_uiState.value.isDeleting) return
        _uiState.value = _uiState.value.copy(isDeleting = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.deleteHabit(habitId)) {
                is DeleteHabitResult.Success -> {
                    _uiState.value = _uiState.value.copy(isDeleting = false)
                    onDeleted()
                }
                is DeleteHabitResult.Error ->
                    _uiState.value = _uiState.value.copy(isDeleting = false, errorMessage = result.message)
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
