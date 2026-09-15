package com.vitta.app.data.repository

import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.LoginRequest
import com.vitta.app.data.remote.dto.RegisterRequest
import retrofit2.HttpException

sealed class AuthResult {
    data class Success(val nombre: String) : AuthResult()
    object InvalidCredentials : AuthResult()
    object EmailTaken : AuthResult()
    data class OtherError(val message: String) : AuthResult()
}

class AuthRepository(private val tokenManager: TokenManager) {

    suspend fun login(correo: String, password: String): AuthResult {
        return try {
            val response = ApiClient.authApi.login(
                LoginRequest(correo, password)
            )

            tokenManager.saveToken(response.token)

            AuthResult.Success(response.usuario.nombre)

        } catch (e: HttpException) {

            if (e.code() == 401) {
                AuthResult.InvalidCredentials
            } else {
                AuthResult.OtherError(
                    "Error del servidor. Intenta de nuevo."
                )
            }

        } catch (e: Exception) {

            AuthResult.OtherError(
                "No se pudo conectar. Revisa tu conexión."
            )
        }
    }

    suspend fun register(
        nombre: String,
        correo: String,
        password: String
    ): AuthResult {

        return try {

            val response = ApiClient.authApi.register(
                RegisterRequest(nombre, correo, password)
            )

            tokenManager.saveToken(response.token)

            AuthResult.Success(response.usuario.nombre)

        } catch (e: HttpException) {

            if (e.code() == 409) {
                AuthResult.EmailTaken
            } else {
                AuthResult.OtherError(
                    "Error del servidor. Intenta de nuevo."
                )
            }

        } catch (e: Exception) {

            AuthResult.OtherError(
                "No se pudo conectar. Revisa tu conexión."
            )
        }
    }
}