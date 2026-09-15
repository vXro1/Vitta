package com.vitta.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.cards.VittaSurfaceCard
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun FirstHabitTeaserScreen(
    userName: String,
    onChooseFirstHabit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
            .padding(VittaSpacing.Lg)
    ) {
        Text("Hola, $userName", style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
        Spacer(Modifier.height(VittaSpacing.Lg))

        VittaSurfaceCard {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(R.drawable.mascot_onboarding_welcome),
                    contentDescription = null,
                    modifier = Modifier.size(140.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(VittaSpacing.Lg))
                Text(
                    "Empecemos por uno",
                    style = VittaTextStyles.heading,
                    color = VittaColorRoles.textPrimary
                )
                Spacer(Modifier.height(VittaSpacing.Xs))
                Text(
                    "Elige un hábito y Vitta lo tendrá esperando aquí mañana temprano. Con uno basta para arrancar.",
                    style = VittaTextStyles.body,
                    color = VittaColorRoles.textSecondary
                )
                Spacer(Modifier.height(VittaSpacing.Lg))
                VittaPrimaryButton(text = "Elegir mi primer hábito", onClick = onChooseFirstHabit)
            }
        }

        Spacer(Modifier.height(VittaSpacing.Md))

        Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
            Box(modifier = Modifier.weight(1f)) {
                VittaSurfaceCard {
                    Text("0", style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                    Text("días de racha", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                VittaSurfaceCard {
                    Text("0", style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                    Text("VitaPuntos", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
                }
            }
        }
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            "La racha y los puntos aparecen con tu primer registro. Crear el hábito todavía no cuenta.",
            style = VittaTextStyles.caption,
            color = VittaColorRoles.textMuted
        )
    }
}