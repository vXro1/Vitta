package com.vitta.app.ui.components.icons

import android.content.Context
import android.content.SharedPreferences

/**
 * Ícono elegido por el usuario para cada hábito. El backend no guarda íconos,
 * así que se recuerda en el teléfono (por id de hábito). Si no hay uno
 * guardado, se deduce del nombre como antes.
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

    fun save(habitId: Int, icon: HabitIcon) {
        prefs?.edit()?.putString(key(habitId), icon.id)?.apply()
    }

    private fun key(habitId: Int) = "habit_$habitId"
}
