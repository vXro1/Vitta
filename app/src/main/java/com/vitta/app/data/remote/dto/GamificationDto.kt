package com.vitta.app.data.remote.dto

/** VitaPuntos y nivel tal como los calcula el backend (spec v2.0 §6.3–6.4). */
data class UserPointsDto(
    val puntos_totales: Int,
    val nivel: Int,
    val nombre_nivel: String,
    val puntos_en_nivel: Int,
    val puntos_para_siguiente: Int?,
    val puntos_nivel_siguiente: Int?
) {
    /** Avance dentro del nivel actual (0–1); 1 en el nivel máximo. */
    val progresoNivel: Float
        get() {
            val siguiente = puntos_para_siguiente ?: return 1f
            val tramo = puntos_en_nivel + siguiente
            return if (tramo <= 0) 0f else puntos_en_nivel.toFloat() / tramo
        }
}

/** `GET /api/users/me` */
data class PerfilDto(
    val id: Int,
    val nombre: String,
    val correo: String,
    val creado_en: String,
    val habitos_activos: Int,
    val logros_obtenidos: Int,
    val puntos: UserPointsDto
)

/** `GET /api/dashboard` (solo los campos que usa la app). */
data class DashboardDto(
    val fecha: String,
    val puntos: UserPointsDto,
    val rachas: DashboardRachasDto
)

data class DashboardRachasDto(
    val activa_mas_larga: RachaDestacadaDto?,
    val maxima: RachaDestacadaDto?
)

data class RachaDestacadaDto(
    val habito_id: Int,
    val nombre: String,
    val dias: Int
)
