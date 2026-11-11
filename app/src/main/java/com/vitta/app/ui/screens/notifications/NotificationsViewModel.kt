package com.vitta.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.reminders.ReminderScheduler
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.ReminderDto
import com.vitta.app.data.repository.DeleteReminderResult
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.data.repository.LogroRepository
import com.vitta.app.data.repository.LogrosResult
import com.vitta.app.data.repository.MovementsResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.data.repository.RemindersResult
import com.vitta.app.data.repository.SaveReminderResult
import com.vitta.app.data.repository.UserRepository
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
import java.time.Instant

data class ReminderItem(
    val reminder: ReminderDto,
    val habit: HabitDto,
    val icon: HabitIcon
)

enum class TipoAviso { Habito, Logro, Racha }

/** Una entrada del historial, armada con datos guardados en la base de datos. */
data class AvisoItem(
    val id: String,
    val tipo: TipoAviso,
    val titulo: String,
    val detalle: String,
    val fecha: Instant
)

data class NotificationsUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val recordatorios: List<ReminderItem> = emptyList(),
    val historial: List<AvisoItem> = emptyList(),
    val errorHistorial: String? = null,
    val ocupado: Int? = null,
    val mensaje: String? = null
)

/**
 * Notificaciones (HAL-20):
 * - Recordatorios: hora y estado de `/api/recordatorios` (activar/pausar y
 *   cambiar la hora con `PATCH`), que programan las notificaciones del teléfono.
 * - Historial: lo que ya pasó, desde la base de datos: los VitaPuntos por
 *   hábito cumplido (`/api/points/me/movements`) y las medallas obtenidas,
 *   incluidas las de racha (`/api/logros`, `obtenido_en`).
 */
class NotificationsViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val habitRepository: HabitRepository = HabitRepository(),
    private val logroRepository: LogroRepository = LogroRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        _uiState.update { it.copy(cargando = it.recordatorios.isEmpty() && it.historial.isEmpty(), error = null) }
        viewModelScope.launch {
            val habitsCall = async { habitRepository.listAllHabits() }
            val remindersCall = async { userRepository.listReminders() }
            val movementsCall = async { userRepository.listMovements() }
            val logrosCall = async { logroRepository.listLogros() }

            val habits = when (val r = habitsCall.await()) {
                is HabitsResult.Success -> r.habits
                is HabitsResult.Error -> {
                    _uiState.update { it.copy(cargando = false, error = r.message) }
                    return@launch
                }
            }
            val reminders = when (val r = remindersCall.await()) {
                is RemindersResult.Success -> r.reminders
                is RemindersResult.Error -> {
                    _uiState.update { it.copy(cargando = false, error = r.message) }
                    return@launch
                }
            }
            val porId = habits.associateBy { it.id }
            val items = reminders.mapNotNull { r ->
                val h = porId[r.habito_id] ?: return@mapNotNull null
                ReminderItem(r, h, HabitIcons.byId(h.icono) ?: HabitIconStore.iconFor(h.id, h.nombre))
            }.sortedBy { it.reminder.hora }
            ReminderScheduler.sync(reminders, habits.filter { it.activo != false })

            // Historial: cada registro con puntos se asocia a su hábito con los registros de cada hábito.
            val registroAHabito: Map<Int, HabitDto> = coroutineScope {
                habits.map { h -> async { h to habitRepository.listRecords(h.id) } }.awaitAll()
            }.flatMap { (h, r) -> (r as? RecordsResult.Success)?.records.orEmpty().map { it.id to h } }.toMap()

            val movimientos = movementsCall.await()
            val logros = (logrosCall.await() as? LogrosResult.Success)?.logros.orEmpty()
            val historial = mutableListOf<AvisoItem>()
            if (movimientos is MovementsResult.Success) {
                movimientos.movements.filter { it.motivo == "habito_cumplido" }.forEach { m ->
                    val fecha = runCatching { Instant.parse(m.creado_en) }.getOrNull() ?: return@forEach
                    val h = m.registro_id?.let { registroAHabito[it] }
                    historial += AvisoItem(
                        id = "m${m.id}",
                        tipo = TipoAviso.Habito,
                        titulo = if (h != null) "Completaste ${h.nombre}" else "Completaste un hábito",
                        detalle = "+${m.puntos} VitaPuntos",
                        fecha = fecha
                    )
                }
            }
            logros.filter { it.obtenido }.forEach { l ->
                val fecha = l.obtenido_en?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return@forEach
                val racha = l.icono.orEmpty().startsWith("racha_")
                historial += AvisoItem(
                    id = "l${l.id}",
                    tipo = if (racha) TipoAviso.Racha else TipoAviso.Logro,
                    titulo = if (racha) "¡Nueva racha! ${l.nombre}" else "Logro desbloqueado: ${l.nombre}",
                    detalle = l.condicion + if (l.puntos_bonus > 0) " · +${l.puntos_bonus} VitaPuntos" else "",
                    fecha = fecha
                )
            }
            _uiState.update {
                it.copy(
                    cargando = false,
                    recordatorios = items,
                    historial = historial.sortedByDescending { a -> a.fecha },
                    errorHistorial = (movimientos as? MovementsResult.Error)?.message
                )
            }
        }
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }

    fun cambiarActivo(item: ReminderItem, activo: Boolean) = actualizar(item, activo = activo)

    fun cambiarHora(item: ReminderItem, hora: String) = actualizar(item, hora = hora)

    /** DELETE /api/recordatorios/:id; el repositorio también cancela la alarma. */
    fun borrar(item: ReminderItem) {
        if (_uiState.value.ocupado != null) return
        _uiState.update { it.copy(ocupado = item.reminder.id) }
        viewModelScope.launch {
            when (val r = userRepository.deleteReminder(item.reminder.id, item.habit.id)) {
                is DeleteReminderResult.Success -> _uiState.update {
                    it.copy(
                        ocupado = null,
                        recordatorios = it.recordatorios.filter { n -> n.reminder.id != item.reminder.id },
                        mensaje = "Recordatorio de ${item.habit.nombre} borrado"
                    )
                }
                is DeleteReminderResult.Error -> _uiState.update { it.copy(ocupado = null, mensaje = r.message) }
            }
        }
    }

    private fun actualizar(item: ReminderItem, hora: String? = null, activo: Boolean? = null) {
        if (_uiState.value.ocupado != null) return
        _uiState.update { it.copy(ocupado = item.reminder.id) }
        viewModelScope.launch {
            when (val r = userRepository.updateReminder(item.reminder.id, hora = hora, activo = activo)) {
                is SaveReminderResult.Success -> {
                    val nuevos = _uiState.value.recordatorios.map { if (it.reminder.id == item.reminder.id) it.copy(reminder = r.reminder) else it }
                    ReminderScheduler.sync(nuevos.map { it.reminder }, nuevos.map { it.habit })
                    _uiState.update {
                        it.copy(
                            ocupado = null,
                            recordatorios = nuevos.sortedBy { n -> n.reminder.hora },
                            mensaje = when {
                                activo == true -> "Recordatorio de ${item.habit.nombre} activado"
                                activo == false -> "Recordatorio de ${item.habit.nombre} pausado"
                                else -> "Hora de ${item.habit.nombre} actualizada"
                            }
                        )
                    }
                }
                is SaveReminderResult.Error -> _uiState.update { it.copy(ocupado = null, mensaje = r.message) }
            }
        }
    }
}
