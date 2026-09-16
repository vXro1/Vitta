package com.vitta.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.components.brand.VittaLogo
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.screens.habits.VittaHabitDoneIndicator
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

@Composable
fun ChecklistScreen(userName: String) {
    val viewModel: ChecklistViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(VittaColorRoles.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VittaColorRoles.primary)
        }
        return
    }

    val doneCount = state.cards.count { it.doneToday }
    val totalCount = state.cards.size
    val racha = state.cards.maxOfOrNull { it.streakActual } ?: 0

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(VittaColorRoles.background),
        contentPadding = PaddingValues(VittaSpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
    ) {
        item { HeaderBlock(userName = userName) }

        item { HeroCard(doneCount = doneCount, totalCount = totalCount, racha = racha) }

        item { Spacer(Modifier.height(VittaSpacing.Sm)) }

        item {
            Text("Hoy", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        }

        items(state.cards, key = { it.habit.id }) { card ->
            HabitChecklistCard(card = card, viewModel = viewModel)
        }

        item { Spacer(Modifier.height(VittaSpacing.Xl)) }
    }
}

@Composable
private fun HeaderBlock(userName: String) {
    val today = remember {
        val locale = Locale("es", "ES")
        val date = LocalDate.now()
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val formatted = date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", locale))
        "${dayName.replaceFirstChar { it.uppercase() }} $formatted"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        VittaLogo(size = 28.dp)
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column {
            Text(today, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            Text("Hola, $userName", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        }
    }
    Spacer(Modifier.height(VittaSpacing.Lg))
}

@Composable
private fun HeroCard(doneCount: Int, totalCount: Int, racha: Int) {
    val empezando = doneCount == 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(VittaColors.Surface, VittaShapes.large)
    ) {
        // Burbujas decorativas de fondo, recortadas dentro de la tarjeta.
        Box(
            Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceVariant)
        )
        Box(
            Modifier
                .size(90.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = 30.dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceMuted)
        )

        Column(modifier = Modifier.padding(VittaSpacing.Xl)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (empezando) "TU DÍA EMPIEZA AQUÍ" else "VAS BIEN",
                        style = VittaTextStyles.label,
                        color = VittaColors.OrangeAccent
                    )
                    Spacer(Modifier.height(VittaSpacing.Xs))
                    Text(
                        if (empezando) "Vitta te está esperando" else "$doneCount de $totalCount hábitos listos",
                        style = VittaTextStyles.displayLarge,
                        color = VittaColorRoles.textPrimary
                    )
                    Spacer(Modifier.height(VittaSpacing.Sm))
                    Text(
                        if (empezando) "$totalCount hábitos listos para registrar. Empieza por el más fácil."
                        else "Vitta ya se siente mejor. Queda poco para cerrar el día.",
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
                Box(
                    Modifier
                        .weight(1f)
                        .background(VittaColors.SurfaceMuted, VittaShapes.medium)
                        .padding(VittaSpacing.Md)
                ) {
                    Column {
                        Text("🔥 $racha", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                        Text("días de racha", style = VittaTextStyles.body, color = VittaColorRoles.textMuted)
                    }
                }
                Box(
                    Modifier
                        .weight(1f)
                        .background(VittaColors.SurfaceMuted, VittaShapes.medium)
                        .padding(VittaSpacing.Md)
                ) {
                    Column {
                        Text("VitaPuntos", style = VittaTextStyles.body, color = VittaColorRoles.textMuted)
                        Text("Próximamente", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun HabitChecklistCard(card: HabitCardUiState, viewModel: ChecklistViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (card.doneToday) VittaColors.SurfaceTint else VittaColors.Surface, VittaShapes.large)
            .padding(VittaSpacing.Lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (card.icon != null) {
                VittaHabitIconBubble(icon = card.icon, bubbleSize = 48.dp)
            } else {
                Box(
                    Modifier.size(48.dp).background(VittaColors.SurfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        card.habit.nombre.take(1).uppercase(),
                        style = VittaTextStyles.title,
                        color = VittaColorRoles.textSecondary
                    )
                }
            }
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(card.habit.nombre, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                Text(
                    when {
                        card.doneToday -> "Registrado hoy"
                        card.metaTarget != null -> "${card.pendingValue} de ${card.metaTarget} ${card.metaUnidad}"
                        else -> card.habit.meta
                    },
                    style = VittaTextStyles.body,
                    color = VittaColorRoles.textMuted
                )
                if (card.streakActual > 0) {
                    Text("🔥 ${card.streakActual} días", style = VittaTextStyles.bodySmall, color = VittaColors.OrangeAccent)
                }
            }
            Spacer(Modifier.width(VittaSpacing.Sm))
            TrailingControl(card = card, viewModel = viewModel)
        }

        if (card.esAgua && !card.doneToday && card.metaTarget != null) {
            Spacer(Modifier.height(VittaSpacing.Md))
            VittaWaterTracker(
                filled = card.pendingValue,
                total = card.metaTarget,
                onGlassClick = { index -> viewModel.onGlassTap(card.habit.id, index) }
            )
        }

        if (card.isSubmitting) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = VittaColorRoles.primary)
        }
        if (card.errorMessage != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(card.errorMessage, style = VittaTextStyles.bodySmall, color = VittaColors.Error)
        }
    }
}

@Composable
private fun TrailingControl(card: HabitCardUiState, viewModel: ChecklistViewModel) {
    when {
        card.doneToday -> VittaHabitDoneIndicator()

        card.esAgua && card.metaTarget != null -> {
            Box(
                Modifier
                    .background(VittaColors.SurfaceMuted, VittaShapes.pill)
                    .clickable { viewModel.onMarkAll(card.habit.id) }
                    .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm)
            ) {
                Text("Todos", style = VittaTextStyles.button, color = VittaColorRoles.textPrimary)
            }
        }

        card.metaTarget != null -> {
            val step = if (card.metaUnidad.contains("paso")) "+1k" else "+${if (card.metaUnidad.contains("minuto")) 5 else 1}"
            Box(
                Modifier
                    .size(48.dp)
                    .background(VittaColors.SurfaceMuted, CircleShape)
                    .clickable { viewModel.onQuickAdd(card.habit.id) },
                contentAlignment = Alignment.Center
            ) {
                Text(step, style = VittaTextStyles.caption, color = VittaColorRoles.textPrimary)
            }
        }

        else -> {
            Box(
                Modifier
                    .size(40.dp)
                    .background(VittaColors.SurfaceVariant, CircleShape)
                    .clickable { viewModel.onToggleBoolean(card.habit.id) }
            )
        }
    }
}