package com.vitta.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.data.repository.RemindersResult
import com.vitta.app.data.repository.StreakResult
import com.vitta.app.data.repository.UserRepository
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconStore
import com.vitta.app.ui.components.icons.HabitIcons
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/** Un día del historial del hábito (un registro del backend). */
data class HabitDayRecord(
    val id: Int,
    val dia: LocalDate,
    val cumplido: Boolean,
    val valor: Double?,
    val nota: String?
)

/** Cumplimiento en una ventana de días (solo cuenta los días que tocaba el hábito). */
data class Cumplimiento(val hechos: Int, val programados: Int) {
    val ratio: Float get() = if (programados == 0) 0f else (hechos.toFloat() / programados).coerceIn(0f, 1f)
}

data class HabitDetailUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    val habit: HabitDto? = null,
    val icon: HabitIcon = HabitIcons.Default,
    val metaValor: Int? = null,
    val metaUnidad: String = "",
    val hora: LocalTime? = null,
    val rachaActual: Int = 0,
    val rachaMaxima: Int = 0,
    val hoy: HabitDayRecord? = null,
    val semana: Cumplimiento = Cumplimiento(0, 0),
    val mes: Cumplimiento = Cumplimiento(0, 0),
    val totalCumplidos: Int = 0,
    val historial: List<HabitDayRecord> = emptyList()
)

/**
 * Avance de un hábito (HAL-09): progreso de hoy, racha, cumplimiento de los
 * últimos 7 y 30 días e historial, todo de `GET /api/habits/:id`,
 * `/records`, `/streak` y `/api/recordatorios`.
 */
class HabitDetailViewModel(
    private val habitId: Int,
    private val repository: HabitRepository = HabitRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    fun cargar() {
        _uiState.update { it.copy(cargando = it.habit == null, error = null) }
        viewModelScope.launch {
            val recordsCall = async { repository.listRecords(habitId) }
            val streakCall = async { repository.getStreak(habitId) }
            val remindersCall = async { userRepository.listReminders() }
            val habit = when (val r = repository.getHabit(habitId)) {
                is HabitResult.Success -> r.habit
                is HabitResult.Error -> {
                    _uiState.update { it.copy(cargando = false, error = r.message) }
                    return@launch
                }
            }
            val records = when (val r = recordsCall.await()) {
                is RecordsResult.Success -> r.records
                is RecordsResult.Error -> {
                    _uiState.update { it.copy(cargando = false, error = r.message) }
                    return@launch
                }
            }
            val streak = (streakCall.await() as? StreakResult.Success)?.streak
            val hora = (remindersCall.await() as? RemindersResult.Success)?.reminders
                ?.firstOrNull { it.habito_id == habitId }?.let { HabitTextFormat.parseHora(it.hora) }

            val zona = ZoneId.systemDefault()
            val hoy = LocalDate.now(zona)
            val dias = records.mapNotNull { it.toDay(zona) }
            val cumplidos = dias.filter { it.cumplido }.map { it.dia }.toSet()
            val inicio = listOfNotNull(
                runCatching { Instant.parse(habit.creado_en).atZone(zona).toLocalDate() }.getOrNull(),
                cumplidos.minOrNull()
            ).minOrNull() ?: hoy
            val (cantidad, unidad) = HabitTextFormat.parseMeta(habit.meta)

            _uiState.update {
                it.copy(
                    cargando = false,
                    habit = habit,
                    icon = HabitIcons.byId(habit.icono) ?: HabitIconStore.iconFor(habit.id, habit.nombre),
                    metaValor = cantidad ?: habit.meta_valor?.toInt(),
                    metaUnidad = unidad.ifBlank { habit.unidad.orEmpty() },
                    hora = hora,
                    rachaActual = streak?.racha_actual ?: 0,
                    rachaMaxima = streak?.racha_maxima ?: 0,
                    hoy = dias.lastOrNull { d -> d.dia == hoy },
                    semana = cumplimiento(habit.frecuencia, cumplidos, inicio, hoy.minusDays(6), hoy),
                    mes = cumplimiento(habit.frecuencia, cumplidos, inicio, hoy.minusDays(29), hoy),
                    totalCumplidos = cumplidos.size,
                    historial = dias.sortedWith(compareByDescending<HabitDayRecord> { d -> d.dia }.thenByDescending { d -> d.id })
                )
            }
        }
    }

    private fun cumplimiento(frecuencia: String, cumplidos: Set<LocalDate>, inicio: LocalDate, desde: LocalDate, hasta: LocalDate): Cumplimiento {
        var hechos = 0
        var programados = 0
        var d = maxOf(desde, inicio)
        while (!d.isAfter(hasta)) {
            if (HabitTextFormat.isScheduledOn(frecuencia, WeekDays[d.dayOfWeek.value - 1].first)) {
                programados++
                if (d in cumplidos) hechos++
            }
            d = d.plusDays(1)
        }
        return Cumplimiento(hechos, programados)
    }
}

private fun RecordDto.toDay(zona: ZoneId): HabitDayRecord? {
    val d = dia?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: runCatching { Instant.parse(fecha).atZone(zona).toLocalDate() }.getOrNull()
        ?: return null
    return HabitDayRecord(id, d, cumplido, valor, notas?.takeIf { it.isNotBlank() })
}

class HabitDetailViewModelFactory(private val habitId: Int) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HabitDetailViewModel(habitId) as T
    }
}
