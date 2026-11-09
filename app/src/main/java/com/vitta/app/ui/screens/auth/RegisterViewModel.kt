package com.vitta.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.AuthRepository
import com.vitta.app.data.repository.AuthResult
import com.vitta.app.ui.common.VittaValidation
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
) {
    /** Hay algo escrito: salir pide confirmación (HAL-01). */
    val hasChanges: Boolean
        get() = !justRegistered && (nombre.isNotEmpty() || correo.isNotEmpty() || password.isNotEmpty() || confirmPassword.isNotEmpty())

    /** "Crear cuenta" solo se habilita cuando todos los campos cumplen las reglas (HAL-02, HAL-03). */
    val canSubmit: Boolean
        get() = VittaValidation.nombre(nombre) == null &&
            VittaValidation.correo(correo) == null &&
            VittaValidation.contrasena(password) == null &&
            confirmPassword == password
}

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // La validación es en vivo: el error aparece debajo del campo en cuanto se escribe algo inválido.
    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(
            nombre = value,
            nombreError = if (value.isEmpty()) null else VittaValidation.nombre(value),
            generalError = null
        )
    }

    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(
            correo = value,
            correoError = if (value.isEmpty()) null else VittaValidation.correo(value),
            generalError = null
        )
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordError = if (value.isEmpty()) null else VittaValidation.contrasena(value),
            confirmPasswordError = null,
            generalError = null
        )
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
    }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value

        val nombreError = VittaValidation.nombre(state.nombre)
        val correoError = VittaValidation.correo(state.correo)
        val passwordError = VittaValidation.contrasena(state.password)
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
            when (val result = repository.register(state.nombre.trim(), state.correo.trim(), state.password)) {
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
