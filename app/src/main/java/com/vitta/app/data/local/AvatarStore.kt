package com.vitta.app.data.local

import android.content.Context
import com.vitta.app.ui.components.mascot.MascotState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Foto de perfil del usuario: una de las ilustraciones de Vitta que ya trae
 * la app. El backend no tiene un campo de avatar (`usuarios` solo guarda
 * nombre, correo y contraseña), así que la elección se guarda en este
 * teléfono, por cuenta (correo).
 */
object AvatarStore {

    /** Las poses distintas de Vitta que existen en la app (sin repetidas). */
    val opciones: List<Pair<MascotState, String>> = listOf(
        MascotState.GoodHabits to "Campeón",
        MascotState.Strong to "Fuerte",
        MascotState.Reader to "Lector",
        MascotState.Yoga to "Zen",
        MascotState.Evening to "Puntual",
        MascotState.NoGym to "Pensativo"
    )

    private const val PREFS = "vitta_avatar"
    private lateinit var appContext: Context
    private val _actual = MutableStateFlow(MascotState.GoodHabits)
    val actual: StateFlow<MascotState> = _actual.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** Carga el avatar guardado para la cuenta [correo]. */
    fun cargar(correo: String?) {
        if (!::appContext.isInitialized || correo.isNullOrBlank()) return
        val nombre = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(correo.lowercase(), null)
        _actual.value = MascotState.entries.firstOrNull { it.name == nombre } ?: MascotState.GoodHabits
    }

    fun guardar(correo: String?, estado: MascotState) {
        _actual.value = estado
        if (!::appContext.isInitialized || correo.isNullOrBlank()) return
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(correo.lowercase(), estado.name).apply()
    }
}
