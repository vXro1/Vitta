package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.CreateHabitRequest
import com.vitta.app.data.remote.dto.CreateRecordRequest
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.remote.dto.RecordWriteDto
import com.vitta.app.data.remote.dto.SaveProgressRequest
import com.vitta.app.data.remote.dto.StreakDto
import com.vitta.app.data.remote.dto.UpdateHabitMetaValorRequest
import com.vitta.app.data.remote.dto.UpdateHabitRequest
import retrofit2.HttpException

sealed class HabitsResult {
    data class Success(val habits: List<HabitDto>) : HabitsResult()
    data class Error(val message: String) : HabitsResult()
}

sealed class CreateHabitResult {
    data class Success(val habit: HabitDto) : CreateHabitResult()
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

sealed class SaveProgressResult {
    /** El backend guardó el avance; [record].cumplido dice si ya se alcanzó la meta. */
    data class Success(val record: RecordWriteDto) : SaveProgressResult()
    /**
     * El backend no admite avance parcial para este hábito: el hábito no tiene
     * `meta_valor` (400) o el servidor no publica `PATCH /progreso` (404).
     */
    object Unsupported : SaveProgressResult()
    data class Error(val message: String) : SaveProgressResult()
}

sealed class CreateRecordResult {
    data class Success(val record: RecordWriteDto) : CreateRecordResult()
    /** 409: el backend ya tiene un registro de ese hábito para ese día (UTC); el hábito ya está hecho. */
    object AlreadyRegistered : CreateRecordResult()
    data class Error(val message: String) : CreateRecordResult()
}

sealed class DeleteHabitResult {
    /** [archivado] = true si tenía historial y el backend lo archivó en vez de borrarlo. */
    data class Success(val archivado: Boolean) : DeleteHabitResult()
    data class Error(val message: String) : DeleteHabitResult()
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

    /**
     * Crea el hábito con su meta numérica ([metaValor] + [unidad]) para que el
     * avance parcial (`PATCH /progreso`) funcione desde el primer día, y con
     * [icono]/[categoria] para que cuente en los logros por área.
     */
    suspend fun createHabit(
        nombre: String,
        meta: String,
        frecuencia: String,
        tipo: String,
        metaValor: Double? = null,
        unidad: String? = null,
        icono: String? = null,
        categoria: String? = null
    ): CreateHabitResult = try {
        CreateHabitResult.Success(
            ApiClient.habitApi.createHabit(
                CreateHabitRequest(nombre, meta, frecuencia, tipo, metaValor, unidad, icono, categoria)
            )
        )
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

    suspend fun updateHabit(
        habitId: Int,
        nombre: String,
        meta: String,
        frecuencia: String,
        metaValor: Double? = null,
        unidad: String? = null,
        icono: String? = null,
        categoria: String? = null
    ): HabitResult = try {
        HabitResult.Success(
            ApiClient.habitApi.updateHabit(
                habitId,
                UpdateHabitRequest(nombre, meta, frecuencia, metaValor, unidad, icono, categoria)
            )
        )
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

    suspend fun createRecord(
        habitId: Int,
        fecha: String,
        cumplido: Boolean,
        valor: Double?,
        notas: String? = null
    ): CreateRecordResult = try {
        CreateRecordResult.Success(
            ApiClient.habitApi.createRecord(habitId, CreateRecordRequest(fecha, cumplido, valor, notas))
        )
    } catch (e: HttpException) {
        if (e.code() == 409) CreateRecordResult.AlreadyRegistered
        else CreateRecordResult.Error("No se pudo registrar (${e.code()})")
    } catch (e: Exception) {
        CreateRecordResult.Error("No se pudo conectar.")
    }

    /** null = listo; texto = mensaje de error para mostrar. */
    suspend fun deleteRecord(habitId: Int, registroId: Int): String? = try {
        val response = ApiClient.habitApi.deleteRecord(habitId, registroId)
        when {
            response.isSuccessful || response.code() == 404 -> null
            else -> "No se pudo desmarcar (${response.code()})"
        }
    } catch (e: Exception) {
        "No se pudo conectar."
    }

    /**
     * `DELETE /api/habits/:id`: si el hábito no tiene registros se borra; si
     * tiene historial el backend lo archiva (sale de los listados y conserva
     * sus días, puntos y medallas).
     */
    suspend fun deleteHabit(habitId: Int): DeleteHabitResult = try {
        val response = ApiClient.habitApi.deleteHabit(habitId)
        val body = response.body()
        when {
            response.isSuccessful -> DeleteHabitResult.Success(archivado = body?.archivado == true)
            response.code() == 404 -> DeleteHabitResult.Success(archivado = false)
            else -> DeleteHabitResult.Error("No se pudo eliminar (${response.code()})")
        }
    } catch (e: Exception) {
        DeleteHabitResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    /**
     * Guarda el avance acumulado del día con `PATCH /api/habits/:id/progreso`.
     * [notas]: null = no tocar la nota del día; "" = borrarla.
     */
    suspend fun saveProgress(habitId: Int, valor: Double, notas: String? = null): SaveProgressResult = try {
        SaveProgressResult.Success(ApiClient.habitApi.saveProgress(habitId, SaveProgressRequest(valor, notas)))
    } catch (e: HttpException) {
        when (e.code()) {
            400, 404 -> SaveProgressResult.Unsupported
            409 -> SaveProgressResult.Error("No se pudo guardar tu avance. Inténtalo de nuevo.")
            else -> SaveProgressResult.Error("No se pudo guardar tu avance (${e.code()})")
        }
    } catch (e: Exception) {
        SaveProgressResult.Error("No se pudo conectar. Tu avance se guardará al reintentar.")
    }

    /** Alinea `meta_valor` del backend con la meta que muestra la app ("8 vasos" → 8). */
    suspend fun updateMetaValor(habitId: Int, metaValor: Double, unidad: String?): HabitResult = try {
        HabitResult.Success(
            ApiClient.habitApi.updateHabitMetaValor(habitId, UpdateHabitMetaValorRequest(metaValor, unidad))
        )
    } catch (e: HttpException) {
        HabitResult.Error("No se pudo actualizar la meta (${e.code()})")
    } catch (e: Exception) {
        HabitResult.Error("No se pudo conectar.")
    }

    suspend fun getStreak(habitId: Int): StreakResult = try {
        StreakResult.Success(ApiClient.habitApi.getStreak(habitId))
    } catch (e: HttpException) {
        StreakResult.Error("No se pudo cargar la racha (${e.code()})")
    } catch (e: Exception) {
        StreakResult.Error("No se pudo conectar.")
    }
}