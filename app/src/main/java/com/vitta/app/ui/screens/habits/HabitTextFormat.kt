package com.vitta.app.ui.screens.habits

/**
 * Formato de los campos de texto libre del backend:
 *  - `habitos.meta`: "<cantidad> <unidad>", p. ej. "30 minutos", "8 vasos".
 *  - `habitos.frecuencia`: "Diaria" o los días separados por coma, "L,M,X".
 * Es el mismo formato que la app ya enviaba; aquí solo se centraliza para
 * que crear, editar y el inicio lo lean igual.
 */
object HabitTextFormat {

    const val DAILY = "Diaria"

    /** "30 minutos" → (30, "minutos"). Si no hay número → (null, texto completo). */
    fun parseMeta(meta: String): Pair<Int?, String> {
        val match = Regex("""\d+""").find(meta)
        val numero = match?.value?.toIntOrNull()
        val unidad = if (match != null) meta.removeRange(match.range).trim() else meta.trim()
        return numero to unidad
    }

    fun buildMeta(cantidad: Int, unidad: String): String = "$cantidad ${unidad.trim()}".trim()

    /** "Diaria" → (false, todos). "L,X,V" → (true, {L,X,V}). */
    fun parseFrecuencia(frecuencia: String): Pair<Boolean, Set<String>> {
        val codes = WeekDays.map { it.first }.toSet()
        val days = frecuencia.split(",").map { it.trim() }.filter { it in codes }.toSet()
        return if (days.isEmpty()) false to setOf("L", "M", "X", "J", "V") else true to days
    }

    fun buildFrecuencia(diasEspecificos: Boolean, days: Set<String>): String =
        if (diasEspecificos) orderedDays(days).joinToString(",") else DAILY

    /** ¿El hábito toca hoy? `todayCode` es la letra del día (L..D). */
    fun isScheduledOn(frecuencia: String, todayCode: String): Boolean {
        val (especificos, days) = parseFrecuencia(frecuencia)
        return !especificos || todayCode in days
    }

    fun stepFor(unidad: String): Int = when {
        unidad.contains("paso", ignoreCase = true) -> 500
        else -> 1
    }
}
