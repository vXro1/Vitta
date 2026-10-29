package com.vitta.app.data.remote.dto


data class HabitDto(
    val id: Int,
    val usuario_id: Int,
    val nombre: String,
    val meta: String,
    val frecuencia: String,
    val tipo: String,
    val creado_en: String,
    /** Meta numérica (p. ej. 8 vasos); null si el hábito es solo sí/no. Requerida por `PATCH /progreso`. */
    val meta_valor: Double? = null,
    val unidad: String? = null,
    /** Clave del icono elegido (catálogo de la app, p. ej. "water"); la usan los logros por área. */
    val icono: String? = null,
    /** Área del hábito (p. ej. "Hidratación"); la usa el logro "En Equilibrio". */
    val categoria: String? = null
)

/** POST /api/habits. Los campos opcionales nulos no se envían (Gson omite null). */
data class CreateHabitRequest(
    val nombre: String,
    val meta: String,
    val frecuencia: String,
    val tipo: String,
    val meta_valor: Double? = null,
    val unidad: String? = null,
    val icono: String? = null,
    val categoria: String? = null
)

/** PUT /api/habits/:id (parcial). */
data class UpdateHabitRequest(
    val nombre: String,
    val meta: String,
    val frecuencia: String,
    val meta_valor: Double? = null,
    val unidad: String? = null,
    val icono: String? = null,
    val categoria: String? = null
)

/**
 * `PUT /api/habits/:id` es parcial: aquí solo se envía la meta numérica que
 * necesita `PATCH /api/habits/:id/progreso`, tomada del texto de `meta`.
 */
data class UpdateHabitMetaValorRequest(
    val meta_valor: Double,
    val unidad: String?
)

data class RecordDto(
    val id: Int,
    val habito_id: Int,
    val fecha: String,
    val cumplido: Boolean,
    val valor: Double?,
    /** Día local del usuario (`YYYY-MM-DD`) según `X-Timezone`. */
    val dia: String? = null,
    /** Nota del día (columna `reflexion`); null si no hay. */
    val notas: String? = null
)

/**
 * Respuesta de `POST /records` y `PATCH /progreso`: el registro más sus
 * efectos (racha recalculada y logros recién desbloqueados).
 */
data class RecordWriteDto(
    val id: Int,
    val habito_id: Int,
    val fecha: String,
    val cumplido: Boolean,
    val valor: Double?,
    val dia: String? = null,
    val notas: String? = null,
    val racha: StreakDto? = null,
    val logros_desbloqueados: List<LogroDto>? = null
)

data class CreateRecordRequest(
    val fecha: String,
    val cumplido: Boolean,
    val valor: Double?,
    /** Nota opcional del día; Gson omite null. */
    val notas: String? = null
)

/** `PATCH /api/habits/:id/progreso`: [valor] es el total acumulado del día, no un incremento. */
data class SaveProgressRequest(
    val valor: Double,
    /** null = no tocar la nota; texto vacío = borrarla. */
    val notas: String? = null
)

data class StreakDto(
    val habito_id: Int,
    val racha_actual: Int,
    val racha_maxima: Int
)

/** Respuesta de `DELETE /api/habits/:id`. */
data class DeleteHabitDto(
    val mensaje: String? = null,
    /** true = tenía historial y se archivó en vez de borrarse. */
    val archivado: Boolean = false
)
