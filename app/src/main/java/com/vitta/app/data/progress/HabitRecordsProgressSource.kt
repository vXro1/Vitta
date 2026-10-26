package com.vitta.app.data.progress

import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.data.repository.StreakResult
import com.vitta.app.ui.screens.habits.HabitTextFormat
import com.vitta.app.ui.screens.habits.WeekDays
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Historial REAL, armado con los endpoints que el frontend ya usa:
 * `GET /api/habits`, `GET /api/habits/:id/records` y `GET /api/habits/:id/streak`.
 * Los días se toman en UTC, igual que el backend (un registro por hábito y día UTC).
 *
 * Se activa en [ProgressSources]; no requiere cambios en la pantalla.
 */
class HabitRecordsProgressSource(
    private val repository: HabitRepository = HabitRepository()
) : ProgressSource {

    override suspend fun loadHistory(): ProgressSourceResult {
        val habits = when (val r = repository.listHabits()) {
            is HabitsResult.Success -> r.habits
            is HabitsResult.Error -> return ProgressSourceResult.Error(r.message)
        }
        val histories = coroutineScope { habits.map { async { historyOf(it) } }.awaitAll() }
        if (histories.any { it == null }) {
            return ProgressSourceResult.Error("No se pudo cargar tu historial. Revisa tu conexión.")
        }
        return ProgressSourceResult.Success(ProgressHistory(histories.filterNotNull(), isSample = false))
    }

    private suspend fun historyOf(habit: HabitDto): HabitHistory? {
        val records = when (val r = repository.listRecords(habit.id)) {
            is RecordsResult.Success -> r.records
            is RecordsResult.Error -> return null
        }
        val completed = records.filter { it.cumplido }.mapNotNull { utcDay(it.fecha) }.toSet()
        val streakMax = (repository.getStreak(habit.id) as? StreakResult.Success)?.streak?.racha_maxima ?: 0
        return HabitHistory(
            id = habit.id,
            name = habit.nombre,
            schedule = DayOfWeek.entries.filter { day ->
                HabitTextFormat.isScheduledOn(habit.frecuencia, WeekDays[day.value - 1].first)
            }.toSet(),
            completedDays = completed,
            bestStreak = maxOf(streakMax, ProgressCalculator.longestRun(completed)),
            // Un registro implica que el hábito ya existía ese día, aunque se haya creado después.
            startDate = listOfNotNull(utcDay(habit.creado_en), completed.minOrNull()).minOrNull()
        )
    }

    private fun utcDay(iso: String): LocalDate? =
        runCatching { Instant.parse(iso).atZone(ZoneOffset.UTC).toLocalDate() }
            .recoverCatching { LocalDate.parse(iso.take(10)) }
            .getOrNull()
}
