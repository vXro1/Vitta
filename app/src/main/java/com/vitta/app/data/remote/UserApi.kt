package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.AuthResponse
import com.vitta.app.data.remote.dto.ChangePasswordRequest
import com.vitta.app.data.remote.dto.ChangePasswordResponse
import com.vitta.app.data.remote.dto.CreateReminderRequest
import com.vitta.app.data.remote.dto.DashboardDto
import com.vitta.app.data.remote.dto.PerfilDto
import com.vitta.app.data.remote.dto.PointMovementDto
import com.vitta.app.data.remote.dto.ReminderDto
import com.vitta.app.data.remote.dto.UpdateProfileRequest
import com.vitta.app.data.remote.dto.UpdateReminderRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Datos del usuario autenticado: perfil, dashboard y recordatorios (requieren token). */
interface UserApi {
    /** Backend: GET /api/users/me — perfil con hábitos activos, logros obtenidos y puntos. */
    @GET("users/me")
    suspend fun getPerfil(): PerfilDto

    /** Backend: PATCH /api/users/me — cambia nombre y/o correo; devuelve el usuario y un token nuevo. */
    @PATCH("users/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): AuthResponse

    /** Backend: PATCH /api/users/me/password — verifica la actual, guarda la nueva y devuelve un token nuevo. */
    @PATCH("users/me/password")
    suspend fun changePassword(@Body body: ChangePasswordRequest): ChangePasswordResponse

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

    /** Backend: DELETE /api/recordatorios/:id — borra el recordatorio (el hábito queda igual). */
    @DELETE("recordatorios/{id}")
    suspend fun deleteReminder(@Path("id") id: Int): Response<Unit>

    /** Backend: GET /api/points/me/movements?limit= — historial real de VitaPuntos (máx. 100). */
    @GET("points/me/movements")
    suspend fun listMovements(@Query("limit") limit: Int = 100): List<PointMovementDto>
}
