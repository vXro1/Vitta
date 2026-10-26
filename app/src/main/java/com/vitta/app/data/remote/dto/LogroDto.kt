package com.vitta.app.data.remote.dto

/**
 * Un logro del catálogo tal como lo devuelve `GET /api/logros`: el backend
 * lista todos los logros y marca con [obtenido] los que ya ganó el usuario
 * autenticado ([obtenido_en] es null si aún no lo tiene).
 */
data class LogroDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val icono: String?,
    val condicion: String,
    val puntos_bonus: Int,
    val obtenido: Boolean,
    val obtenido_en: String?
)
