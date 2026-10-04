package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.CreateHabitRequest
import com.vitta.app.data.remote.dto.CreateRecordRequest
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.remote.dto.StreakDto
import com.vitta.app.data.remote.dto.UpdateHabitRequest
import retrofit2.HttpException

sealed class HabitsResult {
    data class Success(val habits: List<HabitDto>) : HabitsResult()
    data class Error(val message: String) : HabitsResult()
}

sealed class CreateHabitResult {
    object Success : CreateHabitResult()
    data class Error(val message: String) : CreateHabitResult()
}

sealed class HabitResult {
    data class Success(val habit: HabitDto) : HabitResult()
    data class Error(val message: String) : HabitResult()
}

sealed class RecordsResult {
    data class Success(val records: List<RecordDto>) : RecordsResult()
    data class Error(val message: String) : RecordsResult()
}

sealed class CreateRecordResult {
    data class Success(val record: RecordDto) : CreateRecordResult()
    data class Error(val message: String) : CreateRecordResult()
}

sealed class StreakResult {
    data class Success(val streak: StreakDto) : StreakResult()
    data class Error(val message: String) : StreakResult()
}

class HabitRepository {

    suspend fun listHabits(): HabitsResult = try {
        HabitsResult.Success(ApiClient.habitApi.listHabits())
    } catch (e: HttpException) {
        HabitsResult.Error("No se pudieron cargar tus hábitos (${e.code()})")
    } catch (e: Exception) {
        HabitsResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    suspend fun createHabit(nombre: String, meta: String, frecuencia: String, tipo: String): CreateHabitResult = try {
        ApiClient.habitApi.createHabit(CreateHabitRequest(nombre, meta, frecuencia, tipo))
        CreateHabitResult.Success
    } catch (e: HttpException) {
        CreateHabitResult.Error("No se pudo guardar \"$nombre\" (${e.code()})")
    } catch (e: Exception) {
        CreateHabitResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    suspend fun getHabit(habitId: Int): HabitResult = try {
        HabitResult.Success(ApiClient.habitApi.getHabit(habitId))
    } catch (e: HttpException) {
        HabitResult.Error(if (e.code() == 404) "Este hábito ya no existe" else "No se pudo cargar el hábito (${e.code()})")
    } catch (e: Exception) {
        HabitResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    suspend fun updateHabit(habitId: Int, nombre: String, meta: String, frecuencia: String): HabitResult = try {
        HabitResult.Success(ApiClient.habitApi.updateHabit(habitId, UpdateHabitRequest(nombre, meta, frecuencia)))
    } catch (e: HttpException) {
        HabitResult.Error("No se pudieron guardar los cambios (${e.code()})")
    } catch (e: Exception) {
        HabitResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    suspend fun listRecords(habitId: Int): RecordsResult = try {
        RecordsResult.Success(ApiClient.habitApi.listRecords(habitId))
    } catch (e: HttpException) {
        RecordsResult.Error("No se pudo cargar el historial (${e.code()})")
    } catch (e: Exception) {
        RecordsResult.Error("No se pudo conectar.")
    }

    suspend fun createRecord(habitId: Int, fecha: String, cumplido: Boolean, valor: Double?): CreateRecordResult = try {
        CreateRecordResult.Success(
            ApiClient.habitApi.createRecord(habitId, CreateRecordRequest(fecha, cumplido, valor))
        )
    } catch (e: HttpException) {
        CreateRecordResult.Error(if (e.code() == 409) "Ya registraste este hábito hoy" else "No se pudo registrar (${e.code()})")
    } catch (e: Exception) {
        CreateRecordResult.Error("No se pudo conectar.")
    }

    suspend fun getStreak(habitId: Int): StreakResult = try {
        StreakResult.Success(ApiClient.habitApi.getStreak(habitId))
    } catch (e: HttpException) {
        StreakResult.Error("No se pudo cargar la racha (${e.code()})")
    } catch (e: Exception) {
        StreakResult.Error("No se pudo conectar.")
    }
}