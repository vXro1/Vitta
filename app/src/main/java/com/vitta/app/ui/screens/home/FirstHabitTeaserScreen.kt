package com.vitta.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.cards.VittaSurfaceCard
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun FirstHabitTeaserScreen(
    userName: String,
    onChooseFirstHabit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceVariant)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VittaSpacing.Lg)
        ) {
            Text(
                if (userName.isBlank()) "Hola" else "Hola, $userName",
                style = VittaTextStyles.heading,
                color = VittaColorRoles.textPrimary
            )

            Spacer(Modifier.height(VittaSpacing.Lg))

            VittaSurfaceCard {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(R.drawable.mascot_onboarding_welcome),
                        contentDescription = null,
                        modifier = Modifier.size(180.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(VittaSpacing.Lg))
                    Text(
                        "Empecemos por uno",
                        style = VittaTextStyles.displayLarge,
                        color = VittaColorRoles.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(VittaSpacing.Sm))
                    Text(
                        "Elige un hábito y Vitta lo tendrá esperando aquí mañana temprano. Con uno basta para arrancar.",
                        style = VittaTextStyles.body,
                        color = VittaColorRoles.textSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(VittaSpacing.Lg))
                    VittaPrimaryButton(text = "Elegir mi primer hábito", onClick = onChooseFirstHabit)
                }
            }

            Spacer(Modifier.height(VittaSpacing.Lg))

            Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
                Box(modifier = Modifier.weight(1f)) {
                    VittaSurfaceCard {
                        Text("0", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                        Text("días de racha", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
                    }
                }
                Box(modifier = Modifier.weight(1f)) {
                    VittaSurfaceCard {
                        Text("0", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                        Text("VitaPuntos", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
                    }
                }
            }
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(
                "La racha y los puntos aparecen con tu primer registro. Crear el hábito todavía no cuenta.",
                style = VittaTextStyles.caption,
                color = VittaColorRoles.textMuted
            )
            Spacer(Modifier.height(VittaSpacing.Lg))
        }
    }
}