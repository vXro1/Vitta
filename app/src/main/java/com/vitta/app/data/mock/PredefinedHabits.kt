package com.vitta.app.data.mock

import com.vitta.app.ui.screens.habits.HabitIcon

data class PredefinedHabit(
    val id: String,
    val nombre: String,
    val categoria: String,
    val icon: HabitIcon,
    val metaValorSugerido: Int,
    val metaUnidad: String,
    val tipo: String = "predefinido"
)

val predefinedHabitsCatalog = listOf(
    PredefinedHabit("agua", "Tomar agua", "Hidratación", HabitIcon.Water, 8, "vasos al día"),
    PredefinedHabit("dormir", "Dormir", "Descanso", HabitIcon.Sleep, 7, "horas"),
    PredefinedHabit("caminar", "Caminar", "Movimiento", HabitIcon.Walk, 6000, "pasos"),
    PredefinedHabit("yoga", "Hacer yoga", "Bienestar", HabitIcon.Yoga, 10, "minutos al día"),
    PredefinedHabit("leer", "Leer", "Lectura", HabitIcon.Read, 20, "minutos al día"),
    PredefinedHabit("comer_saludable", "Comer saludable", "Alimentación", HabitIcon.Eat, 3, "comidas balanceadas"),
    PredefinedHabit("fruta", "Comer fruta", "Alimentación", HabitIcon.Eat, 2, "porciones al día")
)