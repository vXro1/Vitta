package com.vitta.app.data.remote.dto

data class RegisterRequest(
    val nombre: String,
    val correo: String,
    val password: String
)

data class LoginRequest(
    val correo: String,
    val password: String
)

data class UsuarioDto(
    val id: Int,
    val nombre: String,
    val correo: String
)

data class AuthResponse(
    val usuario: UsuarioDto,
    val token: String
)