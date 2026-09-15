package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.CreateHabitRequest
import com.vitta.app.data.remote.dto.HabitDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HabitApi {
    @GET("habits")
    suspend fun listHabits(): List<HabitDto>

    @POST("habits")
    suspend fun createHabit(@Body body: CreateHabitRequest): HabitDto
}