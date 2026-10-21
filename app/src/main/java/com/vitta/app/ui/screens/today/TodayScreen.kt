package com.vitta.app.ui.screens.today

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

// Datos de ejemplo hasta conectar con el backend
private data class TodayHabit(val name: String, val detail: String, val action: String)

private data class TodaySection(val title: String, val range: String, val habits: List<TodayHabit>)

private val sampleSections = listOf(
    TodaySection(
        "Mañana", "06:00 – 12:00",
        listOf(
            TodayHabit("Tomar agua", "Vas 3 de 8 vasos", "Todos"),
            TodayHabit("Hacer yoga", "10 minutos · 07:00", "Listo")
        )
    ),
    TodaySection(
        "Tarde", "12:00 – 19:00",
        listOf(TodayHabit("Caminar", "4.120 de 6.000 pasos", "+1k"))
    ),
    TodaySection(
        "Noche", "19:00 – 23:00",
        listOf(TodayHabit("Leer", "15 de 20 minutos", "+5"))
    )
)

@Composable
fun TodayScreen(modifier: Modifier = Modifier) {
    val allHabits = sampleSections.flatMap { it.habits }
    var done by remember { mutableStateOf(setOf<String>()) }
    val progress = if (allHabits.isEmpty()) 0f else done.size.toFloat() / allHabits.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(VittaSpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
    ) {
        Text("Checklist de hoy", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)

        VittaMascot(state = MascotState.Reader, size = MascotSize.Medium)

        // Tarjeta de progreso
        Column(
            Modifier
                .fillMaxWidth()
                .background(VittaColors.Surface, VittaShapes.large)
                .padding(VittaSpacing.Lg)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${done.size} de ${allHabits.size} hábitos registrados",
                    style = VittaTextStyles.title,
                    color = VittaColorRoles.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = VittaTextStyles.heading,
                    color = VittaColorRoles.textSecondary
                )
            }
            Spacer(Modifier.height(VittaSpacing.Md))
            Box(
                Modifier
                    .fillMaxWidth()
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

        // Secciones: Mañana, Tarde y noche
        sampleSections.forEach { section ->
            Row(verticalAlignment = Alignment.Bottom) {
                Text(section.title, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                Spacer(Modifier.width(VittaSpacing.Sm))
                Text(section.range, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
            section.habits.forEach { habit ->
                val isDone = habit.name in done
                HabitRow(
                    habit = habit,
                    isDone = isDone,
                    onToggle = { done = if (isDone) done - habit.name else done + habit.name }
                )
            }
        }

        Spacer(Modifier.height(VittaSpacing.Lg))
    }
}

@Composable
private fun HabitRow(habit: TodayHabit, isDone: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VittaColors.Surface, VittaShapes.large)
            .padding(VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Círculo de check
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isDone) VittaColors.OrangeAccent else VittaColors.Surface)
                .border(BorderStroke(2.dp, VittaColors.SurfaceVariant), CircleShape)
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Filled.Check, contentDescription = "Hecho", tint = VittaColors.Surface)
            }
        }
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(Modifier.weight(1f)) {
            Text(habit.name, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
            Text(habit.detail, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        }
        Text(habit.action, style = VittaTextStyles.label, color = VittaColors.OrangeAccent)
    }
}