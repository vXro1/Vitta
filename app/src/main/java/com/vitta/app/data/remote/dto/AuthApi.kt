package com.vitta.app.data.remote

import com.vitta.app.data.remote.dto.AuthResponse
import com.vitta.app.data.remote.dto.LoginRequest
import com.vitta.app.data.remote.dto.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse
}