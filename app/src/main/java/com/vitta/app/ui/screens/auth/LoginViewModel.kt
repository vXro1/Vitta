package com.vitta.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.repository.AuthRepository
import com.vitta.app.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val correoError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoading: Boolean = false
)

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, correoError = null, generalError = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null, generalError = null)
    }

    fun login(onSuccess: () -> Unit) {
        val state = _uiState.value

        val correoError = when {
            state.correo.isBlank() -> "Ingresa tu correo"
            !state.correo.contains("@") || state.correo.substringAfter("@", "").isBlank() ->
                "Falta la arroba, ej: nombre@correo.com"
            !state.correo.substringAfter("@").contains(".") ->
                "Falta el dominio, ej: nombre@correo.com"
            else -> null
        }
        val passwordError = if (state.password.isBlank()) "Ingresa tu contraseña" else null

        if (correoError != null || passwordError != null) {
            _uiState.value = state.copy(correoError = correoError, passwordError = passwordError)
            return
        }

        _uiState.value = state.copy(isLoading = true, generalError = null)
        viewModelScope.launch {
            when (val result = repository.login(state.correo, state.password)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                AuthResult.InvalidCredentials -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        passwordError = "Contraseña incorrecta"
                    )
                }
                AuthResult.EmailTaken -> Unit
                is AuthResult.OtherError -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, generalError = result.message)
                }
            }
        }
    }
}