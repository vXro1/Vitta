package com.vitta.app.data.progress

import com.vitta.app.data.repository.DailyProgressResult
import com.vitta.app.data.repository.ProgressRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Progreso REAL con el endpoint agregado del backend, `GET /api/progress?desde&hasta`
 * (`[{fecha, total, completados, parciales}]` por día local).
 *
 * El endpoint no trae el detalle por hábito, así que:
 * - Los totales por día (barras, resumen y mapa de calor) salen de `/api/progress`.
 * - "Por hábito", la mejor racha y el número de hábitos se siguen armando con
 *   [HabitRecordsProgressSource], como hasta ahora.
 * - Si `/api/progress` no responde (servidor anterior o error), se conserva el
 *   cálculo actual completo con [HabitRecordsProgressSource].
 */
class ApiProgressSource(
    private val repository: ProgressRepository = ProgressRepository(),
    private val perHabit: ProgressSource = HabitRecordsProgressSource(),
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.systemDefault()) }
) : ProgressSource {

    override suspend fun loadHistory(): ProgressSourceResult {
        val base = when (val r = perHabit.loadHistory()) {
            is ProgressSourceResult.Success -> r.history
            is ProgressSourceResult.Error -> return r
        }
        val hoy = today()
        val (desde, hasta) = rangeFor(hoy)
        val daily = when (val r = repository.dailyProgress(desde, hasta)) {
            is DailyProgressResult.Success -> r.days.mapNotNull { d ->
                runCatching { LocalDate.parse(d.fecha) }.getOrNull()
                    ?.let { it to DayTotals(done = d.completados, scheduled = d.total) }
            }.toMap()
            // Sin endpoint o con error: se mantiene el cálculo por registros (comportamiento actual).
            DailyProgressResult.Unavailable, is DailyProgressResult.Error -> null
        }
        return ProgressSourceResult.Success(base.copy(dailyTotals = daily))
    }

    /**
     * Cubre todo lo que pinta la pestaña: el mes anterior completo (comparación
     * "Mes"), la semana anterior y las cinco semanas del mapa de calor. Termina
     * hoy: el backend no acepta fechas futuras y esos días se pintan como futuros.
     */
    private fun rangeFor(hoy: LocalDate): Pair<LocalDate, LocalDate> {
        val inicioMesAnterior = hoy.withDayOfMonth(1).minusMonths(1)
        val inicioMapa = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(4)
        val inicioSemanaAnterior = hoy.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)
        return minOf(inicioMesAnterior, inicioMapa, inicioSemanaAnterior) to hoy
    }
}
