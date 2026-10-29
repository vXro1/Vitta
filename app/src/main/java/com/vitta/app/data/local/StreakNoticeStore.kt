package com.vitta.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.rachasDataStore by preferencesDataStore(name = "vitta_rachas")

/**
 * Recuerda, por usuario, los avisos de "Racha terminada" que ya se cerraron
 * con "Empezar una racha nueva hoy" (clave: hábito + día en que se cortó),
 * para no volver a mostrarlos al reabrir la app.
 */
object StreakNoticeStore {

    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private suspend fun key() = appContext?.let { context ->
        TokenManager(context).correoFlow.first()?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
            ?.let { stringSetPreferencesKey("rachas_cerradas_$it") }
    }

    suspend fun dismissed(): Set<String> {
        val context = appContext ?: return emptySet()
        val key = key() ?: return emptySet()
        return context.rachasDataStore.data.first()[key].orEmpty()
    }

    suspend fun dismiss(id: String) {
        val context = appContext ?: return
        val key = key() ?: return
        context.rachasDataStore.edit { it[key] = it[key].orEmpty() + id }
    }
}
