package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.CreateHabitRequest
import com.vitta.app.data.remote.dto.HabitDto
import retrofit2.HttpException

sealed class HabitsResult {
    data class Success(val habits: List<HabitDto>) : HabitsResult()
    data class Error(val message: String) : HabitsResult()
}

sealed class CreateHabitResult {
    object Success : CreateHabitResult()
    data class Error(val message: String) : CreateHabitResult()
}

class HabitRepository {

    suspend fun listHabits(): HabitsResult {
        return try {
            HabitsResult.Success(ApiClient.habitApi.listHabits())
        } catch (e: HttpException) {
            HabitsResult.Error("No se pudieron cargar tus hábitos (${e.code()})")
        } catch (e: Exception) {
            HabitsResult.Error("No se pudo conectar. Revisa tu conexión.")
        }
    }

    suspend fun createHabit(
        nombre: String,
        meta: String,
        frecuencia: String,
        tipo: String
    ): CreateHabitResult {
        return try {
            ApiClient.habitApi.createHabit(CreateHabitRequest(nombre, meta, frecuencia, tipo))
            CreateHabitResult.Success
        } catch (e: HttpException) {
            CreateHabitResult.Error("No se pudo guardar \"$nombre\" (${e.code()})")
        } catch (e: Exception) {
            CreateHabitResult.Error("No se pudo conectar. Revisa tu conexión.")
        }
    }
}