package com.vitta.app.data.progress

import java.time.DayOfWeek
import java.time.LocalDate

/** Periodo que muestra la pestaña Progreso. */
enum class ProgressPeriod(val label: String) {
    Week("Semana"),
    Month("Mes")
}

// ---------------------------------------------------------------------------
// Entrada: historial de hábitos. Lo produce cualquier [ProgressSource]
// (datos de prueba hoy, registros reales del backend mañana).
// ---------------------------------------------------------------------------

/** Historial de un hábito: qué días le tocaba y qué días se cumplió. */
data class HabitHistory(
    val id: Int,
    val name: String,
    /** Días de la semana en que toca el hábito (los 7 si es diario). */
    val schedule: Set<DayOfWeek>,
    /** Días (UTC, como el backend) con un registro cumplido. */
    val completedDays: Set<LocalDate>,
    /** Racha máxima histórica. */
    val bestStreak: Int,
    /** Día de creación: antes de esa fecha el hábito no cuenta. null = sin límite. */
    val startDate: LocalDate? = null
)

data class ProgressHistory(
    val habits: List<HabitHistory>,
    /** true si el historial es de ejemplo (la UI lo indica). */
    val isSample: Boolean
)

sealed interface ProgressSourceResult {
    data class Success(val history: ProgressHistory) : ProgressSourceResult
    data class Error(val message: String) : ProgressSourceResult
}

/** Origen del historial. Cambiar de datos de prueba a reales = cambiar la implementación. */
fun interface ProgressSource {
    suspend fun loadHistory(): ProgressSourceResult
}

// ---------------------------------------------------------------------------
// Salida: lo que pinta la pantalla. Lo calcula [ProgressCalculator].
// ---------------------------------------------------------------------------

/** Una barra de la gráfica: un día (Semana) o una semana del mes (Mes). */
data class ProgressBar(
    val label: String,
    /** Texto largo para el detalle al tocar la barra ("Jueves 23 de octubre"). */
    val detail: String,
    val done: Int,
    val scheduled: Int,
    val isCurrent: Boolean,
    val isFuture: Boolean
) {
    val ratio: Float get() = if (scheduled == 0) 0f else (done.toFloat() / scheduled).coerceIn(0f, 1f)
}

/** Celda del mapa de calor. [level]: -1 futuro, 0 nada, 1 poco, 2 casi, 3 completo. */
data class HeatCell(
    val date: LocalDate,
    val level: Int,
    val isToday: Boolean
)

/** Cumplimiento de un hábito dentro del periodo. */
data class HabitTrend(
    val id: Int,
    val name: String,
    val done: Int,
    val scheduled: Int
) {
    val ratio: Float get() = if (scheduled == 0) 0f else (done.toFloat() / scheduled).coerceIn(0f, 1f)
}

data class ProgressSummary(
    val period: ProgressPeriod,
    /** "Semana del 20 al 26 de octubre" / "Octubre de 2026". */
    val rangeLabel: String,
    val bars: List<ProgressBar>,
    /** Cumplimiento del periodo hasta hoy (0–1). */
    val rate: Float,
    /** Cumplimiento del periodo anterior completo; null si no hubo nada programado. */
    val previousRate: Float?,
    val totalDone: Int,
    val totalScheduled: Int,
    /** Días del periodo (hasta hoy) en que se cumplió todo lo programado. */
    val perfectDays: Int,
    val bestStreak: Int,
    val activeHabits: Int,
    /** Últimas cinco semanas, de lunes a domingo, 35 celdas. */
    val heatmap: List<HeatCell>,
    val habits: List<HabitTrend>,
    /** false si todavía no hay ningún registro (estado vacío del mockup). */
    val hasData: Boolean,
    val isSample: Boolean
)
