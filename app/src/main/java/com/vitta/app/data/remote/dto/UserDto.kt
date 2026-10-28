package com.vitta.app.data.remote.dto

/** VitaPuntos y nivel (`UserPoints` del backend). */
data class UserPointsDto(
    val puntos_totales: Int,
    val nivel: Int,
    val nombre_nivel: String,
    /** Puntos acumulados dentro del nivel actual. */
    val puntos_en_nivel: Int,
    /** Puntos que faltan para el siguiente nivel; null en el nivel máximo. */
    val puntos_para_siguiente: Int?,
    /** Puntos totales donde empieza el siguiente nivel; null en el nivel máximo. */
    val puntos_nivel_siguiente: Int?
) {
    /** Avance 0–1 dentro del nivel actual. */
    val progresoNivel: Float
        get() {
            val siguiente = puntos_nivel_siguiente ?: return 1f
            val inicio = puntos_totales - puntos_en_nivel
            val rango = siguiente - inicio
            return if (rango <= 0) 1f else (puntos_en_nivel.toFloat() / rango).coerceIn(0f, 1f)
        }
}

/** `GET /api/users/me`. */
data class PerfilDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val creado_en: String,
    val habitos_activos: Int,
    val logros_obtenidos: Int,
    val puntos: UserPointsDto?
)

data class RachaRefDto(
    val habito_id: Int,
    val nombre: String,
    val dias: Int
)

data class RachaHabitoDto(
    val habito_id: Int,
    val nombre: String,
    /** Última racha guardada; si [activa] es false, es la racha que se cortó. */
    val racha_actual: Int,
    val racha_maxima: Int,
    /** false si ya pasó un día programado sin cumplir (la racha se perdió). */
    val activa: Boolean
)

data class DashboardRachasDto(
    val activa_mas_larga: RachaRefDto?,
    val maxima: RachaRefDto?,
    val por_habito: List<RachaHabitoDto>?
)

/** `GET /api/dashboard` (solo los campos que usa la app). */
data class DashboardDto(
    val fecha: String,
    val puntos: UserPointsDto?,
    val rachas: DashboardRachasDto?
)

/** `GET /api/recordatorios`: hora local `HH:MM` de un hábito. */
data class ReminderDto(
    val id: Int,
    val habito_id: Int,
    val hora: String,
    val activo: Boolean
)

data class CreateReminderRequest(
    val habito_id: Int,
    val hora: String,
    val activo: Boolean = true
)

data class UpdateReminderRequest(
    val hora: String
)
