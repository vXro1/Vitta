package com.vitta.app.data.repository

import com.vitta.app.data.remote.dto.HabitDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.ZoneOffset
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
            SAMPLE_HABITS.forEach { habits.createHabit(it.nombre, it.meta, it.frecuencia, "predefinido") }
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
        val today = LocalDate.now(ZoneOffset.UTC)
        val valor = Regex("""\d+""").find(habit.meta)?.value?.toDoubleOrNull()
        for (offset in DAYS downTo 1) {
            val day = today.minusDays(offset.toLong())
            if (!sample.scheduledOn(day)) continue
            // Últimos días siempre cumplidos: la demo arranca con una racha viva.
            val done = offset <= 4 || Random(day.toEpochDay() * 31 + habit.nombre.hashCode()).nextFloat() < sample.rate
            if (done) habits.createRecord(habit.id, "${day}T12:00:00Z", true, valor)
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
