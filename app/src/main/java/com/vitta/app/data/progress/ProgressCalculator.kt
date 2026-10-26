package com.vitta.app.data.progress

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Convierte un [ProgressHistory] en lo que muestra la pestaña Progreso.
 * Es lógica pura (sin red ni Android): funciona igual con datos de prueba
 * y con datos reales, y se puede probar con tests unitarios.
 */
object ProgressCalculator {

    fun summarize(history: ProgressHistory, period: ProgressPeriod, today: LocalDate): ProgressSummary {
        val habits = history.habits
        val (from, to) = rangeOf(period, today)
        val (prevFrom, prevTo) = when (period) {
            ProgressPeriod.Week -> from.minusWeeks(1) to from.minusDays(1)
            ProgressPeriod.Month -> from.minusMonths(1) to from.minusDays(1)
        }

        val bars = when (period) {
            ProgressPeriod.Week -> days(from, to).map { day ->
                val (done, scheduled) = dayTotals(habits, day)
                ProgressBar(
                    label = DayLetters[day.dayOfWeek.value - 1],
                    detail = "${DayNames[day.dayOfWeek.value - 1]} ${day.dayOfMonth} de ${MonthNames[day.monthValue - 1]}",
                    done = done,
                    scheduled = scheduled,
                    isCurrent = day == today,
                    isFuture = day.isAfter(today)
                )
            }
            ProgressPeriod.Month -> weeksOfMonth(from, to).map { (start, end) ->
                val (done, scheduled) = rangeTotals(habits, start, minOf(end, today))
                ProgressBar(
                    label = "${start.dayOfMonth}–${end.dayOfMonth}",
                    detail = "Del ${start.dayOfMonth} al ${end.dayOfMonth} de ${MonthNames[end.monthValue - 1]}",
                    done = done,
                    scheduled = scheduled,
                    isCurrent = !today.isBefore(start) && !today.isAfter(end),
                    isFuture = start.isAfter(today)
                )
            }
        }

        val (totalDone, totalScheduled) = rangeTotals(habits, from, minOf(to, today))
        val (prevDone, prevScheduled) = rangeTotals(habits, prevFrom, prevTo)

        val perfectDays = days(from, minOf(to, today)).count { day ->
            val (done, scheduled) = dayTotals(habits, day)
            scheduled > 0 && done >= scheduled
        }

        val heatStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(4)
        val heatmap = (0 until 35).map { offset ->
            val day = heatStart.plusDays(offset.toLong())
            val (done, scheduled) = dayTotals(habits, day)
            val level = when {
                day.isAfter(today) -> -1
                scheduled == 0 || done == 0 -> 0
                done * 2 < scheduled -> 1
                done < scheduled -> 2
                else -> 3
            }
            HeatCell(day, level, isToday = day == today)
        }

        val trends = habits.mapNotNull { habit ->
            val active = days(from, minOf(to, today)).filter { isScheduled(habit, it) }
            if (active.isEmpty()) null
            else HabitTrend(habit.id, habit.name, done = active.count { it in habit.completedDays }, scheduled = active.size)
        }.sortedWith(compareByDescending<HabitTrend> { it.ratio }.thenBy { it.name })

        return ProgressSummary(
            period = period,
            rangeLabel = rangeLabel(period, from, to),
            bars = bars,
            rate = ratio(totalDone, totalScheduled),
            previousRate = if (prevScheduled == 0) null else ratio(prevDone, prevScheduled),
            totalDone = totalDone,
            totalScheduled = totalScheduled,
            perfectDays = perfectDays,
            bestStreak = habits.maxOfOrNull { it.bestStreak } ?: 0,
            activeHabits = habits.size,
            heatmap = heatmap,
            habits = trends,
            hasData = habits.any { it.completedDays.isNotEmpty() },
            isSample = history.isSample
        )
    }

    /** Racha más larga de días consecutivos en [days]. */
    fun longestRun(days: Set<LocalDate>): Int {
        var best = 0
        for (day in days) {
            if (day.minusDays(1) in days) continue // solo empieza a contar en el inicio de una racha
            var length = 1
            while (day.plusDays(length.toLong()) in days) length++
            best = maxOf(best, length)
        }
        return best
    }

    // ---- Helpers ----

    private fun rangeOf(period: ProgressPeriod, today: LocalDate): Pair<LocalDate, LocalDate> = when (period) {
        ProgressPeriod.Week -> {
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            monday to monday.plusDays(6)
        }
        ProgressPeriod.Month -> today.withDayOfMonth(1) to today.with(TemporalAdjusters.lastDayOfMonth())
    }

    /** Semanas (lunes–domingo) recortadas a los límites del mes. */
    private fun weeksOfMonth(from: LocalDate, to: LocalDate): List<Pair<LocalDate, LocalDate>> {
        val weeks = mutableListOf<Pair<LocalDate, LocalDate>>()
        var start = from
        while (!start.isAfter(to)) {
            val end = minOf(start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)), to)
            weeks += start to end
            start = end.plusDays(1)
        }
        return weeks
    }

    private fun days(from: LocalDate, to: LocalDate): List<LocalDate> =
        if (to.isBefore(from)) emptyList()
        else generateSequence(from) { it.plusDays(1) }.takeWhile { !it.isAfter(to) }.toList()

    private fun isScheduled(habit: HabitHistory, day: LocalDate): Boolean =
        (habit.startDate == null || !day.isBefore(habit.startDate)) && day.dayOfWeek in habit.schedule

    /** (cumplidos, programados) de un día. Un cumplimiento solo cuenta si ese día tocaba. */
    private fun dayTotals(habits: List<HabitHistory>, day: LocalDate): Pair<Int, Int> {
        val scheduled = habits.filter { isScheduled(it, day) }
        return scheduled.count { day in it.completedDays } to scheduled.size
    }

    private fun rangeTotals(habits: List<HabitHistory>, from: LocalDate, to: LocalDate): Pair<Int, Int> =
        days(from, to).fold(0 to 0) { (d, s), day ->
            val (done, scheduled) = dayTotals(habits, day)
            (d + done) to (s + scheduled)
        }

    private fun ratio(done: Int, scheduled: Int): Float = if (scheduled == 0) 0f else done.toFloat() / scheduled

    private fun rangeLabel(period: ProgressPeriod, from: LocalDate, to: LocalDate): String = when (period) {
        ProgressPeriod.Week ->
            if (from.month == to.month) "Semana del ${from.dayOfMonth} al ${to.dayOfMonth} de ${MonthNames[to.monthValue - 1]}"
            else "Semana del ${from.dayOfMonth} de ${MonthNames[from.monthValue - 1]} al ${to.dayOfMonth} de ${MonthNames[to.monthValue - 1]}"
        ProgressPeriod.Month -> MonthNames[from.monthValue - 1].replaceFirstChar { it.uppercase() } + " de ${from.year}"
    }

    val DayLetters = listOf("L", "M", "M", "J", "V", "S", "D")
    private val DayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    private val MonthNames = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )
}
