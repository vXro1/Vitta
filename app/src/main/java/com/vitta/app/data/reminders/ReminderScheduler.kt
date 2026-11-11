package com.vitta.app.data.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.ReminderDto
import com.vitta.app.ui.screens.habits.HabitTextFormat
import com.vitta.app.ui.screens.habits.WeekDays
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Recordatorios de hábitos en el teléfono (HAL-20).
 *
 * La hora y el estado (activo / pausado) de cada recordatorio viven en el
 * backend (`/api/recordatorios`); aquí solo se programan las alarmas locales
 * que disparan la notificación a esa hora, en los días que toca el hábito.
 * Se re-sincroniza cada vez que la app carga los hábitos y al reiniciar el
 * teléfono (con la última copia guardada).
 */
object ReminderScheduler {

    const val CHANNEL_ID = "vitta_recordatorios_v2"
    private const val PREFS = "vitta_recordatorios"
    private const val KEY_ITEMS = "items"
    internal const val EXTRA_HABIT_ID = "habit_id"
    internal const val EXTRA_NOMBRE = "nombre"
    internal const val EXTRA_META = "meta"
    internal const val EXTRA_HORA = "hora"
    internal const val EXTRA_FRECUENCIA = "frecuencia"

    private lateinit var appContext: Context

    /** Recordatorio programado: lo mínimo para volver a programarlo sin red. */
    data class Item(val habitId: Int, val nombre: String, val meta: String, val hora: String, val frecuencia: String)

    fun init(context: Context) {
        appContext = context.applicationContext
        createChannel(appContext)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Recordatorios de hábitos", NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Te avisa a la hora que elegiste para cada hábito pendiente."
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    /** Programa los recordatorios activos de hábitos activos y cancela los demás. */
    fun sync(reminders: List<ReminderDto>, habits: List<HabitDto>) {
        if (!::appContext.isInitialized) return
        val porId = habits.filter { it.activo != false }.associateBy { it.id }
        val items = reminders.filter { it.activo }.mapNotNull { r ->
            val h = porId[r.habito_id] ?: return@mapNotNull null
            Item(h.id, h.nombre, h.meta, r.hora.take(5), h.frecuencia)
        }
        val anteriores = load(appContext).map { it.habitId }.toSet()
        (anteriores - items.map { it.habitId }.toSet()).forEach { cancel(appContext, it) }
        items.forEach { schedule(appContext, it) }
        save(appContext, items)
    }

    /**
     * La hora o el estado de un recordatorio cambió (editar hábito o pantalla de
     * notificaciones): se reprograma enseguida, sin esperar a recargar Inicio.
     */
    fun actualizar(reminder: ReminderDto) {
        if (!::appContext.isInitialized) return
        val items = load(appContext).toMutableList()
        val i = items.indexOfFirst { it.habitId == reminder.habito_id }
        if (i < 0) return
        if (!reminder.activo) {
            cancel(appContext, reminder.habito_id)
            items.removeAt(i)
        } else {
            items[i] = items[i].copy(hora = reminder.hora.take(5))
            schedule(appContext, items[i])
        }
        save(appContext, items)
    }

    /** El recordatorio se borró: se cancela su alarma y se quita de la copia local. */
    fun quitar(habitId: Int) {
        if (!::appContext.isInitialized) return
        cancel(appContext, habitId)
        save(appContext, load(appContext).filter { it.habitId != habitId })
    }

    /** Notificación de prueba inmediata, para comprobar permiso y canal. */
    fun probar(context: Context) {
        ReminderReceiver.mostrar(context, Item(0, "tu hábito", "así se verán tus recordatorios", "", ""), prueba = true)
    }

    /** Cancela todo (al cerrar sesión). */
    fun clear() {
        if (!::appContext.isInitialized) return
        load(appContext).forEach { cancel(appContext, it.habitId) }
        save(appContext, emptyList())
    }

    /** Tras reiniciar el teléfono: vuelve a programar con la última copia. */
    internal fun rescheduleAll(context: Context) {
        load(context).forEach { schedule(context, it) }
    }

    internal fun schedule(context: Context, item: Item) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val hora = HabitTextFormat.parseHora(item.hora) ?: return
        val zona = ZoneId.systemDefault()
        val ahora = LocalDateTime.now(zona)
        // Próximo día programado (hoy si la hora aún no pasó).
        var dia = LocalDate.now(zona)
        var proximo: LocalDateTime? = null
        repeat(8) {
            val candidato = dia.atTime(hora)
            val toca = HabitTextFormat.isScheduledOn(item.frecuencia, WeekDays[dia.dayOfWeek.value - 1].first)
            if (proximo == null && toca && candidato.isAfter(ahora)) proximo = candidato
            dia = dia.plusDays(1)
        }
        val cuando = proximo ?: return
        val millis = cuando.atZone(zona).toInstant().toEpochMilli()
        val pi = pendingIntent(context, item)
        // Exacta (también en reposo) para que llegue a la hora elegida; si el sistema
        // no permite alarmas exactas, se usa una ventana corta de 1 minuto.
        val exacta = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarm.canScheduleExactAlarms()
        if (exacta) alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        else alarm.setWindow(AlarmManager.RTC_WAKEUP, millis, 60 * 1000L, pi)
    }

    private fun cancel(context: Context, habitId: Int) {
        val alarm = context.getSystemService(AlarmManager::class.java) ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, habitId, intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarm.cancel(pi)
        pi.cancel()
    }

    private fun pendingIntent(context: Context, item: Item): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_HABIT_ID, item.habitId)
            putExtra(EXTRA_NOMBRE, item.nombre)
            putExtra(EXTRA_META, item.meta)
            putExtra(EXTRA_HORA, item.hora)
            putExtra(EXTRA_FRECUENCIA, item.frecuencia)
        }
        return PendingIntent.getBroadcast(
            context, item.habitId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Copia local mínima (id, nombre, meta, hora, frecuencia) para reprogramar sin red.
    private fun save(context: Context, items: List<Item>) {
        val texto = items.joinToString("\n") { listOf(it.habitId, it.nombre, it.meta, it.hora, it.frecuencia).joinToString("\u001F") }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ITEMS, texto).apply()
    }

    private fun load(context: Context): List<Item> {
        val texto = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ITEMS, null) ?: return emptyList()
        return texto.lines().mapNotNull { linea ->
            val p = linea.split("\u001F")
            if (p.size < 5) null else p[0].toIntOrNull()?.let { Item(it, p[1], p[2], p[3], p[4]) }
        }
    }
}
