package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.DashboardDto
import com.vitta.app.data.remote.dto.PerfilDto
import retrofit2.http.GET
import retrofit2.http.Query

interface GamificationApi {
    @GET("dashboard")
    suspend fun getDashboard(@Query("semanas") semanas: Int = 1): DashboardDto

    @GET("users/me")
    suspend fun getPerfil(): PerfilDto
}
