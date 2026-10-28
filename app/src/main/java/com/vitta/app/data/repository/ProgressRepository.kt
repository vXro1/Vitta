package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.DailyProgressDto
import retrofit2.HttpException
import java.time.LocalDate

sealed class DailyProgressResult {
    data class Success(val days: List<DailyProgressDto>) : DailyProgressResult()
    /** El servidor no publica `GET /api/progress` (404). */
    object Unavailable : DailyProgressResult()
    data class Error(val message: String) : DailyProgressResult()
}

class ProgressRepository {

    /** `GET /api/progress?desde&hasta` (días locales, ambos incluidos). */
    suspend fun dailyProgress(desde: LocalDate, hasta: LocalDate): DailyProgressResult = try {
        DailyProgressResult.Success(ApiClient.progressApi.getProgress(desde.toString(), hasta.toString()))
    } catch (e: HttpException) {
        if (e.code() == 404) DailyProgressResult.Unavailable
        else DailyProgressResult.Error("No se pudo cargar tu progreso (${e.code()})")
    } catch (e: Exception) {
        DailyProgressResult.Error("No se pudo conectar. Revisa tu conexión.")
    }
}
