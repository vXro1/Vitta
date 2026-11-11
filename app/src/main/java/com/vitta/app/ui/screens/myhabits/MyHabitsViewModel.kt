package com.vitta.app.ui.screens.myhabits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.repository.DeleteHabitResult
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitResult
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconStore
import com.vitta.app.ui.components.icons.HabitIcons
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyHabitItem(
    val habit: HabitDto,
    val icon: HabitIcon,
    val activo: Boolean,
    /** Tiene registros: al eliminarlo el backend lo conserva desactivado (borrado lógico). */
    val tieneHistorial: Boolean,
    val notas: Int
)

data class MyHabitsUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val items: List<MyHabitItem> = emptyList(),
    /** Hábito con una operación en curso (desactivar, activar o eliminar). */
    val ocupado: Int? = null,
    val mensaje: String? = null
) {
    val activos: List<MyHabitItem> get() = items.filter { it.activo }
    val desactivados: List<MyHabitItem> get() = items.filterNot { it.activo }
}

/**
 * Pantalla «Mis hábitos» (HAL-13): lista simple de todos los hábitos
 * (`GET /api/habits?incluir_archivados=true`) con Desactivar/Activar
 * (`POST /archive` y `/restore`) y Eliminar (`DELETE /api/habits/:id`).
 */
class MyHabitsViewModel(private val repository: HabitRepository = HabitRepository()) : ViewModel() {

    private val _uiState = MutableStateFlow(MyHabitsUiState())
    val uiState: StateFlow<MyHabitsUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        _uiState.update { it.copy(cargando = it.items.isEmpty(), error = null) }
        viewModelScope.launch {
            when (val r = repository.listAllHabits()) {
                is HabitsResult.Success -> {
                    val items = coroutineScope {
                        r.habits.map { habit ->
                            async {
                                val records = (repository.listRecords(habit.id) as? RecordsResult.Success)?.records.orEmpty()
                                HabitIconStore.sync(habit.id, habit.icono)
                                MyHabitItem(
                                    habit = habit,
                                    icon = HabitIcons.byId(habit.icono) ?: HabitIconStore.iconFor(habit.id, habit.nombre),
                                    activo = habit.activo != false,
                                    tieneHistorial = records.isNotEmpty(),
                                    notas = records.count { !it.notas.isNullOrBlank() }
                                )
                            }
                        }.awaitAll()
                    }
                    _uiState.update { it.copy(cargando = false, items = items.sortedBy { i -> i.habit.nombre.lowercase() }) }
                }
                is HabitsResult.Error -> _uiState.update { it.copy(cargando = false, error = r.message) }
            }
        }
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }

    fun cambiarActivo(item: MyHabitItem, onChanged: () -> Unit) {
        if (_uiState.value.ocupado != null) return
        _uiState.update { it.copy(ocupado = item.habit.id) }
        viewModelScope.launch {
            when (val r = repository.setActive(item.habit.id, !item.activo)) {
                is HabitResult.Success -> {
                    _uiState.update {
                        it.copy(
                            ocupado = null,
                            mensaje = if (item.activo) "\"${item.habit.nombre}\" quedó desactivado" else "\"${item.habit.nombre}\" está activo otra vez"
                        )
                    }
                    onChanged()
                    cargar()
                }
                is HabitResult.Error -> _uiState.update { it.copy(ocupado = null, mensaje = r.message) }
            }
        }
    }

    fun eliminar(item: MyHabitItem, onChanged: () -> Unit) {
        if (_uiState.value.ocupado != null) return
        _uiState.update { it.copy(ocupado = item.habit.id) }
        viewModelScope.launch {
            when (val r = repository.deleteHabit(item.habit.id)) {
                is DeleteHabitResult.Success -> {
                    _uiState.update {
                        it.copy(
                            ocupado = null,
                            mensaje = if (r.archivado) "\"${item.habit.nombre}\" se eliminó de tus listas y su historial se conserva"
                            else "\"${item.habit.nombre}\" se eliminó"
                        )
                    }
                    onChanged()
                    cargar()
                }
                is DeleteHabitResult.Error -> _uiState.update { it.copy(ocupado = null, mensaje = r.message) }
            }
        }
    }
}
