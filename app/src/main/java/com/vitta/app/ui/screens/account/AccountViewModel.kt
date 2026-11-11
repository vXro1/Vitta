package com.vitta.app.ui.screens.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.repository.AccountRepository
import com.vitta.app.data.repository.AccountResult
import com.vitta.app.data.repository.PerfilResult
import com.vitta.app.data.repository.UserRepository
import com.vitta.app.ui.common.VittaValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val cargando: Boolean = true,
    val errorCarga: String? = null,
    /** Valores guardados en el backend (`GET /api/users/me`). */
    val nombreGuardado: String = "",
    val correoGuardado: String = "",
    val nombre: String = "",
    val correo: String = "",
    val nombreError: String? = null,
    val correoError: String? = null,
    val guardandoPerfil: Boolean = false,
    val errorPerfil: String? = null,
    val actual: String = "",
    val nueva: String = "",
    val confirmar: String = "",
    val actualError: String? = null,
    val nuevaError: String? = null,
    val confirmarError: String? = null,
    val guardandoClave: Boolean = false,
    val errorClave: String? = null,
    /** Mensaje de éxito para el snackbar. */
    val mensaje: String? = null
) {
    val perfilCambiado: Boolean
        get() = nombre.trim() != nombreGuardado || !correo.trim().equals(correoGuardado, ignoreCase = true)

    val puedeGuardarPerfil: Boolean
        get() = !guardandoPerfil && perfilCambiado &&
            VittaValidation.nombre(nombre) == null && VittaValidation.correo(correo) == null

    val puedeCambiarClave: Boolean
        get() = !guardandoClave && actual.isNotEmpty() &&
            VittaValidation.contrasena(nueva) == null && confirmar == nueva && nueva != actual

    val hasChanges: Boolean
        get() = perfilCambiado || actual.isNotEmpty() || nueva.isNotEmpty() || confirmar.isNotEmpty()
}

/** Cambiar nombre, correo y contraseña con `PATCH /api/users/me` y `PATCH /api/users/me/password`. */
class AccountViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = AccountRepository(TokenManager(app))
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, errorCarga = null) }
        viewModelScope.launch {
            when (val r = userRepository.getPerfil()) {
                is PerfilResult.Success -> _uiState.update {
                    it.copy(
                        cargando = false,
                        nombreGuardado = r.perfil.nombre,
                        correoGuardado = r.perfil.correo,
                        nombre = r.perfil.nombre,
                        correo = r.perfil.correo
                    )
                }
                is PerfilResult.Error -> _uiState.update { it.copy(cargando = false, errorCarga = r.message) }
            }
        }
    }

    fun onNombre(v: String) = _uiState.update {
        it.copy(nombre = v, nombreError = if (v.isEmpty()) "Ingresa tu nombre" else VittaValidation.nombre(v), errorPerfil = null)
    }

    fun onCorreo(v: String) = _uiState.update {
        it.copy(correo = v, correoError = if (v.isEmpty()) "Ingresa tu correo" else VittaValidation.correo(v), errorPerfil = null)
    }

    fun onActual(v: String) = _uiState.update { it.copy(actual = v, actualError = null, errorClave = null) }

    fun onNueva(v: String) = _uiState.update {
        it.copy(
            nueva = v,
            nuevaError = when {
                v.isEmpty() -> null
                VittaValidation.contrasena(v) != null -> VittaValidation.contrasena(v)
                v == it.actual -> "La contraseña nueva debe ser distinta de la actual"
                else -> null
            },
            confirmarError = null,
            errorClave = null
        )
    }

    fun onConfirmar(v: String) = _uiState.update {
        it.copy(confirmar = v, confirmarError = if (v.isNotEmpty() && v != it.nueva) "Las contraseñas no coinciden" else null, errorClave = null)
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }

    fun guardarPerfil() {
        val s = _uiState.value
        val nombreError = VittaValidation.nombre(s.nombre)
        val correoError = VittaValidation.correo(s.correo)
        if (nombreError != null || correoError != null) {
            _uiState.update { it.copy(nombreError = nombreError, correoError = correoError) }
            return
        }
        // Solo se envía lo que cambió.
        val nombre = s.nombre.trim().takeIf { it != s.nombreGuardado }
        val correo = s.correo.trim().takeIf { !it.equals(s.correoGuardado, ignoreCase = true) }
        if (nombre == null && correo == null) return
        _uiState.update { it.copy(guardandoPerfil = true, errorPerfil = null) }
        viewModelScope.launch {
            when (val r = repository.updateProfile(nombre, correo)) {
                is AccountResult.Success -> _uiState.update {
                    it.copy(
                        guardandoPerfil = false,
                        nombreGuardado = r.value.nombre,
                        correoGuardado = r.value.correo,
                        nombre = r.value.nombre,
                        correo = r.value.correo,
                        mensaje = "Datos de tu cuenta actualizados"
                    )
                }
                is AccountResult.FieldError -> _uiState.update {
                    if (r.field == "correo") it.copy(guardandoPerfil = false, correoError = r.message)
                    else it.copy(guardandoPerfil = false, nombreError = r.message)
                }
                is AccountResult.Error -> _uiState.update { it.copy(guardandoPerfil = false, errorPerfil = r.message) }
            }
        }
    }

    fun cambiarClave() {
        val s = _uiState.value
        val actualError = if (s.actual.isEmpty()) "Ingresa tu contraseña actual" else null
        val nuevaError = VittaValidation.contrasena(s.nueva)
            ?: if (s.nueva == s.actual) "La contraseña nueva debe ser distinta de la actual" else null
        val confirmarError = if (s.confirmar != s.nueva) "Las contraseñas no coinciden" else null
        if (actualError != null || nuevaError != null || confirmarError != null) {
            _uiState.update { it.copy(actualError = actualError, nuevaError = nuevaError, confirmarError = confirmarError) }
            return
        }
        _uiState.update { it.copy(guardandoClave = true, errorClave = null) }
        viewModelScope.launch {
            when (val r = repository.changePassword(s.actual, s.nueva)) {
                is AccountResult.Success -> _uiState.update {
                    it.copy(guardandoClave = false, actual = "", nueva = "", confirmar = "", mensaje = "Contraseña actualizada")
                }
                is AccountResult.FieldError -> _uiState.update {
                    if (r.field == "actual") it.copy(guardandoClave = false, actualError = r.message)
                    else it.copy(guardandoClave = false, nuevaError = r.message)
                }
                is AccountResult.Error -> _uiState.update { it.copy(guardandoClave = false, errorClave = r.message) }
            }
        }
    }
}
