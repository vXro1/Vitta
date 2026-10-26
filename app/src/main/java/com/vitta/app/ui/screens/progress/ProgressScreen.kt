package com.vitta.app.ui.screens.progress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.progress.ProgressPeriod
import com.vitta.app.data.progress.ProgressSummary
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow
import kotlin.math.roundToInt

private val MaxWidth = 640.dp

/**
 * Pestaña "Progreso", basada en la pantalla Progreso del mockup
 * (`vitta-app-maquetado--main`) con sus colores y tipografías.
 *
 * Los datos llegan por [ProgressViewModel] desde un `ProgressSource`: hoy de
 * prueba, y listos para el historial real cambiando una sola línea en
 * `ProgressSources` (la pantalla no cambia).
 */
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refresh() }

    Box(Modifier.fillMaxSize().background(Mk.Screen), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = MaxWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
        ) {
            Header(state)
            val summary = state.summary
            when {
                summary == null && state.error != null -> ErrorCard(state.error!!, onRetry = viewModel::refresh)
                summary == null -> LoadingSkeleton()
                !summary.hasData -> EmptyState()
                else -> {
                    PeriodSwitch(
                        selected = state.period,
                        onSelect = viewModel::selectPeriod,
                        modifier = Modifier.padding(top = 14.dp)
                    )
                    ProgressContent(summary)
                }
            }
        }
    }
}

@Composable
private fun Header(state: ProgressUiState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Tu progreso",
            style = caprasimo(25f, 1.1f, -0.02f),
            color = Mk.Ink,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (state.summary?.isSample == true) SampleBadge()
    }
    val range = state.summary?.takeIf { it.hasData }?.rangeLabel
    if (range != null) {
        AnimatedContent(
            targetState = range,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
            label = "rango"
        ) { text ->
            Text(text, style = figtree(500, 13f), color = Mk.Muted, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

/** Indica con claridad que la pantalla muestra datos de prueba. */
@Composable
private fun SampleBadge() {
    Row(
        Modifier
            .background(Mk.AmberTile, Pill)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(Mk.Amber, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text("Datos de ejemplo", style = figtree(700, 11f), color = Mk.AmberDeep)
    }
}

@Composable
private fun ProgressContent(summary: ProgressSummary) {
    HeroCard(summary, Modifier.padding(top = 16.dp))
    CompletionChart(summary.bars, summary.period, Modifier.padding(top = 12.dp))

    // Tarjetas de resumen (mockup: Mejor racha histórica · Cumplimiento · Hábitos activos).
    Row(
        Modifier.padding(top = 12.dp).fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatTile(
            "${summary.bestStreak}", "Mejor racha\nhistórica", Mk.OliveTile, Mk.OliveDeep, Mk.OliveText,
            Modifier.weight(1f), mascot = true
        )
        StatTile(
            "${summary.perfectDays}",
            if (summary.period == ProgressPeriod.Week) "Días perfectos\nesta semana" else "Días perfectos\neste mes",
            Mk.AmberTile, Mk.AmberDeep, Mk.AmberText, Modifier.weight(1f)
        )
        StatTile("${summary.activeHabits}", "Hábitos\nactivos", Mk.Sand, Mk.Ink, Mk.SandText, Modifier.weight(1f))
    }

    HeatmapCard(summary.heatmap, Modifier.padding(top = 12.dp))

    if (summary.habits.isNotEmpty()) {
        // margin:26px 2px 11px; h3 Caprasimo 17px + Vitta lectora.
        Row(
            Modifier.padding(start = 2.dp, end = 2.dp, top = 26.dp, bottom = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Por hábito",
                style = caprasimo(17f, 1.2f, -0.015f),
                color = Mk.Ink,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            VittaMascot(MascotState.Reader, size = MascotSize.Small, modifier = Modifier.size(44.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            summary.habits.forEach { HabitTrendRow(it) }
        }
    }

    Footnote(summary.isSample)
}

/** Tarjeta oscura del mockup (estilo de la tarjeta de nivel) con el cumplimiento del periodo. */
@Composable
private fun HeroCard(summary: ProgressSummary, modifier: Modifier = Modifier) {
    val rate = remember(summary.period) { Animatable(0f) }
    LaunchedEffect(summary.rate, summary.period) {
        rate.animateTo(summary.rate, tween(700, easing = CubicBezierEasing(.2f, .8f, .2f, 1f)))
    }
    val periodo = if (summary.period == ProgressPeriod.Week) "esta semana" else "este mes"
    val anterior = if (summary.period == ProgressPeriod.Week) "la semana pasada" else "el mes pasado"

    Column(
        modifier
            .fillMaxWidth()
            .clip(Card28)
            .background(Mk.Night)
            .padding(20.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("CUMPLIMIENTO · ${periodo.uppercase()}", style = figtree(600, 11f, 1f, 0.09f), color = Mk.Amber)
                Text(
                    "${(rate.value * 100).roundToInt()}%",
                    style = caprasimo(44f, 1f),
                    color = Mk.NightText,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    "${summary.totalDone} de ${summary.totalScheduled} hábitos cumplidos",
                    style = figtree(500, 13f),
                    color = Mk.NightSub,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            VittaMascot(MascotState.Strong, size = MascotSize.Small, modifier = Modifier.offset(x = 6.dp))
        }
        // Barra como la del nivel en el mockup: pista rgba(249,244,238,.18), relleno #F1AA52.
        Box(
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(9.dp)
                .clip(Pill)
                .background(Mk.NightTrack)
        ) {
            Box(Modifier.fillMaxWidth(rate.value.coerceIn(0f, 1f)).fillMaxHeight().background(Mk.Amber, Pill))
        }
        summary.previousRate?.let { prev ->
            val diff = ((summary.rate - prev) * 100).roundToInt()
            Text(
                when {
                    diff > 0 -> "↑ $diff puntos más que $anterior"
                    diff < 0 -> "↓ ${-diff} puntos menos que $anterior"
                    else -> "Igual que $anterior"
                },
                style = figtree(600, 12f),
                color = if (diff >= 0) Mk.Amber else Mk.NightSub,
                modifier = Modifier.padding(top = 9.dp)
            )
        }
    }
}

/** flex:1;border-radius:24px;padding:16px 14px — número Caprasimo 25px + etiqueta Figtree 600 11.5px. */
@Composable
private fun StatTile(
    value: String,
    label: String,
    bg: Color,
    valueColor: Color,
    labelColor: Color,
    modifier: Modifier,
    mascot: Boolean = false
) {
    Box(
        modifier
            .fillMaxHeight()
            .clip(Card24)
            .background(bg)
            .semantics(mergeDescendants = true) {}
    ) {
        if (mascot) {
            // <img vitta-fuerte ... position:absolute;bottom:-12px;right:-16px;width:58px;opacity:.5>
            VittaMascot(
                MascotState.Strong,
                size = MascotSize.Small,
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = 16.dp, y = 12.dp).size(58.dp).alpha(.5f)
            )
        }
        Column(Modifier.padding(horizontal = 14.dp, vertical = 16.dp)) {
            Text(value, style = caprasimo(25f, 1f), color = valueColor)
            Text(label, style = figtree(600, 11.5f, 1.3f), color = labelColor, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** Estado vacío del mockup: barras punteadas + "Tu progreso empieza mañana". */
@Composable
private fun EmptyState() {
    Column(
        Modifier
            .padding(top = 18.dp)
            .fillMaxWidth()
            .mockShadow(Card28)
            .background(Mk.Paper, Card28)
            .padding(horizontal = 22.dp, vertical = 24.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().height(118.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            listOf(.34f, .56f, .42f, .70f, .48f, .62f, .38f).forEach { h ->
                Box(Modifier.weight(1f).fillMaxHeight(h).dashedOutline(Mk.Dashed, 10.dp))
            }
        }
        Text(
            "Tu progreso empieza mañana",
            style = caprasimo(21f, 1.15f, -0.015f),
            color = Mk.Ink,
            modifier = Modifier.padding(top = 22.dp)
        )
        Text(
            "Con tres días de registro Vitta ya puede mostrarte tendencias. El mapa de calor se llena solo, un cuadro por día.",
            style = figtree(500, 13.5f, 1.5f),
            color = Mk.Body,
            modifier = Modifier.padding(top = 9.dp)
        )
    }
    Footnote(isSample = false)
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Column(
        Modifier
            .padding(top = 18.dp)
            .fillMaxWidth()
            .mockShadow(Card28)
            .background(Mk.Paper, Card28)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VittaMascot(MascotState.EmptyState, size = MascotSize.Small)
        Text(
            message,
            style = figtree(500, 13.5f, 1.5f),
            color = Mk.Body,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        TextButton(onClick = onRetry) { Text("Reintentar", style = figtree(600, 14.5f), color = Mk.Olive) }
    }
}

/** Bloques suaves que laten mientras carga (misma forma que el contenido). */
@Composable
private fun LoadingSkeleton() {
    val pulse by rememberInfiniteTransition(label = "carga").animateFloat(
        initialValue = .45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulso"
    )
    Column(Modifier.padding(top = 14.dp).alpha(pulse), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(52.dp).background(Mk.Sand, Pill))
        Box(Modifier.fillMaxWidth().height(168.dp).background(Mk.Sand, Card28))
        Box(Modifier.fillMaxWidth().height(250.dp).background(Mk.Sand, Card28))
    }
}

@Composable
private fun Footnote(isSample: Boolean) {
    Text(
        if (isSample) "Estos son datos de ejemplo. Cuando se active el historial real, esta vista se llena sola con tus registros."
        else "Todo lo que ves aquí viene de registros reales. Vitta no rellena días que no marcaste.",
        style = figtree(400, 12.5f, 1.5f),
        color = Mk.Muted,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 16.dp)
    )
}
