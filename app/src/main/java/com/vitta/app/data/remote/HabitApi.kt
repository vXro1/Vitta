package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.CreateHabitRequest
import com.vitta.app.data.remote.dto.CreateRecordRequest
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.remote.dto.StreakDto
import com.vitta.app.data.remote.dto.UpdateHabitRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface HabitApi {
    @GET("habits")
    suspend fun listHabits(): List<HabitDto>

    @POST("habits")
    suspend fun createHabit(@Body body: CreateHabitRequest): HabitDto

    @GET("habits/{id}")
    suspend fun getHabit(@Path("id") habitId: Int): HabitDto

    /** Backend: PUT /api/habits/:id — acepta nombre, meta y frecuencia (todos opcionales). */
    @PUT("habits/{id}")
    suspend fun updateHabit(@Path("id") habitId: Int, @Body body: UpdateHabitRequest): HabitDto

    @GET("habits/{id}/records")
    suspend fun listRecords(@Path("id") habitId: Int): List<RecordDto>

    @POST("habits/{id}/records")
    suspend fun createRecord(@Path("id") habitId: Int, @Body body: CreateRecordRequest): RecordDto

    @GET("habits/{id}/streak")
    suspend fun getStreak(@Path("id") habitId: Int): StreakDto

    /** Desmarcar: borra el registro y sus VitaPuntos y recalcula la racha. */
    @DELETE("habits/{id}/records/{registroId}")
    suspend fun deleteRecord(@Path("id") habitId: Int, @Path("registroId") registroId: Int): Response<Unit>

    /** 409 si el hábito ya tiene historial (el backend no borra registros). */
    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") habitId: Int): Response<Unit>
}