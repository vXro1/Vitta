package com.vitta.app.ui.screens.progress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vitta.app.data.progress.HabitTrend
import com.vitta.app.data.progress.HeatCell
import com.vitta.app.data.progress.ProgressBar
import com.vitta.app.data.progress.ProgressCalculator
import com.vitta.app.data.progress.ProgressPeriod
import com.vitta.app.ui.components.icons.HabitIcons
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

internal val Pill = RoundedCornerShape(percent = 50)
internal val Card28 = RoundedCornerShape(28.dp)
internal val Card24 = RoundedCornerShape(24.dp)
internal val Card22 = RoundedCornerShape(22.dp)
private val BarShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
private val BarMax = 104.dp
private val MockEase = CubicBezierEasing(.2f, .8f, .2f, 1f) // cubic-bezier(.2,.8,.2,1) del mockup

/** Color de barra del mockup: completo #778661, ≥75 % #9CAA86, resto #C9D2B7. */
internal fun barColor(ratio: Float): Color = when {
    ratio >= 1f -> Mk.Olive
    ratio >= .75f -> Mk.OliveMid
    else -> Mk.OliveLight
}

internal fun heatColor(level: Int): Color = when (level) {
    3 -> Mk.Olive
    2 -> Mk.Heat2
    1 -> Mk.Heat1
    else -> Mk.Sand
}

/** Contorno punteado (estado vacío / días que aún no llegan), como `border:1.5px dashed #DDD2BF`. */
internal fun Modifier.dashedOutline(color: Color, radius: Dp, width: Dp = 1.5.dp): Modifier = drawBehind {
    val stroke = width.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(stroke / 2, stroke / 2),
        size = Size(size.width - stroke, size.height - stroke),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(width = stroke, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx())))
    )
}

// ---------------------------------------------------------------------------
// Selector Semana / Mes — el control segmentado del mockup, con indicador
// que se desliza entre opciones.
// ---------------------------------------------------------------------------

@Composable
internal fun PeriodSwitch(selected: ProgressPeriod, onSelect: (ProgressPeriod) -> Unit, modifier: Modifier = Modifier) {
    val options = ProgressPeriod.entries
    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .background(Mk.Sand, Pill)
            .padding(5.dp)
    ) {
        val gap = 6.dp
        val itemWidth = (maxWidth - gap * (options.size - 1)) / options.size
        val indicatorX by animateDpAsState(
            targetValue = (itemWidth + gap) * options.indexOf(selected),
            animationSpec = spring(dampingRatio = .82f, stiffness = Spring.StiffnessMediumLow),
            label = "indicador"
        )
        Box(
            Modifier
                .offset(x = indicatorX)
                .width(itemWidth)
                .height(42.dp)
                .shadow(1.5.dp, Pill, ambientColor = Mk.Ink, spotColor = Mk.Ink)
                .background(Mk.Paper, Pill)
        )
        Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            options.forEach { option ->
                val active = option == selected
                val color by animateColorAsState(if (active) Mk.Ink else Mk.SandText, label = "texto")
                Box(
                    Modifier
                        .width(itemWidth)
                        .height(42.dp)
                        .clip(Pill)
                        .selectable(selected = active, role = Role.Tab) { onSelect(option) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(option.label, style = figtree(600, 13.5f), color = color)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Gráfica de cumplimiento (tarjeta "Cumplimiento por día" del mockup).
// Toca una barra para ver su detalle.
// ---------------------------------------------------------------------------

@Composable
internal fun CompletionChart(bars: List<ProgressBar>, period: ProgressPeriod, modifier: Modifier = Modifier) {
    val current = bars.indexOfFirst { it.isCurrent }.takeIf { it >= 0 } ?: bars.lastIndex
    var picked by rememberSaveable(period) { mutableIntStateOf(-1) }
    val selected = if (picked in bars.indices) picked else current

    Column(
        modifier
            .fillMaxWidth()
            .mockShadow(Card28)
            .background(Mk.Paper, Card28)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (period == ProgressPeriod.Week) "Cumplimiento por día" else "Cumplimiento por semana",
                style = figtree(600, 13f),
                color = Mk.Body,
                modifier = Modifier.weight(1f)
            )
            ChartLegend()
        }

        Row(
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(BarMax + 52.dp),
            horizontalArrangement = Arrangement.spacedBy(if (bars.size > 5) 8.dp else 12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            bars.forEachIndexed { index, bar ->
                ChartBar(
                    bar = bar,
                    index = index,
                    period = period,
                    isSelected = index == selected,
                    dimmed = picked in bars.indices && index != selected,
                    animationKey = bars,
                    onClick = { picked = if (picked == index) -1 else index },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Detalle de la barra seleccionada (mejora sobre el mockup).
        AnimatedContent(
            targetState = bars.getOrNull(selected),
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "detalle"
        ) { bar ->
            if (bar != null) BarDetail(bar)
        }
    }
}

@Composable
private fun ChartBar(
    bar: ProgressBar,
    index: Int,
    period: ProgressPeriod,
    isSelected: Boolean,
    dimmed: Boolean,
    animationKey: Any,
    onClick: () -> Unit,
    modifier: Modifier
) {
    // vGrow .5s del mockup, escalonado barra a barra.
    val grow = remember(animationKey) { Animatable(0f) }
    LaunchedEffect(animationKey) {
        delay(index * 45L)
        grow.animateTo(1f, tween(520, easing = MockEase))
    }
    val alpha by animateFloatAsState(if (dimmed) .42f else 1f, label = "atenuar")
    val value = when {
        bar.isFuture -> ""
        period == ProgressPeriod.Week -> "${bar.done}"
        else -> "${(bar.ratio * 100).roundToInt()}%"
    }
    val description = when {
        bar.isFuture -> "${bar.detail}: todavía no llega"
        else -> "${bar.detail}: ${bar.done} de ${bar.scheduled} hábitos"
    }

    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClickLabel = "Ver detalle", onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
    ) {
        Text(
            value,
            style = figtree(700, 11f),
            color = if (isSelected) Mk.Ink else Mk.Muted,
            maxLines = 1,
            modifier = Modifier.alpha(alpha * grow.value)
        )
        if (bar.isFuture) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(BarMax * .22f)
                    .dashedOutline(Mk.Dashed, 10.dp)
            )
        } else {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(maxOf(BarMax * bar.ratio * grow.value, 6.dp))
                    .alpha(alpha)
                    .background(barColor(bar.ratio), BarShape)
                    .then(if (isSelected) Modifier.border(2.dp, Mk.Ink.copy(alpha = .18f), BarShape) else Modifier)
            )
        }
        Text(
            bar.label,
            style = figtree(if (bar.isCurrent) 700 else 600, if (period == ProgressPeriod.Week) 11f else 10.5f),
            color = if (bar.isCurrent) Mk.Ink else Mk.Faint,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun BarDetail(bar: ProgressBar) {
    Row(
        Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .background(Mk.SandSoft, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .then(
                    if (bar.isFuture) Modifier.dashedOutline(Mk.Dashed, 5.dp, 1.dp)
                    else Modifier.background(barColor(bar.ratio), CircleShape)
                )
        )
        Spacer(Modifier.width(10.dp))
        Text(bar.detail, style = figtree(600, 13f), color = Mk.Ink, modifier = Modifier.weight(1f))
        Text(
            when {
                bar.isFuture -> "Todavía no llega"
                bar.scheduled == 0 -> "Sin hábitos ese día"
                else -> "${bar.done} de ${bar.scheduled} · ${(bar.ratio * 100).roundToInt()}%"
            },
            style = figtree(700, 13f),
            color = if (bar.isFuture) Mk.Muted else Mk.OliveDeep
        )
    }
}

@Composable
private fun ChartLegend() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        listOf(Mk.OliveLight, Mk.OliveMid, Mk.Olive).forEach {
            Box(Modifier.size(8.dp).background(it, RoundedCornerShape(2.dp)))
        }
        Text("Completo", style = figtree(500, 11.5f), color = Mk.Muted, modifier = Modifier.padding(start = 2.dp))
    }
}

// ---------------------------------------------------------------------------
// Mapa de calor "Últimas cinco semanas" del mockup.
// ---------------------------------------------------------------------------

@Composable
internal fun HeatmapCard(cells: List<HeatCell>, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .mockShadow(Card28)
            .background(Mk.Paper, Card28)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Últimas cinco semanas", style = figtree(600, 13f), color = Mk.Body, modifier = Modifier.weight(1f))
            Text("Menos", style = figtree(500, 11.5f), color = Mk.Muted)
            Row(Modifier.padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                (0..3).forEach { Box(Modifier.size(10.dp).background(heatColor(it), RoundedCornerShape(3.dp))) }
            }
            Text("Más", style = figtree(500, 11.5f), color = Mk.Muted)
        }
        Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            cells.chunked(7).forEach { week ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    week.forEach { cell -> HeatCellBox(cell, Modifier.weight(1f)) }
                }
            }
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ProgressCalculator.DayLetters.forEach {
                Text(
                    it,
                    style = figtree(600, 10.5f),
                    color = Mk.Faint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun HeatCellBox(cell: HeatCell, modifier: Modifier) {
    val shape = RoundedCornerShape(7.dp)
    val label = when (cell.level) {
        -1 -> "todavía no llega"
        0 -> "sin hábitos cumplidos"
        1 -> "menos de la mitad"
        2 -> "más de la mitad"
        else -> "todo cumplido"
    }
    Box(
        modifier
            .aspectRatio(1f)
            .then(
                if (cell.level < 0) Modifier.dashedOutline(Mk.Dashed, 7.dp, 1.dp)
                else Modifier.background(heatColor(cell.level), shape)
            )
            .then(if (cell.isToday) Modifier.border(2.dp, Mk.Ink, shape) else Modifier)
            .semantics { contentDescription = "${cell.date.dayOfMonth}/${cell.date.monthValue}: $label" + if (cell.isToday) ", hoy" else "" }
    )
}

// ---------------------------------------------------------------------------
// Fila "Por hábito" del mockup (+ conteo de días, mejora).
// ---------------------------------------------------------------------------

@Composable
internal fun HabitTrendRow(trend: HabitTrend, modifier: Modifier = Modifier) {
    val icon = HabitIcons.forHabitName(trend.name) ?: HabitIcons.Default
    val fill = remember(trend) { Animatable(0f) }
    LaunchedEffect(trend) { fill.animateTo(trend.ratio, tween(650, easing = MockEase)) }
    val pct = (trend.ratio * 100).roundToInt()

    Row(
        modifier
            .fillMaxWidth()
            .mockShadow(Card22)
            .background(Mk.Paper, Card22)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${trend.name}: $pct %, ${trend.done} de ${trend.scheduled} días"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(icon.drawable), contentDescription = null, modifier = Modifier.size(26.dp).alpha(.85f))
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(trend.name, style = figtree(600, 14.5f), color = Mk.Ink, modifier = Modifier.weight(1f), maxLines = 1)
                Text("${trend.done} de ${trend.scheduled} días", style = figtree(500, 11.5f), color = Mk.Muted)
            }
            Box(
                Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(Pill)
                    .background(Mk.HabitTrack)
            ) {
                Box(Modifier.fillMaxWidth(fill.value).fillMaxHeight().background(Mk.Olive, Pill))
            }
        }
        Spacer(Modifier.width(13.dp))
        Text(
            "$pct%",
            style = figtree(700, 13.5f),
            color = Mk.OliveDeep,
            textAlign = TextAlign.End,
            modifier = Modifier.width(40.dp)
        )
    }
}
