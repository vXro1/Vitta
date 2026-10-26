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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.graphics.graphicsLayer
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

/** Escala del mapa de calor: de arena (nada) a oliva profundo (todo cumplido). */
internal fun heatColor(level: Int): Color = when (level) {
    3 -> Mk.OliveDeep
    2 -> Mk.Heat2
    1 -> Mk.Heat1
    else -> Mk.Sand
}

/** Color del número del día sobre cada nivel (≥ 4.5:1 en todos). */
private fun heatTextColor(level: Int): Color = when (level) {
    3 -> Mk.NightText
    -1 -> Mk.Chevron
    else -> Mk.Ink
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

/**
 * Mapa de calor de las últimas cinco semanas, como un calendario: cada celda
 * muestra su día y al tocarla aparece el detalle (cumplidos / programados).
 */
@Composable
internal fun HeatmapCard(cells: List<HeatCell>, modifier: Modifier = Modifier) {
    val todayIndex = cells.indexOfFirst { it.isToday }.takeIf { it >= 0 } ?: cells.lastIndex
    var picked by rememberSaveable { mutableIntStateOf(-1) }
    val selected = if (picked in cells.indices) picked else todayIndex

    Column(
        modifier
            .fillMaxWidth()
            .mockShadow(Card28)
            .background(Mk.Paper, Card28)
            .padding(20.dp)
    ) {
        Text("Últimas cinco semanas", style = figtree(600, 13f), color = Mk.Body)
        if (cells.isNotEmpty()) {
            Text(
                "${shortDate(cells.first())} – ${shortDate(cells.last())} · toca un día para ver el detalle",
                style = figtree(500, 11.5f, 1.35f),
                color = Mk.Muted,
                modifier = Modifier.padding(top = 3.dp)
            )
        }

        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ProgressCalculator.DayLetters.forEach {
                Text(
                    it,
                    style = figtree(700, 11f),
                    color = Mk.Muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Column(
            Modifier.padding(top = 6.dp).selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            cells.chunked(7).forEachIndexed { week, days ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    days.forEachIndexed { day, cell ->
                        val index = week * 7 + day
                        HeatCellBox(
                            cell = cell,
                            isSelected = index == selected,
                            onClick = { picked = if (picked == index) -1 else index },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = cells.getOrNull(selected),
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) },
            label = "detalle-dia"
        ) { cell -> if (cell != null) HeatDetail(cell) }

        Row(
            Modifier.padding(top = 14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(12.dp).border(2.5.dp, Mk.Amber, RoundedCornerShape(4.dp)))
            Text("Hoy", style = figtree(500, 11.5f), color = Mk.Muted, modifier = Modifier.padding(start = 6.dp).weight(1f))
            Text("Menos", style = figtree(500, 11.5f), color = Mk.Muted)
            Row(Modifier.padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                (0..3).forEach { Box(Modifier.size(12.dp).background(heatColor(it), RoundedCornerShape(4.dp))) }
            }
            Text("Más", style = figtree(500, 11.5f), color = Mk.Muted)
        }
    }
}

@Composable
private fun HeatCellBox(cell: HeatCell, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(9.dp)
    val background by animateColorAsState(
        if (cell.level < 0) Color.Transparent else heatColor(cell.level),
        label = "celda"
    )
    val scale by animateFloatAsState(if (isSelected) 1.08f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "zoom")
    Box(
        modifier
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(background, shape)
            .then(if (cell.level < 0) Modifier.dashedOutline(Mk.Dashed, 9.dp, 1.dp) else Modifier)
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, Mk.Ink, shape)
                    cell.isToday -> Modifier.border(3.dp, Mk.Amber, shape)
                    else -> Modifier
                }
            )
            .selectable(selected = isSelected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = "${dayLabel(cell)}: ${heatSummary(cell)}" + if (cell.isToday) ", hoy" else "" },
        contentAlignment = Alignment.Center
    ) {
        Text(
            "${cell.date.dayOfMonth}",
            style = figtree(if (cell.isToday || isSelected) 700 else 600, 12f, 1f),
            color = heatTextColor(cell.level)
        )
    }
}

@Composable
private fun HeatDetail(cell: HeatCell) {
    Column(
        Modifier
            .padding(top = 14.dp)
            .fillMaxWidth()
            .background(Mk.SandSoft, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        HeatDetailHeader(cell)
        if (cell.level >= 0 && cell.scheduled > 0) {
            Text(
                "Hábitos que tocaban ese día",
                style = figtree(600, 11.5f),
                color = Mk.Muted,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            cell.habitsDone.forEach { HabitDayRow(it.id, it.name, done = true) }
            cell.habitsMissed.forEach { HabitDayRow(it.id, it.name, done = false) }
        }
    }
}

/** Una línea por hábito: ✓ cumplido (oliva) o círculo vacío (sin marcar). */
@Composable
private fun HabitDayRow(id: Int, name: String, done: Boolean) {
    val icon = com.vitta.app.ui.components.icons.HabitIconStore.iconFor(id, name)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$name: " + if (done) "cumplido" else "sin marcar" },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(22.dp)
                .then(
                    if (done) Modifier.background(Mk.OliveDeep, CircleShape)
                    else Modifier.border(1.5.dp, Mk.Dashed, CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (done) {
                androidx.compose.material3.Icon(
                    androidx.compose.material.icons.Icons.Filled.Check,
                    contentDescription = null,
                    tint = Mk.NightText,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Image(
            painterResource(icon.drawable),
            contentDescription = null,
            modifier = Modifier.size(20.dp).alpha(if (done) 1f else .5f),
            colorFilter = com.vitta.app.ui.theme.VittaIconTint.habit
        )
        Spacer(Modifier.width(8.dp))
        Text(
            name,
            style = figtree(if (done) 600 else 500, 13.5f),
            color = if (done) Mk.Ink else Mk.Muted,
            modifier = Modifier.weight(1f),
            maxLines = 1
        )
        Text(
            if (done) "Cumplido" else "Sin marcar",
            style = figtree(600, 11.5f),
            color = if (done) Mk.OliveDeep else Mk.Muted
        )
    }
}

@Composable
private fun HeatDetailHeader(cell: HeatCell) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .then(
                    if (cell.level < 0) Modifier.dashedOutline(Mk.Dashed, 4.dp, 1.dp)
                    else Modifier.background(heatColor(cell.level), RoundedCornerShape(4.dp))
                )
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(dayLabel(cell) + if (cell.isToday) " · hoy" else "", style = figtree(600, 13f), color = Mk.Ink)
            if (cell.scheduled > 0 && cell.level >= 0) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(Pill)
                        .background(Mk.HabitTrack)
                ) {
                    Box(Modifier.fillMaxWidth(cell.ratio).fillMaxHeight().background(Mk.Olive, Pill))
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            heatSummary(cell),
            style = figtree(700, 13f),
            color = if (cell.level < 0 || cell.scheduled == 0) Mk.Muted else Mk.OliveDeep
        )
    }
}

private fun dayLabel(cell: HeatCell): String =
    "${ProgressCalculator.DayNames[cell.date.dayOfWeek.value - 1]} ${cell.date.dayOfMonth} de " +
        ProgressCalculator.MonthNames[cell.date.monthValue - 1]

private fun shortDate(cell: HeatCell): String =
    "${cell.date.dayOfMonth} ${ProgressCalculator.MonthNames[cell.date.monthValue - 1].take(3)}"

private fun heatSummary(cell: HeatCell): String = when {
    cell.level < 0 -> "Todavía no llega"
    cell.scheduled == 0 -> "Sin hábitos"
    else -> "${cell.done} de ${cell.scheduled} hábitos"
}

// ---------------------------------------------------------------------------
// Fila "Por hábito" del mockup (+ conteo de días, mejora).
// ---------------------------------------------------------------------------

@Composable
internal fun HabitTrendRow(trend: HabitTrend, modifier: Modifier = Modifier) {
    val icon = com.vitta.app.ui.components.icons.HabitIconStore.iconFor(trend.id, trend.name)
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
        Image(
            painterResource(icon.drawable),
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            colorFilter = com.vitta.app.ui.theme.VittaIconTint.habit
        )
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
