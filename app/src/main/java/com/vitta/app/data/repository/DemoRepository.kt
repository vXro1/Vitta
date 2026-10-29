package com.vitta.app.data.repository

import com.vitta.app.data.remote.dto.HabitDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import com.vitta.app.ui.components.icons.HabitIcons
import com.vitta.app.ui.screens.habits.HabitTextFormat
import java.time.LocalDate
import kotlin.random.Random

/**
 * Cuenta demo para explorar Vitta sin registrarse. Entra con
 * [DEMO_CORREO]; si la cuenta no existe en el backend la crea, y si no tiene
 * hábitos ni historial le carga hábitos de ejemplo con unas semanas de
 * registros para que Inicio, Progreso y Logros se vean con datos.
 */
class DemoRepository(
    private val auth: AuthRepository,
    private val habits: HabitRepository = HabitRepository()
) {

    suspend fun enter(): AuthResult {
        val login = auth.login(DEMO_CORREO, DEMO_PASSWORD)
        val result = if (login is AuthResult.InvalidCredentials) {
            auth.register(DEMO_NOMBRE, DEMO_CORREO, DEMO_PASSWORD)
        } else login
        if (result is AuthResult.Success) ensureSampleData()
        return result
    }

    private suspend fun ensureSampleData() {
        val existing = (habits.listHabits() as? HabitsResult.Success)?.habits ?: return
        if (existing.isEmpty()) {
            // Con meta numérica, icono y área: así funcionan el avance parcial y los logros por área.
            SAMPLE_HABITS.forEach {
                val icon = HabitIcons.forHabitName(it.nombre) ?: HabitIcons.Default
                habits.createHabit(
                    it.nombre, it.meta, it.frecuencia, "predefinido",
                    metaValor = HabitTextFormat.parseMeta(it.meta).first?.toDouble(),
                    unidad = HabitTextFormat.unidadParaBackend(HabitTextFormat.parseMeta(it.meta).second),
                    icono = icon.id,
                    categoria = icon.category.label
                )
            }
        }
        val all = (habits.listHabits() as? HabitsResult.Success)?.habits ?: return
        coroutineScope {
            all.map { habit -> async { seedHistoryIfEmpty(habit) } }.awaitAll()
        }
    }

    /** Registros de los últimos [DAYS] días (sin hoy), del más antiguo al más reciente para que la racha cuadre. */
    private suspend fun seedHistoryIfEmpty(habit: HabitDto) {
        val records = (habits.listRecords(habit.id) as? RecordsResult.Success)?.records ?: return
        if (records.isNotEmpty()) return
        val sample = SAMPLE_HABITS.firstOrNull { it.nombre == habit.nombre } ?: return
        // Día local: el backend asigna cada registro al día de la zona que envía la app (X-Timezone).
        val today = LocalDate.now()
        val valor = Regex("""\d+""").find(habit.meta)?.value?.toDoubleOrNull()
        for (offset in DAYS downTo 1) {
            val day = today.minusDays(offset.toLong())
            if (!sample.scheduledOn(day)) continue
            // Últimos días siempre cumplidos: la demo arranca con una racha viva.
            val done = offset <= 4 || Random(day.toEpochDay() * 31 + habit.nombre.hashCode()).nextFloat() < sample.rate
            // "YYYY-MM-DD" de un día pasado: el backend lo guarda al mediodía local de ese día.
            if (done) habits.createRecord(habit.id, day.toString(), true, valor)
        }
    }

    private data class SampleHabit(val nombre: String, val meta: String, val frecuencia: String, val rate: Float) {
        private val days = if (frecuencia == "Diaria") null else frecuencia.split(",").toSet()
        fun scheduledOn(day: LocalDate): Boolean =
            days == null || listOf("L", "M", "X", "J", "V", "S", "D")[day.dayOfWeek.value - 1] in days
    }

    companion object {
        const val DEMO_CORREO = "demo@vitta.test"
        const val DEMO_PASSWORD = "Vitta2026!"
        private const val DEMO_NOMBRE = "Demo Vitta"
        private const val DAYS = 27

        private val SAMPLE_HABITS = listOf(
            SampleHabit("Tomar agua", "8 vasos", "Diaria", .9f),
            SampleHabit("Leer", "20 minutos", "Diaria", .75f),
            SampleHabit("Hacer yoga", "10 minutos", "L,X,V", .7f),
            SampleHabit("Meditar", "10 minutos", "Diaria", .6f)
        )
    }
}
