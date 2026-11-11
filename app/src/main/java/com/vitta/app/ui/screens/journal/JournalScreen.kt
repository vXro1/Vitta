package com.vitta.app.ui.screens.journal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.screens.habits.HabitScreenHeader
import com.vitta.app.ui.screens.habits.VittaFilterChip
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.VittaIconTint
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Es = Locale("es", "ES")
private val DiaSemana = DateTimeFormatter.ofPattern("EEEE", Es)
private val MesCorto = DateTimeFormatter.ofPattern("MMM yyyy", Es)
private val R24 = RoundedCornerShape(24.dp)
private val R28 = RoundedCornerShape(28.dp)
private val Pill = RoundedCornerShape(percent = 50)

/**
 * Bitácora con las notas reales de cada día.
 * - Con [habitId]: bitácora de un hábito (HAL-14), todas sus notas ordenadas por fecha.
 * - Sin [habitId]: bitácora general (HAL-15).
 * Arriba hay un buscador por letras o números y un botón "Filtros" (hábito y
 * fecha) que abre una hoja inferior, para no saturar la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(habitId: Int?, onBack: () -> Unit) {
    val viewModel: JournalViewModel = viewModel(key = "journal_${habitId ?: "all"}", factory = JournalViewModelFactory(habitId))
    val state by viewModel.uiState.collectAsState()
    var filtrosAbiertos by rememberSaveable { mutableStateOf(false) }
    val titulo = if (habitId == null) "Bitácora" else "Bitácora · ${state.habitFijo?.nombre ?: ""}"

    Column(Modifier.fillMaxSize().background(Mk.Screen)) {
        HabitScreenHeader(
            title = titulo,
            subtitle = if (habitId == null) "Todas tus notas del día" else "Todas las notas de este hábito",
            onBack = onBack
        )
        when {
            state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Mk.Olive)
            }
            state.error != null -> Column(Modifier.padding(20.dp)) {
                VittaAuthErrorBanner(state.error ?: "")
                Spacer(Modifier.height(16.dp))
                VittaPrimaryButton(text = "Reintentar", onClick = viewModel::cargar)
            }
            else -> {
                val notas = state.visibles
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Resumen(total = state.entries.size, habitos = state.habitsConNotas.size, general = habitId == null) }
                    item {
                        BarraBusqueda(
                            texto = state.busqueda,
                            onTexto = viewModel::buscar,
                            filtros = state.filtrosActivos,
                            onFiltros = { filtrosAbiertos = true }
                        )
                    }
                    // Filtros activos como chips pequeños que se quitan con la X.
                    if (state.filtrosActivos > 0) {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                state.habits.firstOrNull { it.id == state.filtroHabito }?.takeIf { habitId == null }?.let { h ->
                                    FiltroActivo(h.nombre) { viewModel.filtrarHabito(null) }
                                }
                                if (state.filtroPeriodo != JournalPeriodo.Todo) {
                                    FiltroActivo(state.filtroPeriodo.label) { viewModel.filtrarPeriodo(JournalPeriodo.Todo) }
                                }
                            }
                        }
                    }
                    item {
                        Text(
                            when (notas.size) { 0 -> "Sin resultados"; 1 -> "1 nota"; else -> "${notas.size} notas" },
                            style = figtree(600, 12.5f),
                            color = Mk.Muted,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    if (notas.isEmpty()) {
                        item { Vacio(hayNotas = state.entries.isNotEmpty(), onLimpiar = viewModel::limpiarFiltros) }
                    }
                    items(notas, key = { it.recordId }) { NotaCard(it, mostrarHabito = habitId == null, resaltar = state.busqueda.trim()) }
                }
            }
        }
    }

    if (filtrosAbiertos) {
        ModalBottomSheet(
            onDismissRequest = { filtrosAbiertos = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Mk.Screen
        ) {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 28.dp)) {
                Text("Filtrar notas", style = caprasimo(20f, 1.15f), color = Mk.Ink, modifier = Modifier.semantics { heading() })
                if (habitId == null && state.habitsConNotas.isNotEmpty()) {
                    Text("Hábito", style = figtree(700, 13f), color = Mk.Body, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        VittaFilterChip("Todos", selected = state.filtroHabito == null) { viewModel.filtrarHabito(null) }
                        state.habitsConNotas.forEach { h ->
                            VittaFilterChip(h.nombre, selected = state.filtroHabito == h.id) {
                                viewModel.filtrarHabito(if (state.filtroHabito == h.id) null else h.id)
                            }
                        }
                    }
                }
                Text("Fecha", style = figtree(700, 13f), color = Mk.Body, modifier = Modifier.padding(top = 18.dp, bottom = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    JournalPeriodo.entries.forEach { p ->
                        VittaFilterChip(p.label, selected = state.filtroPeriodo == p) { viewModel.filtrarPeriodo(p) }
                    }
                }
                Spacer(Modifier.height(22.dp))
                VittaPrimaryButton(text = if (state.visibles.size == 1) "Ver 1 nota" else "Ver ${state.visibles.size} notas", onClick = { filtrosAbiertos = false })
            }
        }
    }
}

/** Tarjeta oscura de resumen con Vitta lector. */
@Composable
private fun Resumen(total: Int, habitos: Int, general: Boolean) {
    Row(
        Modifier.padding(top = 4.dp).fillMaxWidth().clip(R28).background(Mk.Night).padding(start = 20.dp, end = 8.dp, top = 16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(Modifier.weight(1f).padding(bottom = 18.dp)) {
            Text("TU BITÁCORA", style = figtree(600, 11f, 1f, 0.09f), color = Mk.Amber)
            Text(
                if (total == 1) "1 nota escrita" else "$total notas escritas",
                style = caprasimo(22f, 1.15f),
                color = Mk.NightText,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                if (general) "En $habitos hábito${if (habitos == 1) "" else "s"}. Cada nota es la prueba de un día cumplido."
                else "Cada nota es la prueba de un día cumplido.",
                style = figtree(500, 12.5f, 1.4f),
                color = Mk.NightSub,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        VittaMascot(state = MascotState.Reader, size = MascotSize.Small, modifier = Modifier.offset(y = 6.dp))
    }
}

@Composable
private fun BarraBusqueda(texto: String, onTexto: (String) -> Unit, filtros: Int, onFiltros: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = texto,
            onValueChange = onTexto,
            modifier = Modifier.weight(1f),
            singleLine = true,
            placeholder = { Text("Buscar por palabra o número…", style = figtree(500, 14f), color = Mk.Muted) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = Mk.OliveDeep) },
            trailingIcon = if (texto.isNotEmpty()) {
                { IconButton(onClick = { onTexto("") }) { Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda", tint = Mk.Muted) } }
            } else null,
            textStyle = figtree(500, 14.5f).copy(color = Mk.Ink),
            shape = Pill,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Mk.Olive,
                unfocusedBorderColor = Mk.Dashed,
                focusedContainerColor = Mk.Paper,
                unfocusedContainerColor = Mk.Paper,
                cursorColor = Mk.OliveDeep
            )
        )
        Spacer(Modifier.width(10.dp))
        Box {
            Box(
                Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (filtros > 0) Mk.OliveDeep else Mk.Paper)
                    .border(1.dp, if (filtros > 0) Mk.OliveDeep else Mk.Dashed, CircleShape)
                    .clickable(role = Role.Button, onClickLabel = "Abrir filtros", onClick = onFiltros),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Tune, contentDescription = "Filtros", tint = if (filtros > 0) Mk.NightText else Mk.OliveDeep)
            }
            if (filtros > 0) {
                Box(
                    Modifier.align(Alignment.TopEnd).size(20.dp).background(Mk.Amber, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("$filtros", style = figtree(700, 11f), color = Mk.Ink) }
            }
        }
    }
}

@Composable
private fun FiltroActivo(texto: String, onQuitar: () -> Unit) {
    Row(
        Modifier
            .clip(Pill)
            .background(Mk.OliveTile)
            .clickable(role = Role.Button, onClickLabel = "Quitar filtro $texto", onClick = onQuitar)
            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, style = figtree(600, 12.5f), color = Mk.OliveDeep)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.Filled.Close, contentDescription = null, tint = Mk.OliveDeep, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun Vacio(hayNotas: Boolean, onLimpiar: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().mockShadow(R28).background(Mk.Paper, R28).padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VittaMascot(state = if (hayNotas) MascotState.NoGym else MascotState.Reader, size = MascotSize.Medium)
        Text(
            if (hayNotas) "No encontramos notas" else "Todavía no hay notas",
            style = caprasimo(20f, 1.15f),
            color = Mk.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            if (hayNotas) "Prueba con otra palabra o quita los filtros."
            else "Cuando completes un hábito, cuéntale a Vitta cómo te fue: tus notas aparecerán aquí con su fecha.",
            style = figtree(500, 13.5f, 1.5f),
            color = Mk.Body,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        if (hayNotas) {
            Text(
                "Quitar filtros",
                style = figtree(700, 14f),
                color = Mk.OliveDeep,
                modifier = Modifier.padding(top = 12.dp).clip(Pill).clickable(onClick = onLimpiar).padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

/** Nota de un día: fecha destacada, hábito, cita y "Ver más" para ampliarla. */
@Composable
private fun NotaCard(entry: JournalEntry, mostrarHabito: Boolean, resaltar: String) {
    var ampliada by rememberSaveable(entry.recordId) { mutableStateOf(false) }
    val larga = entry.nota.length > 110 || entry.nota.contains('\n')
    Row(
        Modifier
            .fillMaxWidth()
            .mockShadow(R24)
            .clip(R24)
            .background(Mk.Paper)
            .clickable(enabled = larga, onClickLabel = if (ampliada) "Ver menos" else "Ver la nota completa") { ampliada = !ampliada }
            .padding(14.dp)
            .animateContentSize()
    ) {
        // Fecha como en un calendario de escritorio.
        Column(
            Modifier
                .width(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (entry.cumplido) Mk.OliveTile else Mk.SandSoft)
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("${entry.dia.dayOfMonth}", style = caprasimo(22f, 1f), color = if (entry.cumplido) Mk.OliveDeep else Mk.Ink)
            Text(entry.dia.format(MesCorto).replaceFirstChar { it.uppercase() }, style = figtree(600, 10.5f), color = Mk.Muted, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (mostrarHabito) {
                    Image(painterResource(entry.icon.drawable), contentDescription = null, modifier = Modifier.size(18.dp), colorFilter = VittaIconTint.habit)
                    Spacer(Modifier.width(6.dp))
                    Text(entry.habitName, style = figtree(700, 13.5f), color = Mk.Ink, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    entry.dia.format(DiaSemana).replaceFirstChar { it.uppercase() },
                    style = figtree(500, 12f),
                    color = Mk.Muted,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    if (entry.cumplido) "Cumplido" else "Parcial",
                    style = figtree(700, 11f),
                    color = if (entry.cumplido) Mk.OliveDeep else Mk.SandText,
                    modifier = Modifier
                        .background(if (entry.cumplido) Mk.OliveTile else Mk.SandSoft, Pill)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Text(
                resaltado("“${entry.nota}”", resaltar),
                style = figtree(500, 14f, 1.5f).copy(fontStyle = FontStyle.Italic),
                color = Mk.Body,
                maxLines = if (ampliada) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 6.dp)
            )
            if (larga) {
                Text(
                    if (ampliada) "Ver menos ▴" else "Ver más ▾",
                    style = figtree(700, 12f),
                    color = Mk.OliveDeep,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/** Marca en ámbar las coincidencias de la búsqueda dentro de la nota. */
private fun resaltado(texto: String, buscado: String): androidx.compose.ui.text.AnnotatedString {
    val q = normalizar(buscado)
    if (q.isEmpty()) return androidx.compose.ui.text.AnnotatedString(texto)
    val base = normalizar(texto)
    return androidx.compose.ui.text.buildAnnotatedString {
        append(texto)
        // NFD puede cambiar longitudes: solo se resalta cuando coinciden.
        if (base.length == texto.length) {
            var i = base.indexOf(q)
            while (i >= 0) {
                addStyle(
                    androidx.compose.ui.text.SpanStyle(
                        background = Mk.AmberTile,
                        color = Mk.Ink,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    i, i + q.length
                )
                i = base.indexOf(q, i + q.length)
            }
        }
    }
}
