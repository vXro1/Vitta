package com.vitta.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.logrosDataStore by preferencesDataStore(name = "vitta_logros")

/**
 * Recuerda, por usuario, qué medallas ya se celebraron (o ya estaban
 * obtenidas cuando se abrió la app), para que la celebración de una medalla
 * no se repita al reabrir la app ni al volver a registrar un hábito.
 *
 * Se inicializa una vez desde VittaApplication, igual que [com.vitta.app.data.remote.ApiClient].
 */
object LogroCelebrationStore {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private suspend fun userKey(): String? {
        val context = appContext ?: return null
        val correo = TokenManager(context).correoFlow.first()?.trim()?.lowercase()
        return if (correo.isNullOrEmpty()) null else "celebrados_$correo"
    }

    /** Ids de logros que ya no deben celebrarse para el usuario actual. */
    suspend fun celebrated(): Set<Int> {
        val context = appContext ?: return emptySet()
        val key = userKey() ?: return emptySet()
        val stored = context.logrosDataStore.data.first()[stringSetPreferencesKey(key)].orEmpty()
        return stored.mapNotNull { it.toIntOrNull() }.toSet()
    }

    suspend fun markCelebrated(ids: Collection<Int>) {
        if (ids.isEmpty()) return
        val context = appContext ?: return
        val key = stringSetPreferencesKey(userKey() ?: return)
        context.logrosDataStore.edit { prefs ->
            prefs[key] = prefs[key].orEmpty() + ids.map { it.toString() }
        }
    }
}
