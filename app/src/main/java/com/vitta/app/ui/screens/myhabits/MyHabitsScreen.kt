package com.vitta.app.ui.screens.myhabits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PauseCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.screens.habits.HabitInfoNote
import com.vitta.app.ui.screens.habits.HabitScreenHeader
import com.vitta.app.ui.screens.habits.HabitTextFormat
import com.vitta.app.ui.screens.habits.VittaHabitIconBubble
import com.vitta.app.ui.screens.habits.frequencySummary
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * «Mis hábitos» (HAL-13): lista simple, sin estadísticas, con Desactivar /
 * Activar y Eliminar (con confirmación) y el acceso a la bitácora de cada
 * hábito (HAL-14). Tocar un hábito activo abre su avance (HAL-09).
 */
@Composable
fun MyHabitsScreen(
    onBack: () -> Unit,
    onOpenHabit: (Int) -> Unit,
    onOpenLog: (Int) -> Unit,
    onChanged: () -> Unit,
    viewModel: MyHabitsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var confirmarEliminar by remember { mutableStateOf<MyHabitItem?>(null) }
    var confirmarDesactivar by remember { mutableStateOf<MyHabitItem?>(null) }

    LaunchedEffect(state.mensaje) {
        val m = state.mensaje ?: return@LaunchedEffect
        snackbar.showSnackbar(m)
        viewModel.consumirMensaje()
    }

    Box(Modifier.fillMaxSize().background(VittaColorRoles.background)) {
        Column(Modifier.fillMaxSize()) {
            HabitScreenHeader(title = "Mis hábitos", subtitle = "Todos tus hábitos, activos y desactivados", onBack = onBack)
            when {
                state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VittaColorRoles.primary)
                }
                state.error != null && state.items.isEmpty() -> Column(Modifier.padding(VittaSpacing.Lg)) {
                    VittaAuthErrorBanner(state.error ?: "")
                    Spacer(Modifier.height(VittaSpacing.Lg))
                    VittaPrimaryButton(text = "Reintentar", onClick = viewModel::cargar)
                }
                state.items.isEmpty() -> Column(
                    Modifier.fillMaxWidth().padding(VittaSpacing.Xxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    VittaMascot(state = MascotState.EmptyState, size = MascotSize.Medium)
                    Text(
                        "Todavía no tienes hábitos",
                        style = VittaTextStyles.heading,
                        color = VittaColorRoles.textPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = VittaSpacing.Lg)
                    )
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm),
                    verticalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)
                ) {
                    item { Grupo("Activos (${state.activos.size})") }
                    if (state.activos.isEmpty()) {
                        item { Text("No tienes hábitos activos.", style = VittaTextStyles.body, color = VittaColorRoles.textSecondary) }
                    }
                    items(state.activos, key = { it.habit.id }) { item ->
                        HabitRow(
                            item = item,
                            busy = state.ocupado == item.habit.id,
                            onOpen = { onOpenHabit(item.habit.id) },
                            onLog = { onOpenLog(item.habit.id) },
                            onToggle = { confirmarDesactivar = item },
                            onDelete = { confirmarEliminar = item }
                        )
                    }
                    if (state.desactivados.isNotEmpty()) {
                        item { Grupo("Desactivados (${state.desactivados.size})") }
                        items(state.desactivados, key = { "off_${it.habit.id}" }) { item ->
                            HabitRow(
                                item = item,
                                busy = state.ocupado == item.habit.id,
                                onOpen = null,
                                onLog = { onOpenLog(item.habit.id) },
                                onToggle = { viewModel.cambiarActivo(item, onChanged) },
                                // Con historial el backend no lo borra (borrado lógico): ya está fuera de tus listas.
                                onDelete = if (item.tieneHistorial) null else ({ confirmarEliminar = item })
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(VittaSpacing.Sm))
                        HabitInfoNote(
                            "Desactivar pausa el hábito: deja de aparecer en Inicio y en Hoy y conserva tus días, racha y notas. " +
                                "Eliminar borra el hábito; si ya tiene días registrados se conserva su historial para no perder tus rachas ni tus bitácoras."
                        )
                        Spacer(Modifier.height(VittaSpacing.Xl))
                    }
                }
            }
        }
        com.vitta.app.ui.common.VittaSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }

    confirmarDesactivar?.let { item ->
        com.vitta.app.ui.common.VittaDangerDialog(
            titulo = "¿Desactivar \"${item.habit.nombre}\"?",
            mensaje = "Dejará de aparecer en Inicio y en Hoy y no recibirás su recordatorio.",
            conserva = "Tus días registrados, tu racha y tus notas se conservan. Puedes activarlo cuando quieras.",
            etiqueta = "PAUSAR HÁBITO",
            confirmar = "Sí, desactivar",
            cancelar = "Mantenerlo activo",
            peligroAlto = false,
            onConfirm = { confirmarDesactivar = null; viewModel.cambiarActivo(item, onChanged) },
            onDismiss = { confirmarDesactivar = null }
        )
    }
    confirmarEliminar?.let { item ->
        com.vitta.app.ui.common.VittaDangerDialog(
            titulo = "¿Eliminar \"${item.habit.nombre}\"?",
            mensaje = if (item.tieneHistorial)
                "Saldrá de todas tus listas y dejarás de construir su racha."
            else "Todavía no tiene registros, así que se borrará por completo. Esta acción no se puede deshacer.",
            conserva = if (item.tieneHistorial)
                "Como ya tiene días registrados, su historial (días, racha, notas, VitaPuntos y medallas) se conserva." else null,
            confirmar = "Sí, eliminar",
            cancelar = "Conservar hábito",
            onConfirm = { confirmarEliminar = null; viewModel.eliminar(item, onChanged) },
            onDismiss = { confirmarEliminar = null }
        )
    }
}

@Composable
private fun Grupo(texto: String) {
    Text(
        texto,
        style = VittaTextStyles.title,
        color = VittaColorRoles.textPrimary,
        modifier = Modifier.padding(top = VittaSpacing.Sm).semantics { heading() }
    )
}

@Composable
private fun HabitRow(
    item: MyHabitItem,
    busy: Boolean,
    onOpen: (() -> Unit)?,
    onLog: () -> Unit,
    onToggle: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val (especificos, dias) = HabitTextFormat.parseFrecuencia(item.habit.frecuencia)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(VittaShapes.large)
            .background(VittaColorRoles.surface)
            .border(1.dp, VittaColorRoles.border, VittaShapes.large)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .then(if (onOpen != null) Modifier.clickable(role = Role.Button, onClickLabel = "Ver avance", onClick = onOpen) else Modifier)
                .padding(VittaSpacing.Lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VittaHabitIconBubble(icon = item.icon, bubbleSize = 44.dp, modifier = Modifier.alpha(if (item.activo) 1f else .5f))
            Spacer(Modifier.width(VittaSpacing.Md))
            Column(Modifier.weight(1f)) {
                Text(item.habit.nombre, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                Text(
                    "${item.habit.meta} · ${frequencySummary(especificos, dias)}",
                    style = VittaTextStyles.bodySmall,
                    color = VittaColorRoles.textMuted
                )
            }
            if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = VittaColorRoles.primary)
            else Text(
                if (item.activo) "Activo" else "Desactivado",
                style = VittaTextStyles.caption,
                color = if (item.activo) VittaColorRoles.primaryPressed else VittaColorRoles.textMuted,
                modifier = Modifier
                    .background(if (item.activo) VittaColorRoles.surfaceTint else VittaColorRoles.surfaceMuted, VittaShapes.pill)
                    .padding(horizontal = VittaSpacing.Sm, vertical = VittaSpacing.Xxs)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(start = VittaSpacing.Sm, end = VittaSpacing.Sm, bottom = VittaSpacing.Sm),
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Xxs)
        ) {
            Accion(Icons.AutoMirrored.Outlined.MenuBook, "Ver bitácora (${item.notas})", VittaColorRoles.primaryPressed, !busy, onLog)
            Accion(
                if (item.activo) Icons.Outlined.PauseCircle else Icons.Outlined.PlayCircle,
                if (item.activo) "Desactivar" else "Activar",
                VittaColorRoles.textSecondary,
                !busy,
                onToggle
            )
            if (onDelete != null) Accion(Icons.Outlined.Delete, "Eliminar", VittaColors.Error, !busy, onDelete)
        }
    }
}

@Composable
private fun Accion(icon: ImageVector, label: String, color: Color, enabled: Boolean, onClick: () -> Unit) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.heightIn(min = 44.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(VittaSpacing.Xxs))
        Text(label, style = VittaTextStyles.bodySmall, color = color)
    }
}

@Composable
private fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VittaColorRoles.surface,
        title = { Text(title, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary) },
        text = { Text(text, style = VittaTextStyles.body, color = VittaColorRoles.textSecondary) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirm, style = VittaTextStyles.button, color = VittaColors.Error) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", style = VittaTextStyles.button, color = VittaColorRoles.textSecondary) }
        }
    )
}
