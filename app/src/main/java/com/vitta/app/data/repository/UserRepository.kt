package com.vitta.app.data.repository

import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.UsuarioDto
import retrofit2.HttpException
import java.io.IOException

sealed class ProfileResult {
    data class Success(val user: UsuarioDto) : ProfileResult()
    data class Error(val message: String) : ProfileResult()
}

class UserRepository {

    suspend fun getMe(): ProfileResult = try {
        ProfileResult.Success(ApiClient.userApi.getMe())
    } catch (e: HttpException) {
        ProfileResult.Error("No se pudo cargar el perfil (${e.code()})")
    } catch (e: IOException) {
        ProfileResult.Error("Sin conexión con el servidor")
    }
}