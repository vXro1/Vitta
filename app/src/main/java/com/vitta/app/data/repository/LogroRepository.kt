package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.LogroDto
import retrofit2.HttpException

sealed class LogrosResult {
    data class Success(val logros: List<LogroDto>) : LogrosResult()
    data class Error(val message: String) : LogrosResult()
}

class LogroRepository {

    suspend fun listLogros(): LogrosResult = try {
        LogrosResult.Success(ApiClient.logroApi.listLogros())
    } catch (e: HttpException) {
        LogrosResult.Error(
            if (e.code() == 401) "Tu sesión expiró. Vuelve a iniciar sesión."
            else "No se pudieron cargar tus logros (${e.code()})"
        )
    } catch (e: Exception) {
        LogrosResult.Error("No se pudo conectar. Revisa tu conexión.")
    }
}
