package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.CreateHabitRequest
import com.vitta.app.data.remote.dto.CreateRecordRequest
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.remote.dto.RecordWriteDto
import com.vitta.app.data.remote.dto.SaveProgressRequest
import com.vitta.app.data.remote.dto.StreakDto
import com.vitta.app.data.remote.dto.UpdateHabitMetaValorRequest
import com.vitta.app.data.remote.dto.UpdateHabitRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
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
    suspend fun createRecord(@Path("id") habitId: Int, @Body body: CreateRecordRequest): RecordWriteDto

    /** Backend: PUT /api/habits/:id parcial — aquí solo `meta_valor` y `unidad`. */
    @PUT("habits/{id}")
    suspend fun updateHabitMetaValor(@Path("id") habitId: Int, @Body body: UpdateHabitMetaValorRequest): HabitDto

    /**
     * Backend: PATCH /api/habits/:id/progreso — avance del día `{valor}` (total acumulado).
     * Crea o actualiza el registro del día; queda cumplido solo cuando `valor >= meta_valor`.
     */
    @PATCH("habits/{id}/progreso")
    suspend fun saveProgress(@Path("id") habitId: Int, @Body body: SaveProgressRequest): RecordWriteDto

    @GET("habits/{id}/streak")
    suspend fun getStreak(@Path("id") habitId: Int): StreakDto
}