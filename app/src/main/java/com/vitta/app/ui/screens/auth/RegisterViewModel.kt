package com.vitta.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.AuthRepository
import com.vitta.app.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombreError: String? = null,
    val correoError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false,
    val justRegistered: Boolean = false
)

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, nombreError = null, generalError = null)
    }

    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, correoError = null, generalError = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, confirmPasswordError = null, generalError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value

        val nombreError = if (state.nombre.isBlank()) "Ingresa tu nombre" else null
        val correoError = when {
            state.correo.isBlank() -> "Ingresa tu correo"
            !state.correo.contains("@") || state.correo.substringAfter("@", "").isBlank() ->
                "Falta la arroba, ej: nombre@correo.com"
            !state.correo.substringAfter("@").contains(".") ->
                "Falta el dominio, ej: nombre@correo.com"
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "Ingresa una contraseña"
            state.password.length < 8 -> "Mínimo 8 caracteres"
            else -> null
        }
        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Repite tu contraseña"
            state.confirmPassword != state.password -> "Las contraseñas no coinciden"
            else -> null
        }

        if (nombreError != null || correoError != null || passwordError != null || confirmPasswordError != null) {
            _uiState.value = state.copy(
                nombreError = nombreError,
                correoError = correoError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }

        _uiState.value = state.copy(isLoading = true, generalError = null)
        viewModelScope.launch {
            when (val result = repository.register(state.nombre, state.correo, state.password)) {
                is AuthResult.Success -> {
                    // Mostramos el toast de éxito; la navegación real la
                    // dispara la pantalla después de un par de segundos.
                    _uiState.value = _uiState.value.copy(isLoading = false, justRegistered = true)
                }
                AuthResult.EmailTaken -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        correoError = "Ese correo ya está registrado"
                    )
                }
                AuthResult.InvalidCredentials -> Unit
                is AuthResult.OtherError -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, generalError = result.message)
                }
            }
        }
    }
}