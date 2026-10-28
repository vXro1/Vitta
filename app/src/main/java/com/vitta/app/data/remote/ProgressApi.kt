package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.DailyProgressDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ProgressApi {
    /**
     * Backend: GET /api/progress?desde=YYYY-MM-DD&hasta=YYYY-MM-DD — requiere token.
     * Sin parámetros devuelve los últimos 7 días; máximo 366 días por consulta.
     */
    @GET("progress")
    suspend fun getProgress(
        @Query("desde") desde: String?,
        @Query("hasta") hasta: String?
    ): List<DailyProgressDto>
}
