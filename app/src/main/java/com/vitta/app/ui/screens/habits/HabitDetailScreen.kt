package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.common.VittaNoteDialog
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaBackButton
import com.vitta.app.ui.components.buttons.VittaIconCircleButton
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val FechaCorta = DateTimeFormatter.ofPattern("EEE d 'de' MMM", Locale("es", "ES"))
private val FechaLarga = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", Locale("es", "ES"))

/**
 * Avance de un hábito (HAL-09): al tocar un hábito en Inicio o en Hoy se ve
 * su progreso, racha e historial. Editar queda en el ícono del lápiz.
 */
@Composable
fun HabitDetailScreen(
    habitId: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onOpenLog: () -> Unit
) {
    val viewModel: HabitDetailViewModel = viewModel(key = "detail_$habitId", factory = HabitDetailViewModelFactory(habitId))
    val state by viewModel.uiState.collectAsState()
    var notaAbierta by remember { mutableStateOf<HabitDayRecord?>(null) }

    // Se recarga al volver de Editar para mostrar los cambios.
    LaunchedEffect(Unit) { viewModel.cargar() }

    Column(Modifier.fillMaxSize().background(VittaColorRoles.background)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VittaBackButton(onClick = onBack)
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(
                    state.habit?.nombre ?: "Tu hábito",
                    style = VittaTextStyles.heading,
                    color = VittaColorRoles.textPrimary,
                    modifier = Modifier.semantics { heading() }
                )
                Text("Tu avance", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
            if (state.habit != null) {
                VittaIconCircleButton(icon = Icons.Outlined.Edit, contentDescription = "Editar hábito", onClick = onEdit)
            }
        }

        when {
            state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VittaColorRoles.primary)
            }
            state.error != null && state.habit == null -> Column(Modifier.padding(VittaSpacing.Lg)) {
                VittaAuthErrorBanner(state.error ?: "")
                Spacer(Modifier.height(VittaSpacing.Lg))
                VittaPrimaryButton(text = "Reintentar", onClick = viewModel::cargar)
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
            ) {
                item { Resumen(state) }
                item { Hoy(state) }
                item {
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
                        Dato("${state.rachaActual}", if (state.rachaActual == 1) "día de racha" else "días de racha", Modifier.weight(1f), racha = true)
                        Dato("${state.rachaMaxima}", "mejor racha", Modifier.weight(1f))
                        Dato("${state.totalCumplidos}", "días cumplidos", Modifier.weight(1f))
                    }
                }
                item { Ventana("Últimos 7 días", state.semana) }
                item { Ventana("Últimos 30 días", state.mes) }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = VittaSpacing.Sm)) {
                        Text(
                            "Historial",
                            style = VittaTextStyles.title,
                            color = VittaColorRoles.textPrimary,
                            modifier = Modifier.weight(1f).semantics { heading() }
                        )
                        TextButton(onClick = onOpenLog) {
                            Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = null, tint = VittaColorRoles.primaryPressed, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(VittaSpacing.Xxs))
                            Text("Ver bitácora", style = VittaTextStyles.button, color = VittaColorRoles.primaryPressed)
                        }
                    }
                }
                if (state.historial.isEmpty()) {
                    item {
                        Text(
                            "Todavía no hay registros. Cuando completes este hábito o registres avance, aparecerá aquí.",
                            style = VittaTextStyles.body,
                            color = VittaColorRoles.textSecondary
                        )
                    }
                }
                items(state.historial, key = { it.id }) { r ->
                    FilaHistorial(r, state.metaValor, state.metaUnidad, onNota = { notaAbierta = r })
                }
                item { Spacer(Modifier.height(VittaSpacing.Xl)) }
            }
        }
    }

    notaAbierta?.let { r ->
        VittaNoteDialog(
            titulo = "Nota · ${state.habit?.nombre.orEmpty()}",
            fecha = r.dia.format(FechaLarga).replaceFirstChar { it.uppercase() },
            nota = r.nota.orEmpty(),
            onDismiss = { notaAbierta = null },
            onVerTodas = onOpenLog
        )
    }
}

@Composable
private fun Tarjeta(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .border(1.dp, VittaColorRoles.border, VittaShapes.large)
            .padding(VittaSpacing.Lg),
        content = content
    )
}

@Composable
private fun Resumen(state: HabitDetailUiState) {
    val habit = state.habit ?: return
    val (especificos, dias) = HabitTextFormat.parseFrecuencia(habit.frecuencia)
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VittaHabitIconBubble(icon = state.icon, bubbleSize = 56.dp)
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text("Meta: ${habit.meta}", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                Text(frequencySummary(especificos, dias), style = VittaTextStyles.bodySmall, color = VittaColorRoles.textSecondary)
                state.hora?.let {
                    Text("Recordatorio: ${formatReminderTime(it)}", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
                }
            }
        }
    }
}

@Composable
private fun Hoy(state: HabitDetailUiState) {
    val meta = state.metaValor
    val hoy = state.hoy
    val valor = hoy?.valor?.roundToInt() ?: if (hoy?.cumplido == true) meta ?: 0 else 0
    Tarjeta {
        Text("HOY", style = VittaTextStyles.label, color = VittaColorRoles.primaryPressed)
        Spacer(Modifier.height(VittaSpacing.Xs))
        Text(
            when {
                hoy?.cumplido == true -> "¡Cumplido!"
                meta != null -> "$valor de $meta ${state.metaUnidad}"
                else -> "Aún no lo registras"
            },
            style = VittaTextStyles.heading,
            color = VittaColorRoles.textPrimary
        )
        if (meta != null && meta > 0) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Barra(if (hoy?.cumplido == true) 1f else valor.toFloat() / meta)
        }
        Text(
            "Registra tu avance desde Inicio o Hoy.",
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textMuted,
            modifier = Modifier.padding(top = VittaSpacing.Sm)
        )
    }
}

@Composable
private fun Dato(valor: String, etiqueta: String, modifier: Modifier, racha: Boolean = false) {
    Column(
        modifier
            .fillMaxHeight()
            .background(VittaColorRoles.surfaceVariant, VittaShapes.medium)
            .padding(VittaSpacing.Md)
            .semantics(mergeDescendants = true) {}
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (racha) {
                VittaAssetImage(assetPath = VittaIconAssets.streakFlame, contentDescription = null, size = 20.dp)
                Spacer(Modifier.width(VittaSpacing.Xxs))
            }
            Text(valor, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        }
        Text(etiqueta, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
    }
}

@Composable
private fun Ventana(titulo: String, c: Cumplimiento) {
    Tarjeta {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(titulo, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary, modifier = Modifier.weight(1f))
            Text("${(c.ratio * 100).roundToInt()}%", style = VittaTextStyles.title, color = VittaColorRoles.primaryPressed)
        }
        Spacer(Modifier.height(VittaSpacing.Xs))
        Barra(c.ratio)
        Text(
            if (c.programados == 0) "No tocaba ningún día en este periodo"
            else "${c.hechos} de ${c.programados} días programados",
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textSecondary,
            modifier = Modifier.padding(top = VittaSpacing.Xs)
        )
    }
}

@Composable
private fun Barra(progreso: Float) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(VittaColors.GreenTrack)
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(progreso.coerceIn(0f, 1f)).background(VittaColors.GreenPrimary, CircleShape))
    }
}

@Composable
private fun FilaHistorial(r: HabitDayRecord, meta: Int?, unidad: String, onNota: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.medium)
            .border(1.dp, VittaColorRoles.border, VittaShapes.medium)
            .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(28.dp)
                .then(
                    if (r.cumplido) Modifier.background(VittaColorRoles.primaryStrong, CircleShape)
                    else Modifier.border(1.5.dp, VittaColorRoles.border, CircleShape)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (r.cumplido) Icon(Icons.Filled.Check, contentDescription = null, tint = VittaColorRoles.onPrimary, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(Modifier.weight(1f)) {
            Text(r.dia.format(FechaCorta).replaceFirstChar { it.uppercase() }, style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
            val valor = r.valor?.roundToInt()
            Text(
                when {
                    r.cumplido && valor != null && meta != null -> "Cumplido · $valor de $meta $unidad"
                    r.cumplido -> "Cumplido"
                    valor != null && meta != null -> "Avance: $valor de $meta $unidad"
                    else -> "Sin completar"
                },
                style = VittaTextStyles.bodySmall,
                color = VittaColorRoles.textSecondary
            )
        }
        if (r.nota != null) {
            Row(
                Modifier
                    .clip(VittaShapes.pill)
                    .clickable(role = Role.Button, onClickLabel = "Ver nota", onClick = onNota)
                    .padding(horizontal = VittaSpacing.Sm, vertical = VittaSpacing.Xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Outlined.StickyNote2, contentDescription = null, tint = VittaColorRoles.primaryPressed, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(VittaSpacing.Xxs))
                Text("Ver nota", style = VittaTextStyles.caption, color = VittaColorRoles.primaryPressed)
            }
        }
    }
}
