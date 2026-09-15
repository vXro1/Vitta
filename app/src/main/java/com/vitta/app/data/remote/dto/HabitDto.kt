package com.vitta.app.data.remote.dto


data class HabitDto(
    val id: Int,
    val usuario_id: Int,
    val nombre: String,
    val meta: String,
    val frecuencia: String,
    val tipo: String,
    val creado_en: String
)

data class CreateHabitRequest(
    val nombre: String,
    val meta: String,
    val frecuencia: String,
    val tipo: String
)

data class RecordDto(
    val id: Int,
    val habito_id: Int,
    val fecha: String,
    val cumplido: Boolean,
    val valor: Double?
)

data class CreateRecordRequest(
    val fecha: String,
    val cumplido: Boolean,
    val valor: Double?
)

data class StreakDto(
    val habito_id: Int,
    val racha_actual: Int,
    val racha_maxima: Int
)