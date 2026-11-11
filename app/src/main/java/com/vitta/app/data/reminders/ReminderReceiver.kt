package com.vitta.app.data.reminders

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.vitta.app.MainActivity
import com.vitta.app.R
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.RecordsResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Llega a la hora del recordatorio: si el hábito sigue pendiente hoy
 * (según `GET /api/habits/:id/records`) muestra la notificación, y deja
 * programado el siguiente día que toca.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getIntExtra(ReminderScheduler.EXTRA_HABIT_ID, -1)
        if (habitId <= 0) return
        val item = ReminderScheduler.Item(
            habitId = habitId,
            nombre = intent.getStringExtra(ReminderScheduler.EXTRA_NOMBRE).orEmpty(),
            meta = intent.getStringExtra(ReminderScheduler.EXTRA_META).orEmpty(),
            hora = intent.getStringExtra(ReminderScheduler.EXTRA_HORA).orEmpty(),
            frecuencia = intent.getStringExtra(ReminderScheduler.EXTRA_FRECUENCIA).orEmpty()
        )
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Siempre llega a la hora elegida; si ya lo cumplió hoy, el aviso lo felicita.
                mostrar(context, item, hecho = yaCumplido(habitId))
            } finally {
                ReminderScheduler.schedule(context, item)
                pending.finish()
            }
        }
    }

    /** true solo si el backend confirma que hoy ya está cumplido (sin red se avisa igual). */
    private suspend fun yaCumplido(habitId: Int): Boolean {
        val hoy = LocalDate.now(ZoneId.systemDefault())
        val r = withTimeoutOrNull(8000) { HabitRepository().listRecords(habitId) } as? RecordsResult.Success ?: return false
        return r.records.any { rec ->
            val dia = rec.dia?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: runCatching { Instant.parse(rec.fecha).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
            dia == hoy && rec.cumplido
        }
    }

    companion object {
    fun mostrar(context: Context, item: ReminderScheduler.Item, prueba: Boolean = false, hecho: Boolean = false) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val open = PendingIntent.getActivity(
            context,
            item.habitId,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(
                when {
                    prueba -> "Así te recordará Vitta tus hábitos"
                    hecho -> "¡Ya cumpliste ${item.nombre} hoy!"
                    else -> "Es hora de: ${item.nombre}"
                }
            )
            .setContentText(
                when {
                    prueba -> "Las notificaciones están activas."
                    hecho -> "Vitta está orgulloso de ti. Mañana a esta hora te recordamos otra vez."
                    else -> "Tu meta de hoy: ${item.meta}. ¡Vitta te espera!"
                }
            )
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                when {
                    prueba -> "Las notificaciones están activas. Te avisaremos a la hora que elegiste para cada hábito."
                    hecho -> "Ya registraste ${item.nombre} hoy (${item.meta}). Vitta está orgulloso de ti. Mañana a esta hora te recordamos otra vez."
                    else -> "Tu meta de hoy: ${item.meta}. Márcalo en Vitta cuando lo completes. ¡Vitta te espera!"
                }
            ))
            .setColor(0xFF778661.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(if (prueba) 999_999 else item.habitId, notification) }
    }
    }
}

/** Al reiniciar el teléfono las alarmas se borran: se vuelven a programar. */
class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            ReminderScheduler.rescheduleAll(context)
        }
    }
}
