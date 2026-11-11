package com.vitta.app.ui.common

import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconCategory

/**
 * Lista cerrada de unidades de meta (HAL-06) y su relación con cada hábito
 * (HAL-07). Cada unidad define:
 *  - [max]: máximo razonable por día/registro (HAL-07 y HAL-10: no más de
 *    600 minutos, 24 horas, etc.).
 *  - [paso]: lo que suman los botones − / + del formulario.
 *
 * La unidad ya no se escribe a mano: el usuario solo elige entre las que
 * tienen sentido para el hábito (según su icono o, si no hay una regla
 * específica, según su área), y la primera es la preseleccionada.
 */
data class HabitUnit(val nombre: String, val max: Int, val paso: Int = 1)

object HabitUnits {
    val Minutos = HabitUnit("minutos", max = 600, paso = 5)
    val Horas = HabitUnit("horas", max = 24)
    val Veces = HabitUnit("veces", max = 50)
    val Vasos = HabitUnit("vasos", max = 20)
    val Litros = HabitUnit("litros", max = 6)
    val Paginas = HabitUnit("páginas", max = 500, paso = 5)
    val Capitulos = HabitUnit("capítulos", max = 50)
    val Pasos = HabitUnit("pasos", max = 50000, paso = 500)
    val Kilometros = HabitUnit("kilómetros", max = 100)
    val Porciones = HabitUnit("porciones", max = 10)
    val Comidas = HabitUnit("comidas balanceadas", max = 6)
    val Sesiones = HabitUnit("sesiones", max = 10)
    val Tareas = HabitUnit("tareas", max = 30)

    val all = listOf(
        Minutos, Horas, Veces, Vasos, Litros, Paginas, Capitulos,
        Pasos, Kilometros, Porciones, Comidas, Sesiones, Tareas
    )

    /** Reglas por icono (las del informe: Leer, Tomar agua, Caminar…). */
    private val porIcono: Map<String, List<HabitUnit>> = mapOf(
        "water" to listOf(Vasos, Litros),
        "walk" to listOf(Minutos, Pasos, Kilometros),
        "running" to listOf(Minutos, Kilometros),
        "bicycle" to listOf(Minutos, Kilometros),
        "trekking" to listOf(Minutos, Kilometros, Pasos),
        "treadmill" to listOf(Minutos, Kilometros),
        "swimming" to listOf(Minutos, Kilometros),
        "read" to listOf(Paginas, Minutos, Horas),
        "sleep" to listOf(Horas),
        "meditate" to listOf(Minutos),
        "yoga" to listOf(Minutos, Sesiones),
        "eat" to listOf(Comidas, Porciones),
        "fruit" to listOf(Porciones)
    )

    /** Reglas por área cuando el icono no tiene una propia. */
    private val porArea: Map<HabitIconCategory, List<HabitUnit>> = mapOf(
        HabitIconCategory.Exercise to listOf(Minutos, Sesiones, Veces),
        HabitIconCategory.Mind to listOf(Minutos, Horas, Veces),
        HabitIconCategory.Health to listOf(Veces, Minutos),
        HabitIconCategory.Food to listOf(Porciones, Comidas, Veces),
        HabitIconCategory.Hydration to listOf(Vasos, Litros),
        HabitIconCategory.Study to listOf(Paginas, Minutos, Horas, Capitulos),
        HabitIconCategory.Work to listOf(Minutos, Horas, Tareas),
        HabitIconCategory.Organization to listOf(Veces, Minutos, Tareas),
        HabitIconCategory.Leisure to listOf(Minutos, Horas, Veces),
        HabitIconCategory.SelfCare to listOf(Veces, Minutos),
        HabitIconCategory.Home to listOf(Veces, Minutos, Tareas),
        HabitIconCategory.Social to listOf(Veces, Minutos),
        HabitIconCategory.Finance to listOf(Veces, Minutos)
    )

    /** Unidades permitidas para el hábito; la primera es la más común (preseleccionada). */
    fun paraHabito(icon: HabitIcon): List<HabitUnit> =
        porIcono[icon.id] ?: porArea[icon.category] ?: listOf(Veces, Minutos)

    fun porNombre(nombre: String?): HabitUnit? {
        val n = nombre?.trim().orEmpty()
        return all.firstOrNull { it.nombre.equals(n, ignoreCase = true) }
    }

    /** La unidad actual si está permitida; si no, la preseleccionada del hábito. */
    fun normalizar(icon: HabitIcon, actual: String?): HabitUnit {
        val permitidas = paraHabito(icon)
        return permitidas.firstOrNull { it.nombre.equals(actual?.trim(), ignoreCase = true) } ?: permitidas.first()
    }

    /** Mensaje de error de la cantidad para esa unidad (null si es válida). */
    fun validarCantidad(cantidad: Int, unidad: String): String? {
        val u = porNombre(unidad)
        return when {
            cantidad <= 0 -> "La cantidad debe ser mayor que 0"
            u != null && cantidad > u.max -> "El máximo para ${u.nombre} es ${u.max}"
            else -> null
        }
    }

    /** Lo que suma el atajo rápido de avance en Hoy/Inicio (se mantiene +5 en minutos). */
    fun pasoRapido(unidad: String): Int = when {
        unidad.contains("paso", ignoreCase = true) -> 1000
        unidad.contains("minuto", ignoreCase = true) -> 5
        else -> 1
    }

    /** Máximo que se puede registrar de una vez (HAL-10); null si la unidad no está en la lista. */
    fun maxPorRegistro(unidad: String): Int? = porNombre(unidad)?.max
}
