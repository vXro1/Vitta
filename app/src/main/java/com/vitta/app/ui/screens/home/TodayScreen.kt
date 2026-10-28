package com.vitta.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.components.progress.VittaLevelProgressBar
import com.vitta.app.ui.screens.habits.HabitTextFormat
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow

private val MaxWidth = 640.dp
private val R22 = RoundedCornerShape(22.dp)
private val R24 = RoundedCornerShape(24.dp)
private val R28 = RoundedCornerShape(28.dp)
private val Pill = RoundedCornerShape(percent = 50)

/** Momentos del día del mockup ("Mañana 06:00 – 12:00", ...), según la hora del recordatorio. */
private enum class Momento(val titulo: String, val hora: String) {
    Manana("Mañana", "06:00 – 12:00"),
    Tarde("Tarde", "12:00 – 19:00"),
    Noche("Noche", "19:00 – 23:00"),
    SinHora("En cualquier momento", "sin hora definida")
}

private fun momentoDe(card: HabitCardUiState): Momento {
    val hora = HabitTextFormat.parseHora(card.reminderHora) ?: return Momento.SinHora
    return when {
        hora.hour < 12 -> Momento.Manana
        hora.hour < 19 -> Momento.Tarde
        else -> Momento.Noche
    }
}

/**
 * Pestaña «Hoy» ("Checklist de hoy" del mockup). Usa el mismo
 * [ChecklistViewModel] que Inicio, así que registra hábitos reales: el
 * círculo cierra el hábito de una vez y la acción de la derecha suma avance
 * parcial ("+5", "+1k", "Todos") o lo marca ("Listo").
 */
@Composable
fun TodayScreen(
    viewModel: ChecklistViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (Int) -> Unit,
    onVerLogros: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize().background(Mk.Screen), contentAlignment = Alignment.TopCenter) {
        if (state.isLoading) {
            CircularProgressIndicator(color = Mk.Olive, modifier = Modifier.align(Alignment.Center))
            return@Box
        }
        val cards = state.todayCards
        val hechos = cards.count { it.doneToday }
        val vacio = state.cards.isEmpty()

        Column(
            Modifier
                .widthIn(max = MaxWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
        ) {
            // display:flex;align-items:flex-end;justify-content:space-between;gap:14px
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                Text(
                    "Checklist de hoy",
                    style = caprasimo(25f, 1.1f, -0.02f),
                    color = Mk.Ink,
                    modifier = Modifier.weight(1f).padding(bottom = 6.dp).semantics { heading() }
                )
                Spacer(Modifier.width(14.dp))
                VittaMascot(
                    state = when {
                        vacio -> MascotState.EmptyState
                        cards.isNotEmpty() && hechos == cards.size -> MascotState.Strong
                        else -> MascotState.GoodHabits
                    },
                    size = MascotSize.Small,
                    modifier = Modifier.offset(y = 6.dp)
                )
            }

            state.errorMessage?.let { msg ->
                Column(
                    Modifier.padding(top = 14.dp).fillMaxWidth().mockShadow(R24).background(Mk.Paper, R24).padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(msg, style = figtree(500, 13.5f, 1.5f), color = Mk.Body, textAlign = TextAlign.Center)
                    TextButton(onClick = viewModel::load) { Text("Reintentar", style = figtree(600, 14.5f), color = Mk.Olive) }
                }
            }

            if (vacio && state.errorMessage == null) {
                EstadoVacio(onAddHabit)
                return@Column
            }

            // Resumen: "{n} de {m} hábitos registrados" + barra + porcentaje.
            val pct = if (cards.isEmpty()) 0 else (hechos * 100) / cards.size
            Row(
                Modifier
                    .padding(top = 14.dp)
                    .fillMaxWidth()
                    .mockShadow(R24)
                    .background(Mk.Paper, R24)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("$hechos de ${cards.size} hábitos registrados", style = figtree(700, 14f), color = Mk.Ink)
                    VittaLevelProgressBar(
                        progress = pct / 100f,
                        modifier = Modifier.padding(top = 9.dp).height(9.dp),
                        trackColor = Mk.HabitTrack,
                        fillColor = Mk.Olive
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text("$pct%", style = caprasimo(24f, 1f), color = Mk.Olive)
            }

            if (cards.isEmpty()) {
                Text(
                    "Hoy no tienes hábitos programados. ¡Disfruta el descanso!",
                    style = figtree(500, 13.5f, 1.5f),
                    color = Mk.Body,
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            Momento.entries.forEach { momento ->
                val items = cards.filter { momentoDe(it) == momento }
                    .sortedBy { HabitTextFormat.parseHora(it.reminderHora) }
                if (items.isEmpty()) return@forEach
                Column(Modifier.padding(top = 24.dp)) {
                    Row(Modifier.padding(start = 2.dp, end = 2.dp, bottom = 11.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            momento.titulo,
                            style = caprasimo(16f, 1.2f, -0.01f),
                            color = Mk.Ink,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(momento.hora, style = figtree(500, 12f), color = Mk.Muted)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        items.forEach { card ->
                            FilaHabito(
                                card = card,
                                onComplete = { viewModel.onComplete(card.habit.id) },
                                onAccion = { accionDe(card, viewModel) },
                                onEdit = { onEditHabit(card.habit.id) }
                            )
                        }
                    }
                }
            }

            Text(
                "Los hábitos con meta numérica se pueden registrar por partes. Un toque en el círculo los cierra de una vez.",
                style = figtree(400, 12.5f, 1.5f),
                color = Mk.Muted,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 24.dp)
            )
        }

        RachaSumadaDialog(state.rachaSumada, onDismiss = viewModel::dismissRachaSumada)

        // Medalla recién desbloqueada al completar un hábito desde esta pestaña.
        LogroDesbloqueadoSheet(
            logro = state.logroCelebrado,
            onDismiss = viewModel::dismissCelebration,
            onVerLogros = {
                viewModel.dismissCelebration()
                onVerLogros()
            }
        )
    }
}

private fun pasoDe(card: HabitCardUiState): Int = when {
    card.metaUnidad.contains("paso") -> 1000
    card.metaUnidad.contains("minuto") -> 5
    else -> 1
}

/** Texto de la acción de la derecha (mismo criterio que Inicio y el mockup). */
private fun accionTexto(card: HabitCardUiState): String = when {
    card.metaTarget == null -> "Listo"
    card.esAgua -> "Todos"
    pasoDe(card) == 1000 -> "+1k"
    else -> "+${pasoDe(card)}"
}

private fun accionDe(card: HabitCardUiState, viewModel: ChecklistViewModel) = when {
    card.metaTarget == null -> viewModel.onToggleBoolean(card.habit.id)
    card.esAgua -> viewModel.onMarkAll(card.habit.id)
    else -> viewModel.onQuickAdd(card.habit.id)
}

/** "8 de 8 vasos · meta lista", "3 de 8 vasos", "10 minutos · 07:00"... */
private fun estadoDe(card: HabitCardUiState): String {
    val hora = card.reminderHora?.let { " · $it" }.orEmpty()
    val meta = card.metaTarget
    return when {
        meta == null -> if (card.doneToday) "Registrado hoy" else card.habit.meta + hora
        card.doneToday -> "$meta de $meta ${card.metaUnidad} · meta lista"
        else -> "${card.pendingValue} de $meta ${card.metaUnidad}$hora"
    }
}

/** Fila `h.row` del mockup: círculo (tick), icono, nombre/estado y acción. */
@Composable
private fun FilaHabito(
    card: HabitCardUiState,
    onComplete: () -> Unit,
    onAccion: () -> Unit,
    onEdit: () -> Unit
) {
    val hecho = card.doneToday
    Column(
        Modifier
            .fillMaxWidth()
            .mockShadow(R22)
            .background(if (hecho) Mk.OliveTile else Mk.Paper, R22)
            .padding(horizontal = 15.dp, vertical = 13.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // tick: 46px, borde 2px #CFD6BE o relleno #778661 con check
            Box(
                Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .then(
                        if (hecho) Modifier.background(Mk.Olive)
                        else Modifier.border(2.dp, Mk.OliveLight, CircleShape)
                    )
                    .clickable(
                        enabled = !hecho && !card.isSubmitting,
                        role = Role.Button,
                        onClickLabel = "Completar ${card.habit.nombre}",
                        onClick = onComplete
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    hecho -> Icon(Icons.Filled.Check, contentDescription = "Completado", tint = Mk.NightText, modifier = Modifier.size(17.dp))
                    card.isSubmitting -> CircularProgressIndicator(color = Mk.Olive, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Image(
                painterResource(card.icon.drawable),
                contentDescription = null,
                modifier = Modifier.size(24.dp).alpha(0.85f)
            )
            Spacer(Modifier.width(12.dp))
            // Tocar el nombre abre la edición del hábito (igual que en Inicio).
            Column(Modifier.weight(1f).clickable(onClickLabel = "Editar hábito", onClick = onEdit)) {
                Text(
                    card.habit.nombre,
                    style = figtree(600, 14.5f, 1.25f).copy(
                        textDecoration = if (hecho) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (hecho) Mk.SandText else Mk.Ink
                )
                Text(estadoDe(card), style = figtree(500, 12f), color = Mk.Muted, modifier = Modifier.padding(top = 2.dp))
            }
            if (!hecho) {
                Text(
                    accionTexto(card),
                    style = figtree(700, 12.5f),
                    color = Mk.AmberInk,
                    modifier = Modifier
                        .clip(Pill)
                        .clickable(enabled = !card.isSubmitting, role = Role.Button, onClick = onAccion)
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                )
            }
        }
        card.errorMessage?.let {
            Text(it, style = figtree(500, 12f), color = Mk.Danger, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

/** Estado vacío del mockup: "Nada que registrar todavía" + momentos punteados. */
@Composable
private fun EstadoVacio(onAddHabit: () -> Unit) {
    Column(
        Modifier
            .padding(top = 18.dp)
            .fillMaxWidth()
            .mockShadow(R28)
            .background(Mk.Paper, R28)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VittaMascot(state = MascotState.EmptyState, size = MascotSize.Medium)
        Text(
            "Nada que registrar todavía",
            style = caprasimo(21f, 1.15f, -0.015f),
            color = Mk.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            "Cuando agregues un hábito aparecerá aquí, ordenado por el momento del día en que lo quieras hacer.",
            style = figtree(500, 13.5f, 1.5f),
            color = Mk.Body,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 9.dp)
        )
        Box(
            Modifier
                .padding(top = 18.dp)
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(Pill)
                .background(Mk.Olive)
                .clickable(role = Role.Button, onClick = onAddHabit),
            contentAlignment = Alignment.Center
        ) {
            Text("Agregar un hábito", style = caprasimo(16f, 1.2f), color = Mk.NightText)
        }
    }
    Column(Modifier.padding(top = 22.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(Modifier.padding(start = 2.dp, end = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Mañana", style = caprasimo(16f, 1.2f, -0.01f), color = Mk.Faint)
            Spacer(Modifier.width(9.dp))
            Text("06:00 – 12:00", style = figtree(500, 12f), color = Mk.Chevron)
        }
        listOf(
            "Sin hábitos en este momento del día",
            "Sin hábitos en la tarde",
            "Sin hábitos en la noche"
        ).forEach { CajaPunteada(it) }
    }
}

/** border:1.5px dashed #DDD2BF;border-radius:22px;min-height:62px */
@Composable
private fun CajaPunteada(texto: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .drawBehind {
                drawRoundRect(
                    color = Mk.Dashed,
                    style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))),
                    cornerRadius = CornerRadius(22.dp.toPx())
                )
            }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(texto, style = figtree(500, 13f), color = Mk.Faint)
    }
}
