package com.vitta.app.data.mock

import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIcons

data class PredefinedHabit(
    val id: String,
    val nombre: String,
    val categoria: String,
    val icon: HabitIcon,
    val metaValorSugerido: Int,
    /** Unidad sugerida; el usuario puede cambiarla al configurar el hábito. */
    val metaUnidad: String,
    val tipo: String = "predefinido"
)

// La meta se guarda en el backend como texto libre "<cantidad> <unidad>"
// (columna habitos.meta). La frecuencia va en su propio campo, por eso la
// unidad ya no incluye "al día".
val predefinedHabitsCatalog = listOf(
    PredefinedHabit("agua", "Tomar agua", "Hidratación", HabitIcons.Water, 8, "vasos"),
    PredefinedHabit("dormir", "Dormir", "Descanso", HabitIcons.Sleep, 7, "horas"),
    PredefinedHabit("caminar", "Caminar", "Movimiento", HabitIcons.Walk, 6000, "pasos"),
    PredefinedHabit("yoga", "Hacer yoga", "Bienestar", HabitIcons.Yoga, 10, "minutos"),
    PredefinedHabit("meditar", "Meditar", "Bienestar", HabitIcons.Meditate, 10, "minutos"),
    PredefinedHabit("leer", "Leer", "Lectura", HabitIcons.Read, 20, "minutos"),
    PredefinedHabit("comer_saludable", "Comer saludable", "Alimentación", HabitIcons.Eat, 3, "comidas balanceadas"),
    PredefinedHabit("fruta", "Comer fruta", "Alimentación", HabitIcons.Fruit, 2, "porciones")
)

/** Unidades sugeridas en el selector de unidad (el usuario también puede escribir otra). */
val suggestedUnits = listOf(
    "minutos", "horas", "veces", "vasos", "páginas", "pasos", "kilómetros", "porciones", "litros"
)
