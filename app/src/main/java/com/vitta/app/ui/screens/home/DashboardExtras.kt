package com.vitta.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

// Datos de ejemplo hasta que el backend entregue los retos
data class ChallengeUi(
    val title: String,
    val subtitle: String,
    val difficulty: String,
    val progress: Float,
    val progressLabel: String,
    val points: Int
)

val sampleChallenges = listOf(
    ChallengeUi("Semana hidratada", "Agua · Cumplir la meta 7 días", "Intermedio", 5f / 7f, "5 de 7 días", 60),
    ChallengeUi("Libro en marcha", "Lectura · Acumular 100 páginas", "Avanzado", 0.68f, "68 de 100 páginas", 100),
    ChallengeUi("Primera práctica", "Yoga · Completar 3 sesiones", "Básico", 2f / 3f, "2 de 3 sesiones", 30)
)

@Composable
fun SleepCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColors.SurfaceVariant, VittaShapes.large)
            .padding(VittaSpacing.Lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaMascot(state = MascotState.Evening, size = MascotSize.Medium)
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(Modifier.weight(1f)) {
            Text("Dormir · esta noche", style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(
                "Meta de 7 h. Lo registras mañana al despertar.",
                style = VittaTextStyles.body,
                color = VittaColorRoles.textMuted
            )
        }
    }
}

@Composable
fun ChallengeCard(challenge: ChallengeUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColors.Surface, VittaShapes.large)
            .padding(VittaSpacing.Lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Ícono provisional: la inicial del reto
            Box(
                Modifier.size(40.dp).background(VittaColors.SurfaceVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    challenge.title.take(1),
                    style = VittaTextStyles.title,
                    color = VittaColorRoles.textSecondary
                )
            }
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(challenge.title, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                Text(challenge.subtitle, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
            Spacer(Modifier.width(VittaSpacing.Sm))
            Box(
                Modifier
                    .background(VittaColors.OrangeAccent.copy(alpha = 0.2f), VittaShapes.pill)
                    .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Xs)
            ) {
                Text(challenge.difficulty, style = VittaTextStyles.caption, color = VittaColors.OrangeAccent)
            }
        }

        Spacer(Modifier.height(VittaSpacing.Md))

        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressTrack(progress = challenge.progress, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(VittaSpacing.Md))
            Text(challenge.progressLabel, style = VittaTextStyles.caption, color = VittaColorRoles.textSecondary)
            Spacer(Modifier.width(VittaSpacing.Sm))
            Text("+${challenge.points} pts", style = VittaTextStyles.caption, color = VittaColors.OrangeAccent)
        }
    }
}

@Composable
fun StoreBanner(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(VittaShapes.large)
            .background(VittaColorRoles.primary)
            .clickable { onClick() }
            .padding(VittaSpacing.Lg),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaMascot(state = MascotState.Reader, size = MascotSize.Medium)
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(Modifier.weight(1f)) {
            Text("VITTA STORE", style = VittaTextStyles.label, color = VittaColors.OrangeAccent)
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text("Tres recompensas a tu alcance", style = VittaTextStyles.heading, color = Color.White)
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(
                "Rutinas, recetas y retos para los hábitos que ya tienes.",
                style = VittaTextStyles.body,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        Text("›", style = VittaTextStyles.heading, color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun ProgressTrack(progress: Float, modifier: Modifier = Modifier) {
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun DashboardExtrasPreview() {
    Column(
        Modifier.padding(VittaSpacing.Lg),
        verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
    ) {
        SleepCard()
        sampleChallenges.forEach { ChallengeCard(it) }
        StoreBanner(onClick = {})
    }
}