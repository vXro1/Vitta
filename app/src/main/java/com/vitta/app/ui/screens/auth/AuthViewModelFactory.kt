package com.vitta.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vitta.app.data.repository.AuthRepository

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(repository) as T
            RegisterViewModel::class.java -> RegisterViewModel(repository) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: $modelClass")
        }
    }
}