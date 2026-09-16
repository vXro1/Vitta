package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.CreateHabitRequest
import com.vitta.app.data.remote.dto.CreateRecordRequest
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.remote.dto.StreakDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface HabitApi {
    @GET("habits")
    suspend fun listHabits(): List<HabitDto>

    @POST("habits")
    suspend fun createHabit(@Body body: CreateHabitRequest): HabitDto

    @GET("habits/{id}/records")
    suspend fun listRecords(@Path("id") habitId: Int): List<RecordDto>

    @POST("habits/{id}/records")
    suspend fun createRecord(@Path("id") habitId: Int, @Body body: CreateRecordRequest): RecordDto

    @GET("habits/{id}/streak")
    suspend fun getStreak(@Path("id") habitId: Int): StreakDto
}