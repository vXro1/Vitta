package com.vitta.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.brand.VittaLogo
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
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
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DashboardMaxWidth = 640.dp

/**
 * Inicio (dashboard): encabezado con nivel, tarjeta principal con racha,
 * VitaPuntos y barra de nivel, lista "Hoy", y los bloques de abajo
 * (dormir, retos y Vitta Store, definidos en DashboardExtras.kt).
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

    // TODO: conectar con el backend cuando existan puntos y nivel.
    val level = 5
    val points = 1240
    val levelName = "Impulsor"
    val pointsToNext = 760
    val levelProgress = 0.3f

    Box(Modifier.fillMaxSize().background(VittaColorRoles.background), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = DashboardMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(VittaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
        ) {
            item { HeaderBlock(userName = userName, level = level) }

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
                    HeroCard(
                        doneCount = state.doneToday,
                        totalCount = state.todayCards.size,
                        racha = state.bestCurrentStreak,
                        points = points,
                        levelName = levelName,
                        pointsToNext = pointsToNext,
                        levelProgress = levelProgress
                    )
                }
            }

            item {
                SectionTitle(
                    title = "Hoy",
                    trailing = {
                        Text(
                            "${state.doneToday} de ${state.todayCards.size}",
                            style = VittaTextStyles.body,
                            color = VittaColorRoles.textMuted
                        )
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

            item {
                TextButton(
                    onClick = onAddHabit,
                    colors = ButtonDefaults.textButtonColors(contentColor = VittaColorRoles.primaryPressed)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(VittaSpacing.Xxs))
                    Text("Nuevo hábito", style = VittaTextStyles.button)
                }
            }

            if (state.otherCards.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Otros días",
                        subtitle = "No están programados para hoy, pero puedes registrarlos si quieres."
                    )
                }
                items(state.otherCards, key = { "other_${it.habit.id}" }) { card ->
                    HabitDashboardCard(card = card, viewModel = viewModel, onEdit = { onEditHabit(card.habit.id) })
                }
            }

            // ---- Bloques de DashboardExtras.kt ----
            item { SleepCard() }

            item { SectionTitle(title = "Tus retos") }

            items(sampleChallenges, key = { it.title }) { challenge ->
                ChallengeCard(challenge)
            }

            item { StoreBanner(onClick = { /* TODO: navegar a la tienda */ }) }

            item { Spacer(Modifier.height(VittaSpacing.Xl)) }
        }
    }
}

@Composable
private fun HeaderBlock(userName: String, level: Int) {
    val today = remember {
        val locale = Locale("es", "ES")
        val date = LocalDate.now()
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val formatted = date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", locale))
        "${dayName.replaceFirstChar { it.uppercase() }} $formatted"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = VittaSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaLogo(size = 28.dp)
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column(Modifier.weight(1f)) {
            Text(today, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            Text(
                if (userName.isBlank()) "Hola" else "Hola, $userName",
                style = VittaTextStyles.heading,
                color = VittaColorRoles.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
        }
        LevelPill(level = level)
    }
}

@Composable
private fun LevelPill(level: Int) {
    Row(
        modifier = Modifier
            .background(VittaColorRoles.primary, VittaShapes.pill)
            .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(VittaColors.OrangeAccent, CircleShape))
        Spacer(Modifier.width(VittaSpacing.Xs))
        Text("Nivel $level", style = VittaTextStyles.caption, color = VittaColorRoles.onPrimary)
    }
}

@Composable
private fun HeroCard(
    doneCount: Int,
    totalCount: Int,
    racha: Int,
    points: Int,
    levelName: String,
    pointsToNext: Int,
    levelProgress: Float
) {
    val sinHabitosHoy = totalCount == 0
    val empezando = doneCount == 0
    val completo = totalCount > 0 && doneCount >= totalCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(VittaShapes.extraLarge)
            .background(VittaColorRoles.surface)
    ) {
        // Burbuja decorativa; el clip de arriba la recorta dentro de la tarjeta.
        Box(
            Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceVariant)
        )

        Column(modifier = Modifier.padding(VittaSpacing.Xl)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            sinHabitosHoy -> "DÍA LIBRE"
                            completo -> "¡DÍA COMPLETO!"
                            empezando -> "TU DÍA EMPIEZA AQUÍ"
                            else -> "VAS BIEN"
                        },
                        style = VittaTextStyles.label,
                        color = VittaColors.OrangeAccent
                    )
                    Spacer(Modifier.height(VittaSpacing.Xs))
                    Text(
                        when {
                            sinHabitosHoy -> "Nada programado hoy"
                            completo -> "Vitta está orgulloso de ti"
                            empezando -> "Vitta te está esperando"
                            else -> "$doneCount de $totalCount hábitos listos"
                        },
                        style = VittaTextStyles.displayLarge,
                        color = VittaColorRoles.textPrimary
                    )
                    Spacer(Modifier.height(VittaSpacing.Sm))
                    Text(
                        when {
                            sinHabitosHoy -> "Puedes registrar hábitos de otros días si quieres."
                            completo -> "Registraste todo lo de hoy. ¡Buen trabajo!"
                            empezando -> "$totalCount hábitos listos para registrar. Empieza por el más fácil."
                            else -> "Vitta ya se siente mejor. Queda poco para cerrar el día."
                        },
                        style = VittaTextStyles.body,
                        color = VittaColorRoles.textSecondary
                    )
                }
                Spacer(Modifier.width(VittaSpacing.Sm))
                VittaMascot(
                    state = if (empezando) MascotState.EmptyState else MascotState.GoodHabits,
                    size = MascotSize.Medium
                )
            }

            Spacer(Modifier.height(VittaSpacing.Lg))

            Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
                HeroStat(
                    icon = R.drawable.ic8_fire_element,
                    value = "$racha",
                    label = if (racha == 1) "día de racha" else "días de racha",
                    modifier = Modifier.weight(1f)
                )
                HeroStat(
                    icon = R.drawable.ic8_trophy,
                    value = formatPoints(points),
                    label = "VitaPuntos",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(VittaSpacing.Md))
            LevelProgress(levelName = levelName, pointsToNext = pointsToNext, progress = levelProgress)
        }
    }
}

@Composable
private fun HeroStat(icon: Int, value: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(VittaColorRoles.surfaceVariant, VittaShapes.medium)
            .padding(VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column {
            Text(value, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
            Text(label, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
        }
    }
}

@Composable
private fun LevelProgress(levelName: String, pointsToNext: Int, progress: Float) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(levelName, style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
            Text(
                "$pointsToNext para el siguiente nivel",
                style = VittaTextStyles.bodySmall,
                color = VittaColorRoles.textMuted
            )
        }
        Spacer(Modifier.height(VittaSpacing.Xs))
        ProgressBar(progress = progress, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(8.dp)
            .clip(CircleShape)
            .background(VittaColors.SurfaceVariant)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(VittaColors.OrangeAccent)
        )
    }
}

@Composable
private fun SectionTitle(
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
        Text(
            "Aún no tienes hábitos",
            style = VittaTextStyles.heading,
            color = VittaColorRoles.textPrimary,
            textAlign = TextAlign.Center
        )
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
    val subtitle = when {
        card.doneToday -> "Registrado hoy"
        card.esAgua && card.metaTarget != null -> "Vas ${card.pendingValue} de ${card.metaTarget} vasos"
        card.metaTarget != null -> "${card.pendingValue} de ${card.metaTarget} ${card.metaUnidad}"
        else -> card.habit.meta
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (card.doneToday) VittaColorRoles.surfaceTint else VittaColorRoles.surface,
                VittaShapes.large
            )
            .border(
                1.dp,
                if (card.doneToday) VittaColors.GreenPale else VittaColorRoles.border,
                VittaShapes.large
            )
            .padding(VittaSpacing.Lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VittaHabitIconBubble(
                icon = card.icon,
                bubbleSize = 52.dp,
                containerColor = if (card.doneToday) VittaColorRoles.surface else VittaColorRoles.surfaceVariant
            )
            Spacer(Modifier.width(VittaSpacing.Md))
            // Tocar el nombre abre la edición del hábito.
            Column(Modifier.weight(1f).clickable(onClick = onEdit)) {
                Text(card.habit.nombre, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                Spacer(Modifier.height(VittaSpacing.Xxs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        subtitle,
                        style = VittaTextStyles.bodySmall,
                        color = VittaColorRoles.textSecondary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (card.streakActual > 0) {
                        Spacer(Modifier.width(VittaSpacing.Sm))
                        StreakChip(days = card.streakActual)
                    }
                }
            }
            Spacer(Modifier.width(VittaSpacing.Sm))
            TrailingAction(card = card, viewModel = viewModel)
        }

        if (!card.doneToday && card.metaTarget != null) {
            Spacer(Modifier.height(VittaSpacing.Md))
            if (card.esAgua) {
                VittaWaterTracker(
                    filled = card.pendingValue,
                    total = card.metaTarget,
                    onGlassClick = { index -> viewModel.onGlassTap(card.habit.id, index) }
                )
            } else {
                ProgressBar(
                    progress = card.pendingValue.toFloat() / card.metaTarget,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (card.errorMessage != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(card.errorMessage, style = VittaTextStyles.bodySmall, color = VittaColors.Error)
        }
    }
}

@Composable
private fun TrailingAction(card: HabitCardUiState, viewModel: ChecklistViewModel) {
    when {
        card.doneToday -> {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(VittaColorRoles.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Completado",
                    tint = VittaColorRoles.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        card.isSubmitting -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = VittaColorRoles.primary
            )
        }

        card.esAgua && card.metaTarget != null -> {
            OutlinedPill(text = "Todos", onClick = { viewModel.onMarkAll(card.habit.id) })
        }

        card.metaTarget != null -> {
            val step = when {
                card.metaUnidad.contains("paso") -> 1000
                card.metaUnidad.contains("minuto") -> 5
                else -> 1
            }
            OutlinedPill(
                text = if (step == 1000) "+1k" else "+$step",
                onClick = { viewModel.onQuickAdd(card.habit.id) }
            )
        }

        else -> {
            OutlinedPill(text = "Listo", onClick = { viewModel.onToggleBoolean(card.habit.id) })
        }
    }
}

@Composable
private fun StreakChip(days: Int) {
    Row(
        modifier = Modifier
            .background(VittaColorRoles.surfaceVariant, VittaShapes.pill)
            .padding(horizontal = VittaSpacing.Sm, vertical = VittaSpacing.Xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(R.drawable.ic8_fire_element), contentDescription = null, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(VittaSpacing.Xxs))
        Text("$days", style = VittaTextStyles.caption, color = VittaColors.OrangeAccent)
    }
}

@Composable
private fun OutlinedPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(VittaShapes.pill)
            .border(1.dp, VittaColorRoles.border, VittaShapes.pill)
            .clickable(onClick = onClick)
            .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = VittaTextStyles.button, color = VittaColorRoles.textPrimary)
    }
}

/** 1240 -> "1.240" (separador de miles con punto, como en el prototipo). */
private fun formatPoints(points: Int): String {
    val symbols = DecimalFormatSymbols(Locale("es", "ES")).apply { groupingSeparator = '.' }
    return DecimalFormat("#,###", symbols).format(points)
}

@Preview(showBackground = true)
@Composable
private fun HeroCardPreview() {
    HeroCard(
        doneCount = 0,
        totalCount = 4,
        racha = 12,
        points = 1240,
        levelName = "Impulsor",
        pointsToNext = 760,
        levelProgress = 0.3f
    )
}

@Preview(showBackground = true)
@Composable
private fun PillsPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
        StreakChip(12)
        OutlinedPill("Todos") {}
        OutlinedPill("Listo") {}
        LevelPill(5)
    }
}