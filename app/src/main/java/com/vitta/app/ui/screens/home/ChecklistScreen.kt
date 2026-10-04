package com.vitta.app.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.screens.habits.VittaHabitIconBubble
import com.vitta.app.ui.screens.habits.VittaWaterTracker
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DashboardMaxWidth = 640.dp

/**
 * Inicio: resumen del día (progreso, racha, hábitos activos) y la lista de
 * hábitos que tocan hoy, con su control para registrar cumplimiento.
 */
@Composable
fun ChecklistScreen(
    userName: String,
    viewModel: ChecklistViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(VittaColorRoles.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VittaColorRoles.primary)
        }
        return
    }

    Box(Modifier.fillMaxSize().background(VittaColorRoles.background), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = DashboardMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(VittaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
        ) {
            item { HeaderBlock(userName = userName) }

            state.errorMessage?.let { message ->
                item {
                    VittaAuthErrorBanner(message)
                    Spacer(Modifier.height(VittaSpacing.Sm))
                    VittaPrimaryButton(text = "Reintentar", onClick = viewModel::load)
                }
            }

            if (state.errorMessage == null && state.cards.isEmpty()) {
                item { EmptyHabits(onAddHabit = onAddHabit) }
                return@LazyColumn
            }

            if (state.cards.isNotEmpty()) {
                item {
                    SummaryCard(done = state.doneToday, total = state.todayCards.size)
                }
                item {
                    StatsRow(
                        currentStreak = state.bestCurrentStreak,
                        bestStreak = state.bestStreakEver,
                        activeHabits = state.cards.size
                    )
                }
            }

            item {
                SectionHeader(
                    title = "Hábitos de hoy",
                    trailing = {
                        TextButton(
                            onClick = onAddHabit,
                            colors = ButtonDefaults.textButtonColors(contentColor = VittaColorRoles.primaryPressed)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(VittaSpacing.Xxs))
                            Text("Nuevo hábito", style = VittaTextStyles.button)
                        }
                    }
                )
            }

            if (state.todayCards.isEmpty() && state.cards.isNotEmpty()) {
                item {
                    Text(
                        "Hoy no tienes hábitos programados. ¡Disfruta el descanso!",
                        style = VittaTextStyles.body,
                        color = VittaColorRoles.textSecondary
                    )
                }
            }

            items(state.todayCards, key = { it.habit.id }) { card ->
                HabitDashboardCard(card = card, viewModel = viewModel, onEdit = { onEditHabit(card.habit.id) })
            }

            if (state.otherCards.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Otros días",
                        subtitle = "No están programados para hoy, pero puedes registrarlos si quieres."
                    )
                }
                items(state.otherCards, key = { "other_${it.habit.id}" }) { card ->
                    HabitDashboardCard(card = card, viewModel = viewModel, onEdit = { onEditHabit(card.habit.id) })
                }
            }

            item { Spacer(Modifier.height(VittaSpacing.Xl)) }
        }
    }
}

@Composable
private fun HeaderBlock(userName: String) {
    val today = remember {
        val locale = Locale("es", "ES")
        val date = LocalDate.now()
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val formatted = date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", locale))
        "${dayName.replaceFirstChar { it.uppercase() }}, $formatted"
    }
    Column(Modifier.padding(bottom = VittaSpacing.Xs)) {
        Text(today, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        Text(
            if (userName.isBlank()) "Hola" else "Hola, $userName",
            style = VittaTextStyles.displayLarge,
            color = VittaColorRoles.textPrimary,
            modifier = Modifier.semantics { heading() }
        )
    }
}

/** Tarjeta protagonista: anillo de progreso + mensaje del día. */
@Composable
private fun SummaryCard(done: Int, total: Int) {
    val fraction = if (total == 0) 0f else done.toFloat() / total
    val animated by animateFloatAsState(fraction, label = "dayProgress")
    val percent = (fraction * 100).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VittaColorRoles.primary, VittaShapes.extraLarge)
            .padding(VittaSpacing.Xl)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clearAndSetSemantics { contentDescription = "Progreso de hoy: $percent por ciento" },
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = 10.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(VittaColorRoles.onPrimary.copy(alpha = 0.25f), -90f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                drawArc(VittaColors.GoldAccent, -90f, 360f * animated, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            }
            Text("$percent%", style = VittaTextStyles.title, color = VittaColorRoles.onPrimary)
        }
        Spacer(Modifier.width(VittaSpacing.Lg))
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    total == 0 -> "DÍA LIBRE"
                    done == 0 -> "TU DÍA EMPIEZA AQUÍ"
                    done == total -> "¡DÍA COMPLETO!"
                    else -> "VAS MUY BIEN"
                },
                style = VittaTextStyles.label,
                color = VittaColorRoles.onPrimary
            )
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(
                if (total == 0) "Nada programado hoy" else "$done de $total hábitos completados",
                style = VittaTextStyles.title,
                color = VittaColorRoles.onPrimary
            )
            Spacer(Modifier.height(VittaSpacing.Xxs))
            Text(
                when {
                    total == 0 -> "Puedes registrar hábitos de otros días si quieres."
                    done == 0 -> "Empieza por el más fácil."
                    done == total -> "Vitta está orgulloso de ti."
                    else -> "Queda poco para cerrar el día."
                },
                style = VittaTextStyles.bodySmall,
                color = VittaColorRoles.onPrimary
            )
        }
    }
}

@Composable
private fun StatsRow(currentStreak: Int, bestStreak: Int, activeHabits: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)
    ) {
        // La llama de racha es la ilustración propia de Vitta (no de Icons8).
        StatTile({ StreakFlame(28.dp) }, "$currentStreak", if (currentStreak == 1) "día de racha" else "días de racha", Modifier.weight(1f))
        StatTile({ StatIcon(R.drawable.ic8_trophy) }, "$bestStreak", "mejor racha", Modifier.weight(1f))
        StatTile({ StatIcon(R.drawable.ic8_checklist) }, "$activeHabits", if (activeHabits == 1) "hábito activo" else "hábitos activos", Modifier.weight(1f))
    }
}

@Composable
private fun StatTile(icon: @Composable () -> Unit, value: String, label: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .border(1.dp, VittaColorRoles.border, VittaShapes.large)
            .padding(vertical = VittaSpacing.Md, horizontal = VittaSpacing.Sm)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(VittaSpacing.Xs))
        Text(value, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        Text(label, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted, textAlign = TextAlign.Center)
    }
}

@Composable
private fun StatIcon(icon: Int) {
    Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(28.dp), contentScale = ContentScale.Fit)
}

/** Llama de racha creada para Vitta (`assets/vitta/icons/icon_streak_flame.svg`). Decorativa. */
@Composable
private fun StreakFlame(size: androidx.compose.ui.unit.Dp) {
    VittaAssetImage(assetPath = VittaIconAssets.streakFlame, contentDescription = null, size = size)
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Column(Modifier.padding(top = VittaSpacing.Sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = VittaTextStyles.heading,
                color = VittaColorRoles.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            trailing()
        }
        if (subtitle != null) {
            Text(subtitle, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        }
    }
}

@Composable
private fun EmptyHabits(onAddHabit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.extraLarge)
            .border(1.dp, VittaColorRoles.border, VittaShapes.extraLarge)
            .padding(VittaSpacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VittaMascot(state = MascotState.EmptyState, size = MascotSize.Medium)
        Spacer(Modifier.height(VittaSpacing.Lg))
        Text("Aún no tienes hábitos", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(VittaSpacing.Xs))
        Text(
            "Elige uno sencillo para empezar. Con uno basta.",
            style = VittaTextStyles.body,
            color = VittaColorRoles.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(VittaSpacing.Lg))
        VittaPrimaryButton(text = "Crear mi primer hábito", icon = Icons.Filled.Add, onClick = onAddHabit)
    }
}

@Composable
private fun HabitDashboardCard(card: HabitCardUiState, viewModel: ChecklistViewModel, onEdit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (card.doneToday) VittaColorRoles.surfaceTint else VittaColorRoles.surface, VittaShapes.large)
            .border(1.dp, if (card.doneToday) VittaColors.GreenPale else VittaColorRoles.border, VittaShapes.large)
            .padding(VittaSpacing.Lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VittaHabitIconBubble(
                icon = card.icon,
                bubbleSize = 52.dp,
                containerColor = if (card.doneToday) VittaColorRoles.surface else VittaColorRoles.surfaceVariant
            )
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(card.habit.nombre, style = VittaTextStyles.subtitle.copy(fontSize = VittaTextStyles.title.fontSize), color = VittaColorRoles.textPrimary)
                Text(
                    when {
                        card.doneToday -> "Registrado hoy · Meta: ${card.habit.meta}"
                        card.metaTarget != null -> "${card.pendingValue} de ${card.metaTarget} ${card.metaUnidad}"
                        else -> card.habit.meta
                    },
                    style = VittaTextStyles.bodySmall,
                    color = VittaColorRoles.textSecondary
                )
                if (card.streakActual > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StreakFlame(16.dp)
                        Spacer(Modifier.width(VittaSpacing.Xxs))
                        Text(
                            "${card.streakActual} ${if (card.streakActual == 1) "día" else "días"} de racha",
                            style = VittaTextStyles.bodySmall,
                            color = VittaColors.OrangeAccent
                        )
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, contentDescription = "Editar ${card.habit.nombre}", tint = VittaColorRoles.textSecondary)
            }
        }

        Spacer(Modifier.height(VittaSpacing.Md))
        HabitProgressControl(card = card, viewModel = viewModel)

        if (card.errorMessage != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(card.errorMessage, style = VittaTextStyles.bodySmall, color = VittaColors.Error)
        }
    }
}

/** Control de registro según el tipo de meta. Misma lógica de antes, botones más claros. */
@Composable
private fun HabitProgressControl(card: HabitCardUiState, viewModel: ChecklistViewModel) {
    when {
        card.doneToday -> {
            Row(
                modifier = Modifier
                    .background(VittaColorRoles.primary, VittaShapes.pill)
                    .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = VittaColorRoles.onPrimary, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(VittaSpacing.Xxs))
                Text("Completado hoy", style = VittaTextStyles.bodySmall, color = VittaColorRoles.onPrimary)
            }
        }

        card.isSubmitting -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = VittaColorRoles.primary)
                Spacer(Modifier.width(VittaSpacing.Sm))
                Text("Guardando…", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textSecondary)
            }
        }

        card.esAgua && card.metaTarget != null -> {
            Column {
                VittaWaterTracker(
                    filled = card.pendingValue,
                    total = card.metaTarget,
                    onGlassClick = { index -> viewModel.onGlassTap(card.habit.id, index) }
                )
                Spacer(Modifier.height(VittaSpacing.Sm))
                OutlinedButton(
                    onClick = { viewModel.onMarkAll(card.habit.id) },
                    modifier = Modifier.heightIn(min = 44.dp),
                    shape = VittaShapes.pill,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VittaColorRoles.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VittaColorRoles.primaryPressed)
                ) { Text("Marcar todos los vasos", style = VittaTextStyles.button) }
            }
        }

        card.metaTarget != null -> {
            val step = when {
                card.metaUnidad.contains("paso") -> 1000
                card.metaUnidad.contains("minuto") -> 5
                else -> 1
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { (card.pendingValue.toFloat() / card.metaTarget).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = VittaColorRoles.primary,
                    trackColor = VittaColorRoles.surfaceTint,
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {}
                )
                Spacer(Modifier.width(VittaSpacing.Md))
                FilledTonalButton(
                    onClick = { viewModel.onQuickAdd(card.habit.id) },
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .semantics { contentDescription = "Sumar ${if (step == 1000) "1000" else step} ${card.metaUnidad}" },
                    shape = VittaShapes.pill,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = VittaColorRoles.surfaceTint,
                        contentColor = VittaColorRoles.primaryPressed
                    )
                ) { Text(if (step == 1000) "+1k" else "+$step", style = VittaTextStyles.button) }
            }
        }

        else -> {
            OutlinedButton(
                onClick = { viewModel.onToggleBoolean(card.habit.id) },
                modifier = Modifier.heightIn(min = 44.dp),
                shape = VittaShapes.pill,
                border = androidx.compose.foundation.BorderStroke(1.dp, VittaColorRoles.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VittaColorRoles.primaryPressed)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(VittaSpacing.Xs))
                Text("Marcar como hecho", style = VittaTextStyles.button)
            }
        }
    }
}
