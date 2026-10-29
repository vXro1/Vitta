package com.vitta.app.ui.components.icons

import android.content.Context
import android.content.SharedPreferences

/**
 * Ícono de cada hábito en el teléfono (por id). La fuente de verdad es el
 * campo `icono` del backend: [sync] lo copia aquí al cargar los hábitos, así
 * las pantallas que solo conocen id y nombre (Progreso) muestran el mismo
 * ícono. Los hábitos antiguos sin `icono` usan el guardado o el del nombre.
 */
object HabitIconStore {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("vitta_habit_icons", Context.MODE_PRIVATE)
    }

    fun iconFor(habitId: Int, nombre: String): HabitIcon =
        HabitIcons.byId(prefs?.getString(key(habitId), null))
            ?: HabitIcons.forHabitName(nombre)
            ?: HabitIcons.Default

    /** Copia el `icono` del backend (si es una clave conocida) al teléfono. */
    fun sync(habitId: Int, icono: String?) {
        val icon = HabitIcons.byId(icono) ?: return
        if (prefs?.getString(key(habitId), null) != icon.id) save(habitId, icon)
    }

    fun save(habitId: Int, icon: HabitIcon) {
        prefs?.edit()?.putString(key(habitId), icon.id)?.apply()
    }

    private fun key(habitId: Int) = "habit_$habitId"
}
