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
    val tieneHistorial: Boolean = false
)

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
                    _uiState.value = EditHabitUiState(
                        isLoadingHabit = false,
                        nombre = habit.nombre,
                        icon = icon,
                        categoriaOriginal = habit.categoria,
                        iconOriginalId = habit.icono,
                        metaValor = cantidad ?: habit.meta_valor?.toInt() ?: 1,
                        metaUnidad = unidad.ifBlank { habit.unidad.orEmpty() },
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
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null)
    }

    fun onIconSelect(icon: HabitIcon) {
        _uiState.value = _uiState.value.copy(icon = icon)
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

    fun onReminderChange(time: LocalTime) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
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
