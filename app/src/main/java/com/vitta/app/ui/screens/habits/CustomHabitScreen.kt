package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.forms.VittaTextField
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun CustomHabitScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel: CustomHabitViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
            .verticalScroll(rememberScrollState())
            .padding(VittaSpacing.Lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "‹",
                style = VittaTextStyles.displayLarge,
                color = VittaColorRoles.textPrimary,
                modifier = Modifier.clickable(onClick = onBack).padding(end = VittaSpacing.Md)
            )
            Text("Nuevo hábito", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        }

        Spacer(Modifier.height(VittaSpacing.Xl))

        Text("Nombre", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        Spacer(Modifier.height(VittaSpacing.Xs))
        VittaTextField(value = state.nombre, onValueChange = viewModel::onNombreChange, label = "")
        if (state.nombreError != null) {
            Spacer(Modifier.height(4.dp))
            Text(state.nombreError ?: "", style = VittaTextStyles.body, color = VittaColors.Error)
        }

        Spacer(Modifier.height(VittaSpacing.Xl))
        Text("Icono", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        Spacer(Modifier.height(VittaSpacing.Sm))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
            items(HabitIcon.entries.filter { it != HabitIcon.WaterEmpty }) { icon ->
                val selected = icon == state.icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (selected) VittaColors.SurfaceTint else VittaColors.Surface,
                            VittaShapes.large
                        )
                        .clickable { viewModel.onIconSelect(icon) },
                    contentAlignment = Alignment.Center
                ) {
                    VittaHabitIconBubble(icon = icon, bubbleSize = 40.dp)
                }
            }
        }

        Spacer(Modifier.height(VittaSpacing.Xl))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Meta diaria", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        }
        Spacer(Modifier.height(VittaSpacing.Sm))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColors.Surface, VittaShapes.large)
                .padding(horizontal = VittaSpacing.Xl, vertical = VittaSpacing.Xl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(VittaColors.SurfaceMuted, CircleShape)
                    .clickable { viewModel.onMetaDecrease() },
                contentAlignment = Alignment.Center
            ) {
                Text("–", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
            }
            Text("${state.metaValor}", style = VittaTextStyles.displayLarge, color = VittaColorRoles.textPrimary)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(VittaColorRoles.primary, CircleShape)
                    .clickable { viewModel.onMetaIncrease() },
                contentAlignment = Alignment.Center
            ) {
                Text("+", style = VittaTextStyles.heading, color = VittaColorRoles.background)
            }
        }

        Spacer(Modifier.height(VittaSpacing.Sm))
        VittaTextField(
            value = state.metaUnidad,
            onValueChange = viewModel::onMetaUnidadChange,
            label = "Unidad (ej: minutos al día, vasos al día)"
        )

        Spacer(Modifier.height(VittaSpacing.Xl))
        Text("Frecuencia", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        Spacer(Modifier.height(VittaSpacing.Sm))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColors.SurfaceMuted, VittaShapes.pill)
                .padding(4.dp)
        ) {
            listOf("Todos los días" to false, "Días específicos" to true).forEach { (text, value) ->
                val selected = state.diasEspecificos == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(if (selected) VittaColors.Surface else VittaColors.SurfaceMuted, VittaShapes.pill)
                        .clickable { viewModel.onFrequencyModeChange(value) }
                        .padding(vertical = VittaSpacing.Md),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text,
                        style = VittaTextStyles.button,
                        color = if (selected) VittaColorRoles.textPrimary else VittaColorRoles.textMuted
                    )
                }
            }
        }

        if (state.diasEspecificos) {
            Spacer(Modifier.height(VittaSpacing.Md))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                    val selected = day in state.selectedDays
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (selected) VittaColorRoles.primary else VittaColors.SurfaceVariant,
                                CircleShape
                            )
                            .clickable { viewModel.toggleDay(day) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            day,
                            style = VittaTextStyles.body,
                            color = if (selected) VittaColorRoles.background else VittaColorRoles.textSecondary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(VittaSpacing.Xl))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColors.Surface, VittaShapes.large)
                .padding(VittaSpacing.Lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Recordatorio", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                Text("Vitta te avisa a esta hora", style = VittaTextStyles.body, color = VittaColorRoles.textMuted)
            }
            Text(state.reminderTime, style = VittaTextStyles.title, color = VittaColors.OrangeAccent)
        }

        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            "Crear un hábito no otorga VitaPuntos. Los puntos llegan cuando registras cumplimiento.",
            style = VittaTextStyles.caption,
            color = VittaColorRoles.textMuted
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(state.errorMessage ?: "", style = VittaTextStyles.body, color = VittaColors.Error)
        }

        Spacer(Modifier.height(VittaSpacing.Xxl))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VittaColorRoles.primary)
            }
        } else {
            VittaPrimaryButton(text = "Guardar hábito", onClick = { viewModel.save(onSaved) })
        }

        Spacer(Modifier.height(VittaSpacing.Xl))
    }
}