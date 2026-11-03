package com.vitta.app.data.remote


import com.vitta.app.data.remote.dto.UsuarioDto
import retrofit2.http.GET

interface UserApi {
    @GET("users/me")
    suspend fun getMe(): UsuarioDto
}