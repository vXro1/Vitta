package com.vitta.app.data.progress

/**
 * Punto ÚNICO donde se elige el origen de los datos de Progreso.
 *
 * Hoy la tarea pide datos de prueba ([SampleProgressSource]). Para usar el
 * historial real basta con poner [USE_SAMPLE_DATA] en false: se usará
 * [HabitRecordsProgressSource] y la pantalla no cambia. Si el backend publica
 * un endpoint agregado de progreso, se crea otra implementación de
 * [ProgressSource] y se devuelve aquí.
 */
object ProgressSources {
    const val USE_SAMPLE_DATA = false

    fun default(): ProgressSource =
        if (USE_SAMPLE_DATA) SampleProgressSource() else HabitRecordsProgressSource()
}
