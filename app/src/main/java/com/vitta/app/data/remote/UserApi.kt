package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.CreateReminderRequest
import com.vitta.app.data.remote.dto.DashboardDto
import com.vitta.app.data.remote.dto.PerfilDto
import com.vitta.app.data.remote.dto.ReminderDto
import com.vitta.app.data.remote.dto.UpdateReminderRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** Datos del usuario autenticado: perfil, dashboard y recordatorios (requieren token). */
interface UserApi {
    /** Backend: GET /api/users/me — perfil con hábitos activos, logros obtenidos y puntos. */
    @GET("users/me")
    suspend fun getPerfil(): PerfilDto

    /** Backend: GET /api/dashboard — puntos/nivel y rachas por hábito (`activa`). */
    @GET("dashboard")
    suspend fun getDashboard(): DashboardDto

    /** Backend: GET /api/recordatorios — hora local de cada hábito activo. */
    @GET("recordatorios")
    suspend fun listReminders(): List<ReminderDto>

    @POST("recordatorios")
    suspend fun createReminder(@Body body: CreateReminderRequest): ReminderDto

    @PATCH("recordatorios/{id}")
    suspend fun updateReminder(@Path("id") id: Int, @Body body: UpdateReminderRequest): ReminderDto
}
