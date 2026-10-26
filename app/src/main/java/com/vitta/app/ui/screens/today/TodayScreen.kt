package com.vitta.app.ui.screens.today

import androidx.compose.foundation.background
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.screens.home.AddHabitButton
import com.vitta.app.ui.screens.home.ChecklistViewModel
import com.vitta.app.ui.screens.home.HabitCardUiState
import com.vitta.app.ui.screens.home.HabitDashboardCard
import com.vitta.app.ui.screens.home.UndoRecordDialog
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Checklist del día con los hábitos reales programados para hoy, separados
 * en pendientes y completados. Comparte el [ChecklistViewModel] con Inicio,
 * así que marcar aquí se refleja allá al instante (y viceversa).
 */
@Composable
fun TodayScreen(
    viewModel: ChecklistViewModel,
    onAddHabit: () -> Unit,
    onEditHabit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var undoTarget by remember { mutableStateOf<HabitCardUiState?>(null) }

    if (state.isLoading) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VittaColorRoles.primary)
        }
        return
    }

    val today = state.todayCards
    val pending = today.filterNot { it.doneToday }
    val done = today.filter { it.doneToday }
    val progress = if (today.isEmpty()) 0f else done.size.toFloat() / today.size

    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 640.dp).fillMaxSize(),
            contentPadding = PaddingValues(VittaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
        ) {
            item {
                Text(
                    "Checklist de hoy",
                    style = VittaTextStyles.heading,
                    color = VittaColorRoles.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
            }

            state.errorMessage?.let { message ->
                item {
                    VittaAuthErrorBanner(message)
                    Spacer(Modifier.height(VittaSpacing.Sm))
                    VittaPrimaryButton(text = "Reintentar", onClick = viewModel::load)
                }
            }

            item { ProgressCard(done = done.size, total = today.size, progress = progress) }

            if (today.isEmpty() && state.errorMessage == null) {
                item {
                    Text(
                        if (state.cards.isEmpty()) "Aún no tienes hábitos. Crea uno para empezar."
                        else "Hoy no tienes hábitos programados. ¡Disfruta el descanso!",
                        style = VittaTextStyles.body,
                        color = VittaColorRoles.textSecondary
                    )
                }
            }

            if (pending.isNotEmpty()) {
                item { SectionHeader("Pendientes", "${pending.size}") }
                items(pending, key = { "p_${it.habit.id}" }) { card ->
                    HabitDashboardCard(card, viewModel, onEdit = { onEditHabit(card.habit.id) }, onUndoRequest = { undoTarget = card })
                }
            }

            if (done.isNotEmpty()) {
                item { SectionHeader("Completados", "${done.size}") }
                items(done, key = { "d_${it.habit.id}" }) { card ->
                    HabitDashboardCard(card, viewModel, onEdit = { onEditHabit(card.habit.id) }, onUndoRequest = { undoTarget = card })
                }
            }

            item { AddHabitButton(onAddHabit) }
            item { Spacer(Modifier.height(VittaSpacing.Lg)) }
        }
    }

    undoTarget?.let { card ->
        UndoRecordDialog(
            habitName = card.habit.nombre,
            onConfirm = { viewModel.onUndo(card.habit.id); undoTarget = null },
            onDismiss = { undoTarget = null }
        )
    }
}

@Composable
private fun ProgressCard(done: Int, total: Int, progress: Float) {
    val complete = total > 0 && done >= total
    Row(
        Modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .padding(VittaSpacing.Lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                when {
                    total == 0 -> "Sin hábitos para hoy"
                    complete -> "¡Día completo!"
                    else -> "$done de $total hábitos registrados"
                },
                style = VittaTextStyles.title,
                color = VittaColorRoles.textPrimary
            )
            Spacer(Modifier.height(VittaSpacing.Xxs))
            Text(
                "${(progress * 100).toInt()} % del día",
                style = VittaTextStyles.bodySmall,
                color = VittaColorRoles.textMuted
            )
            Spacer(Modifier.height(VittaSpacing.Md))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(VittaColors.GreenTrack)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .background(VittaColors.GreenPrimary)
                )
            }
        }
        Spacer(Modifier.width(VittaSpacing.Md))
        VittaMascot(
            state = if (complete) MascotState.Strong else MascotState.GoodHabits,
            size = MascotSize.Small
        )
    }
}

@Composable
private fun SectionHeader(title: String, count: String) {
    Row(Modifier.padding(top = VittaSpacing.Sm), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = VittaTextStyles.title,
            color = VittaColorRoles.textPrimary,
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        Text(count, style = VittaTextStyles.body, color = VittaColorRoles.textMuted)
    }
}
