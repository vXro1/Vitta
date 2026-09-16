package com.vitta.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vitta_prefs")

class TokenManager(private val context: Context) {

    private val tokenKey = stringPreferencesKey("jwt_token")
    private val nombreKey = stringPreferencesKey("user_nombre")

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[tokenKey] }
    val nombreFlow: Flow<String?> = context.dataStore.data.map { it[nombreKey] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[tokenKey] = token }
    }

    suspend fun saveNombre(nombre: String) {
        context.dataStore.edit { it[nombreKey] = nombre }
    }

    suspend fun clearToken() {
        context.dataStore.edit {
            it.remove(tokenKey)
            it.remove(nombreKey)
        }
    }
}