package com.vitta.app.data.repository

import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.remote.ApiClient
import com.vitta.app.data.remote.dto.ChangePasswordRequest
import com.vitta.app.data.remote.dto.UpdateProfileRequest
import com.vitta.app.data.remote.dto.UsuarioDto
import retrofit2.HttpException

sealed class AccountResult<out T> {
    data class Success<T>(val value: T) : AccountResult<T>()
    data class FieldError(val field: String, val message: String) : AccountResult<Nothing>()
    data class Error(val message: String) : AccountResult<Nothing>()
}

/** Mensaje `{ "error": "..." }` que devuelve el backend en los 4xx. */
private fun HttpException.backendMessage(): String? = runCatching {
    val body = response()?.errorBody()?.string().orEmpty()
    Regex("\"error\"\\s*:\\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1)
}.getOrNull()

/**
 * Cuenta del usuario con los endpoints que ya existen en el backend:
 * - `PATCH /api/users/me` (nombre y/o correo)
 * - `PATCH /api/users/me/password` (contraseña actual + nueva)
 * Ambos devuelven un token nuevo, que se guarda junto con el nombre y el correo.
 */
class AccountRepository(private val tokenManager: TokenManager) {

    suspend fun updateProfile(nombre: String?, correo: String?): AccountResult<UsuarioDto> = try {
        val response = ApiClient.userApi.updateProfile(UpdateProfileRequest(nombre, correo))
        tokenManager.saveToken(response.token)
        tokenManager.saveNombre(response.usuario.nombre)
        tokenManager.saveCorreo(response.usuario.correo)
        AccountResult.Success(response.usuario)
    } catch (e: HttpException) {
        val msg = e.backendMessage()
        when (e.code()) {
            409 -> AccountResult.FieldError("correo", "Ese correo ya está registrado en otra cuenta")
            400 -> when {
                msg?.contains("correo", ignoreCase = true) == true -> AccountResult.FieldError("correo", "El servidor rechazó el correo: $msg")
                msg?.contains("nombre", ignoreCase = true) == true -> AccountResult.FieldError("nombre", "El servidor rechazó el nombre: $msg")
                else -> AccountResult.Error(msg ?: "No se pudieron guardar los cambios (400)")
            }
            else -> AccountResult.Error("No se pudieron guardar los cambios (${e.code()})")
        }
    } catch (e: Exception) {
        AccountResult.Error("No se pudo conectar. Revisa tu conexión.")
    }

    suspend fun changePassword(actual: String, nueva: String): AccountResult<String> = try {
        val response = ApiClient.userApi.changePassword(ChangePasswordRequest(actual, nueva))
        // Los tokens anteriores quedan invalidados: se guarda el nuevo para seguir con la sesión.
        tokenManager.saveToken(response.token)
        AccountResult.Success(response.mensaje ?: "Contraseña actualizada")
    } catch (e: HttpException) {
        val msg = e.backendMessage()
        when (e.code()) {
            403 -> AccountResult.FieldError("actual", "La contraseña actual no es correcta")
            400 -> if (msg?.contains("distinta", ignoreCase = true) == true)
                AccountResult.FieldError("nueva", "La contraseña nueva debe ser distinta de la actual")
            else AccountResult.FieldError("nueva", msg ?: "La contraseña nueva no es válida")
            else -> AccountResult.Error("No se pudo cambiar la contraseña (${e.code()})")
        }
    } catch (e: Exception) {
        AccountResult.Error("No se pudo conectar. Revisa tu conexión.")
    }
}
