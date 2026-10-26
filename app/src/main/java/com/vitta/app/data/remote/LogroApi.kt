package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.LogroDto
import retrofit2.http.GET

interface LogroApi {
    /** Backend: GET /api/logros — requiere token (lo añade el interceptor de [ApiClient]). */
    @GET("logros")
    suspend fun listLogros(): List<LogroDto>
}
