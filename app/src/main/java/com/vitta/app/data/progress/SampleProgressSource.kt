package com.vitta.app.data.progress

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.random.Random

/**
 * Datos de prueba de la pestaña Progreso: cinco hábitos diarios con el mismo
 * nivel de cumplimiento que muestra el mockup en "Por hábito"
 * (93 %, 88 %, 71 %, 64 %, 52 %). Son deterministas: la misma fecha da
 * siempre el mismo resultado, así la pantalla no "baila" al recargar.
 */
class SampleProgressSource(
    private val today: () -> LocalDate = { LocalDate.now() },
    private val daysOfHistory: Long = 70
) : ProgressSource {

    private data class SampleHabit(val id: Int, val name: String, val rate: Float)

    private val habits = listOf(
        SampleHabit(1, "Tomar agua", .93f),
        SampleHabit(2, "Leer", .88f),
        SampleHabit(3, "Hacer yoga", .71f),
        SampleHabit(4, "Caminar", .64f),
        SampleHabit(5, "Dormir", .52f)
    )

    override suspend fun loadHistory(): ProgressSourceResult {
        val end = today()
        val start = end.minusDays(daysOfHistory)
        val history = habits.map { habit ->
            val completed = generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .filter { day -> Random(day.toEpochDay() * 131 + habit.id).nextFloat() < habit.rate }
                .toSet()
            HabitHistory(
                id = habit.id,
                name = habit.name,
                schedule = DayOfWeek.entries.toSet(),
                completedDays = completed,
                bestStreak = ProgressCalculator.longestRun(completed),
                startDate = start
            )
        }
        return ProgressSourceResult.Success(ProgressHistory(history, isSample = true))
    }
}
