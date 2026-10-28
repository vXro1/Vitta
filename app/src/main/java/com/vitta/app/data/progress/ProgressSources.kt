package com.vitta.app.data.progress

/**
 * Punto ÚNICO donde se elige el origen de los datos de Progreso.
 *
 * Por defecto se usan datos reales con [ApiProgressSource]: el endpoint
 * agregado `GET /api/progress` para los totales por día y
 * [HabitRecordsProgressSource] para el detalle por hábito (que el endpoint no
 * trae). Con [USE_SAMPLE_DATA] en true se vuelve a los datos de prueba
 * ([SampleProgressSource]); la pantalla no cambia en ningún caso.
 */
object ProgressSources {
    const val USE_SAMPLE_DATA = false

    fun default(): ProgressSource =
        if (USE_SAMPLE_DATA) SampleProgressSource() else ApiProgressSource()
}
