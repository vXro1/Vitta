package com.vitta.app.data.remote.dto

/**
 * Un día de `GET /api/progress?desde=&hasta=`: resumen de todos los hábitos
 * del usuario. No trae el detalle por hábito.
 */
data class DailyProgressDto(
    /** Día local del usuario, `YYYY-MM-DD`. */
    val fecha: String,
    /** Hábitos que tocaban ese día. */
    val total: Int,
    /** De esos, los cumplidos. */
    val completados: Int,
    /** De esos, los que tienen avance (`valor > 0`) sin estar cumplidos. */
    val parciales: Int
)
