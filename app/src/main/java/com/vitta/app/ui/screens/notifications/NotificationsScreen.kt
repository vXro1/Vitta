package com.vitta.app.ui.screens.notifications

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.reminders.ReminderScheduler
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.screens.habits.HabitScreenHeader
import com.vitta.app.ui.screens.habits.HabitTextFormat
import com.vitta.app.ui.screens.habits.VittaFilterChip
import com.vitta.app.ui.screens.habits.VittaTimePickerDialog
import com.vitta.app.ui.screens.habits.formatReminderTime
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaIconTint
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Hora = DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "CO"))
private val Fecha = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))
private val R22 = RoundedCornerShape(22.dp)
private val R28 = RoundedCornerShape(28.dp)
private val Pill = RoundedCornerShape(percent = 50)

private enum class Pestana(val label: String) { Recordatorios("Recordatorios"), Historial("Historial") }
private enum class FiltroRecordatorio(val label: String) { Todos("Todos"), Activos("Activos"), Pausados("Pausados") }
private enum class FiltroHistorial(val label: String, val tipo: TipoAviso?) {
    Todo("Todo", null), Habitos("Hábitos", TipoAviso.Habito), Medallas("Medallas", TipoAviso.Logro), Rachas("Rachas", TipoAviso.Racha)
}

/**
 * Notificaciones (HAL-20): permiso de Android, recordatorios de cada hábito
 * (activar/pausar y hora, guardados en el backend) e historial de lo que ya
 * pasó (hábitos cumplidos, medallas y rachas) con datos de la base de datos.
 * Dos pestañas y filtros en cada una para no saturar la pantalla.
 */
@Composable
fun NotificationsScreen(onBack: () -> Unit, viewModel: NotificationsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var permiso by remember { mutableStateOf(tienePermiso(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permiso = it }
    var horaDe by remember { mutableStateOf<ReminderItem?>(null) }
    var pestana by rememberSaveable { mutableStateOf(Pestana.Recordatorios) }
    var filtroR by rememberSaveable { mutableStateOf(FiltroRecordatorio.Todos) }
    var filtroH by rememberSaveable { mutableStateOf(FiltroHistorial.Todo) }

    // Al volver de los ajustes del sistema se revisa otra vez el permiso.
    val lifecycle = androidx.compose.ui.platform.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle) {
        val obs = androidx.lifecycle.LifecycleEventObserver { _, e ->
            if (e == androidx.lifecycle.Lifecycle.Event.ON_RESUME) permiso = tienePermiso(context)
        }
        lifecycle.addObserver(obs)
        onDispose { lifecycle.removeObserver(obs) }
    }
    LaunchedEffect(state.mensaje) {
        val m = state.mensaje ?: return@LaunchedEffect
        snackbar.showSnackbar(m)
        viewModel.consumirMensaje()
    }

    val pedirPermiso = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            context.startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    Box(Modifier.fillMaxSize().background(Mk.Screen)) {
        Column(Modifier.fillMaxSize()) {
            HabitScreenHeader(title = "Notificaciones", subtitle = "Recordatorios e historial", onBack = onBack)
            when {
                state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Mk.Olive)
                }
                state.error != null -> Column(Modifier.padding(20.dp)) {
                    VittaAuthErrorBanner(state.error ?: "")
                    Spacer(Modifier.height(16.dp))
                    VittaPrimaryButton(text = "Reintentar", onClick = viewModel::cargar)
                }
                else -> {
                    val activos = state.recordatorios.count { it.reminder.activo }
                    val recordatorios = state.recordatorios.filter {
                        when (filtroR) {
                            FiltroRecordatorio.Todos -> true
                            FiltroRecordatorio.Activos -> it.reminder.activo
                            FiltroRecordatorio.Pausados -> !it.reminder.activo
                        }
                    }
                    val historial = state.historial.filter { filtroH.tipo == null || it.tipo == filtroH.tipo }
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Cabecera(
                                permiso = permiso,
                                activos = activos,
                                total = state.recordatorios.size,
                                onActivar = pedirPermiso,
                                onProbar = { ReminderScheduler.probar(context) }
                            )
                        }
                        item { Pestanas(pestana) { pestana = it } }
                        if (pestana == Pestana.Recordatorios) {
                            item {
                                Chips(FiltroRecordatorio.entries.map { it.label }, filtroR.ordinal) { filtroR = FiltroRecordatorio.entries[it] }
                            }
                            if (recordatorios.isEmpty()) {
                                item {
                                    Vacio(
                                        if (state.recordatorios.isEmpty()) "Tus hábitos todavía no tienen hora. Elígela al crear o editar un hábito."
                                        else "No hay recordatorios ${filtroR.label.lowercase()}."
                                    )
                                }
                            }
                            items(recordatorios, key = { it.reminder.id }) { item ->
                                RecordatorioRow(
                                    item = item,
                                    busy = state.ocupado == item.reminder.id,
                                    onToggle = { viewModel.cambiarActivo(item, it) },
                                    onHora = { horaDe = item }
                                )
                            }
                            item {
                                Text(
                                    "Te avisamos a esa hora cada día que toca el hábito. Si ya lo cumpliste, el aviso te felicita.",
                                    style = figtree(500, 12.5f, 1.45f),
                                    color = Mk.Muted,
                                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                )
                            }
                        } else {
                            item {
                                Chips(FiltroHistorial.entries.map { it.label }, filtroH.ordinal) { filtroH = FiltroHistorial.entries[it] }
                            }
                            state.errorHistorial?.let { e -> item { Text(e, style = figtree(600, 13f), color = Mk.Danger) } }
                            if (historial.isEmpty()) {
                                item { Vacio("Aquí aparecerán tus hábitos cumplidos, medallas y rachas.") }
                            }
                            // Agrupado por día: "Hoy", "Ayer" o la fecha.
                            val zona = ZoneId.systemDefault()
                            historial.groupBy { it.fecha.atZone(zona).toLocalDate() }.forEach { (dia, avisos) ->
                                item(key = "dia_$dia") { Dia(dia) }
                                items(avisos, key = { it.id }) { AvisoRow(it) }
                            }
                        }
                    }
                }
            }
        }
        com.vitta.app.ui.common.VittaSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    horaDe?.let { item ->
        VittaTimePickerDialog(
            initial = HabitTextFormat.parseHora(item.reminder.hora) ?: java.time.LocalTime.of(7, 0),
            onConfirm = { t -> horaDe = null; viewModel.cambiarHora(item, HabitTextFormat.hora(t)) },
            onDismiss = { horaDe = null }
        )
    }
}

private fun tienePermiso(context: android.content.Context): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    else androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()

/** Tarjeta oscura con Vitta: estado del permiso, recordatorios activos y prueba. */
@Composable
private fun Cabecera(permiso: Boolean, activos: Int, total: Int, onActivar: () -> Unit, onProbar: () -> Unit) {
    Column(Modifier.padding(top = 4.dp).fillMaxWidth().clip(R28).background(Mk.Night)) {
        Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 16.dp), verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f).padding(bottom = 16.dp)) {
                Text(
                    if (permiso) "NOTIFICACIONES ACTIVAS" else "NOTIFICACIONES APAGADAS",
                    style = figtree(600, 11f, 1f, 0.09f),
                    color = if (permiso) Mk.Amber else Mk.NightSub
                )
                Text(
                    "$activos de $total recordatorios encendidos",
                    style = caprasimo(20f, 1.15f),
                    color = Mk.NightText,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            VittaMascot(
                state = if (permiso) MascotState.GoodHabits else MascotState.NoGym,
                size = MascotSize.Small,
                modifier = Modifier.offset(y = 6.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().background(Mk.NightTrack).padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (permiso) Icons.Outlined.NotificationsActive else Icons.Outlined.NotificationsOff,
                contentDescription = null,
                tint = Mk.NightText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                if (permiso) "¿Quieres ver cómo llegan?" else "Actívalas para recibir tus recordatorios.",
                style = figtree(500, 12.5f, 1.35f),
                color = Mk.NightText,
                modifier = Modifier.weight(1f)
            )
            Text(
                if (permiso) "Probar" else "Activar",
                style = figtree(700, 13f),
                color = Mk.Ink,
                modifier = Modifier
                    .clip(Pill)
                    .background(Mk.Amber)
                    .clickable(role = Role.Button, onClick = if (permiso) onProbar else onActivar)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/** Control segmentado como el de Semana/Mes de Progreso. */
@Composable
private fun Pestanas(actual: Pestana, onSelect: (Pestana) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(Pill).background(Mk.Sand).padding(4.dp)) {
        Pestana.entries.forEach { p ->
            val sel = p == actual
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(Pill)
                    .background(if (sel) Mk.Paper else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(p) },
                contentAlignment = Alignment.Center
            ) {
                Text(p.label, style = figtree(if (sel) 700 else 600, 14f), color = if (sel) Mk.Ink else Mk.Muted)
            }
        }
    }
}

@Composable
private fun Chips(opciones: List<String>, seleccion: Int, onSelect: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(opciones.size) { i -> VittaFilterChip(opciones[i], selected = i == seleccion) { onSelect(i) } }
    }
}

@Composable
private fun Vacio(texto: String) {
    Column(
        Modifier.fillMaxWidth().mockShadow(R22).background(Mk.Paper, R22).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VittaMascot(state = MascotState.EmptyState, size = MascotSize.Small)
        Text(texto, style = figtree(500, 13.5f, 1.45f), color = Mk.Body, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun Dia(dia: LocalDate) {
    val hoy = LocalDate.now(ZoneId.systemDefault())
    Text(
        when (dia) {
            hoy -> "Hoy"
            hoy.minusDays(1) -> "Ayer"
            else -> dia.format(Fecha).replaceFirstChar { it.uppercase() }
        },
        style = caprasimo(16f, 1.2f),
        color = Mk.Ink,
        modifier = Modifier.padding(top = 8.dp, start = 2.dp).semantics { heading() }
    )
}

@Composable
private fun RecordatorioRow(item: ReminderItem, busy: Boolean, onToggle: (Boolean) -> Unit, onHora: () -> Unit) {
    val hora = HabitTextFormat.parseHora(item.reminder.hora)
    val on = item.reminder.activo
    Row(
        Modifier
            .fillMaxWidth()
            .mockShadow(R22)
            .clip(R22)
            .background(if (on) Mk.Paper else Mk.SandSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(46.dp).background(if (on) Mk.OliveTile else Mk.Track, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painterResource(item.icon.drawable),
                contentDescription = null,
                modifier = Modifier.size(26.dp).alpha(if (on) 1f else .5f),
                colorFilter = VittaIconTint.habit
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.habit.nombre, style = figtree(700, 14.5f), color = if (on) Mk.Ink else Mk.Muted)
            Row(
                Modifier
                    .padding(top = 4.dp)
                    .clip(Pill)
                    .background(VittaColors.SkySoft)
                    .clickable(enabled = !busy, role = Role.Button, onClickLabel = "Cambiar hora", onClick = onHora)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Alarm, contentDescription = null, tint = com.vitta.app.ui.theme.VittaColorRoles.reminder, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(hora?.let { formatReminderTime(it) } ?: item.reminder.hora, style = figtree(700, 12.5f), color = com.vitta.app.ui.theme.VittaColorRoles.reminder)
            }
            if (!on) Text("Pausado", style = figtree(600, 11.5f), color = Mk.SandText, modifier = Modifier.padding(top = 3.dp))
        }
        if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = Mk.Olive)
        else Switch(
            checked = on,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Mk.NightText,
                checkedTrackColor = Mk.Olive,
                uncheckedTrackColor = Mk.Track,
                uncheckedBorderColor = Mk.Dashed
            )
        )
    }
}

@Composable
private fun AvisoRow(aviso: AvisoItem) {
    val (icono, color, fondo) = when (aviso.tipo) {
        TipoAviso.Habito -> Triple(Icons.Outlined.CheckCircle, Mk.OliveDeep, Mk.OliveTile)
        TipoAviso.Logro -> Triple(Icons.Outlined.EmojiEvents, Mk.Ink, Mk.AmberTile)
        TipoAviso.Racha -> Triple(Icons.Outlined.LocalFireDepartment, VittaColors.OrangeAccent, Mk.AmberTile)
    }
    Row(
        Modifier
            .fillMaxWidth()
            .mockShadow(R22)
            .background(Mk.Paper, R22)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).background(fondo, CircleShape), contentAlignment = Alignment.Center) {
            Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(aviso.titulo, style = figtree(700, 14f, 1.3f), color = Mk.Ink)
            Text(aviso.detalle, style = figtree(500, 12.5f, 1.35f), color = Mk.Body, modifier = Modifier.padding(top = 2.dp))
        }
        Spacer(Modifier.width(8.dp))
        Text(aviso.fecha.atZone(ZoneId.systemDefault()).format(Hora), style = figtree(600, 11.5f), color = Mk.Muted)
    }
}
