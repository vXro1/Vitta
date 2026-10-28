package com.vitta.app.ui.screens.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.components.progress.Insignia
import com.vitta.app.ui.components.progress.VittaLevelProgressBar
import com.vitta.app.ui.screens.profile.LogroUi
import com.vitta.app.ui.screens.profile.ProfileUiState
import com.vitta.app.ui.screens.profile.ProfileViewModel
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow

private val R24 = RoundedCornerShape(24.dp)
private val R26 = RoundedCornerShape(26.dp)
private val R28 = RoundedCornerShape(28.dp)
private val Pill = RoundedCornerShape(percent = 50)
private val MaxWidth = 640.dp

/**
 * Pestaña «Logros» (pantalla `esLogros` del mockup): todas las medallas de
 * `GET /api/logros`, la última obtenida o la más cercana, y el avance de
 * cada medalla pendiente. Ya no vive dentro de «Yo».
 */
@Composable
fun AchievementsScreen(viewModel: ProfileViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    // Al abrir la pestaña se recargan para reflejar medallas recién ganadas.
    LaunchedEffect(Unit) { viewModel.cargarLogros() }

    Box(Modifier.fillMaxSize().background(Mk.Screen), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = MaxWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
        ) {
            Text(
                "Logros",
                style = caprasimo(25f, 1.1f, -0.02f),
                color = Mk.Ink,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(8.dp))
            LogrosSection(state, onRetry = viewModel::cargarLogros)
        }
    }
}

/** h3: Caprasimo 17px, letter-spacing -.015em, margin 26px 2px 12px. */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = caprasimo(17f, 1.2f, -0.015f),
        color = Mk.Ink,
        modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 26.dp, bottom = 12.dp).semantics { heading() }
    )
}

/**
 * Estado vacío del mockup bajo "La más cercana": insignias pendientes en
 * gris (#F4EFE4, radio 26) y "+N" con las que no caben.
 */
@Composable
private fun PendientesStrip(pendientes: List<LogroUi>) {
    Column(
        Modifier.padding(top = 16.dp).fillMaxWidth().background(Mk.SandSoft, R26).padding(horizontal = 18.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            pendientes.take(5).forEach { InsigniaImage(it.insignia, bloqueada = true, size = 44.dp) }
            if (pendientes.size > 5) {
                Text("+${pendientes.size - 5}", style = figtree(700, 13f), color = Mk.Faint, modifier = Modifier.padding(start = 2.dp))
            }
        }
        Text(
            "Cada medalla muestra su condición desde el principio. Ninguna está escondida.",
            style = figtree(500, 12.5f, 1.5f),
            color = Mk.Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
private fun LogrosSection(state: ProfileUiState, onRetry: () -> Unit) {
    when {
        state.cargandoLogros && state.logros.isEmpty() -> Box(
            Modifier.fillMaxWidth().padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(color = Mk.Olive, modifier = Modifier.size(32.dp)) }

        state.errorLogros != null && state.logros.isEmpty() -> Column(
            Modifier.fillMaxWidth().mockShadow(R28).background(Mk.Paper, R28).padding(horizontal = 22.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(state.errorLogros, style = figtree(500, 13.5f, 1.5f), color = Mk.Body, textAlign = TextAlign.Center)
            TextButton(onClick = onRetry) { Text("Reintentar", style = figtree(600, 14.5f), color = Mk.Olive) }
        }

        state.logros.isEmpty() -> Text(
            "Todavía no hay medallas disponibles.",
            style = figtree(400, 14f, 1.5f),
            color = Mk.Body
        )

        else -> Column {
            Text(
                if (state.obtenidos == 0)
                    "${enPalabras(state.logros.size, mayuscula = true)} medallas por desbloquear. " +
                        "La primera llega con tu primer hábito completado."
                else
                    "${enPalabras(state.obtenidos, mayuscula = true)} de ${enPalabras(state.logros.size)} medallas desbloqueadas. " +
                        "Las bloqueadas muestran lo que falta.",
                style = figtree(400, 14f, 1.5f),
                color = Mk.Body,
                modifier = Modifier.widthIn(max = 300.dp)
            )
            val ultima = state.ultima
            val cercana = state.masCercana
            if (ultima != null) UltimaMedallaCard(ultima)
            else if (cercana != null) {
                MasCercanaCard(cercana)
                PendientesStrip(state.logros.filter { !it.obtenido && it.id != cercana.id })
            }

            if (state.constancia.isNotEmpty()) {
                SectionTitle("Constancia")
                MedallasGrid(state.constancia)
            }
            if (state.areas.isNotEmpty()) {
                SectionTitle("Áreas")
                MedallasGrid(state.areas)
            }
        }
    }
}

/** Tarjeta oscura "Última medalla" (background:#2D403E;border-radius:28px;padding:20px;gap:16px). */
@Composable
private fun UltimaMedallaCard(logro: LogroUi) {
    Row(
        Modifier.padding(top = 16.dp).fillMaxWidth().background(Mk.Night, R28).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InsigniaImage(logro.insignia, bloqueada = false, size = 72.dp)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("ÚLTIMA MEDALLA", style = figtree(600, 11f, 1f, 0.09f), color = Mk.Amber)
            Text(logro.nombre, style = caprasimo(19f, 1.15f), color = Mk.NightText, modifier = Modifier.padding(top = 7.dp))
            Text(
                "${logro.condicion} · +${logro.puntosBonus} VitaPuntos",
                style = figtree(500, 12.5f, 1.4f),
                color = Mk.NightSub,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        // <img vitta width:74px;margin:-10px -6px -20px 0>
        VittaMascot(
            state = MascotState.Strong,
            size = MascotSize.Small,
            modifier = Modifier.offset(x = 6.dp, y = 8.dp)
        )
    }
}

/** Estado vacío del mockup: "La más cercana" (background:#FFFCF8;border-radius:28px;padding:26px 22px). */
@Composable
private fun MasCercanaCard(logro: LogroUi) {
    Column(
        Modifier.padding(top = 18.dp).fillMaxWidth().mockShadow(R28).background(Mk.Paper, R28)
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InsigniaImage(logro.insignia, bloqueada = true, size = 92.dp)
        Text("LA MÁS CERCANA", style = figtree(600, 11f, 1f, 0.09f), color = Mk.Faint, modifier = Modifier.padding(top = 16.dp))
        Text(logro.nombre, style = caprasimo(22f, 1.15f, -0.015f), color = Mk.Ink, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 9.dp))
        Text(logro.condicion, style = figtree(500, 13.5f, 1.5f), color = Mk.Body, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 9.dp))
        if (logro.tieneProgreso) {
            MedallaProgreso(logro, Modifier.padding(top = 14.dp).widthIn(max = 260.dp))
        }
        Row(
            Modifier.padding(top = 14.dp).background(Mk.AmberTile, Pill).padding(horizontal = 15.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("+${logro.puntosBonus}", style = caprasimo(14f, 1.2f), color = Mk.AmberInk)
            Spacer(Modifier.width(7.dp))
            Text("VitaPuntos", style = figtree(600, 12.5f), color = Mk.AmberDeep)
        }
    }
}

/** display:grid;grid-template-columns:repeat(2,1fr);gap:11px */
@Composable
private fun MedallasGrid(logros: List<LogroUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        logros.chunked(2).forEach { fila ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                fila.forEach { MedallaCard(it, Modifier.weight(1f)) }
                if (fila.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Tarjeta `medalla()` del mockup. */
@Composable
private fun MedallaCard(logro: LogroUi, modifier: Modifier) {
    val on = logro.obtenido
    Column(
        modifier
            .fillMaxHeight()
            .then(if (on) Modifier.mockShadow(R24) else Modifier)
            .background(if (on) Mk.Paper else Mk.SandSoft, R24)
            .padding(horizontal = 15.dp, vertical = 16.dp)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        InsigniaImage(logro.insignia, bloqueada = !on, size = 62.dp)
        Text(logro.nombre, style = figtree(700, 13.5f, 1.25f), color = Mk.Ink, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
        Text(logro.condicion, style = figtree(500, 11.5f, 1.35f), color = Mk.Muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        if (!on && logro.tieneProgreso) {
            MedallaProgreso(logro, Modifier.padding(top = 10.dp))
        }
        Text(
            "+${logro.puntosBonus} VitaPuntos",
            style = figtree(700, 11f),
            color = if (on) Mk.AmberDeep else Mk.Muted,
            modifier = Modifier
                .padding(top = 10.dp)
                .background(if (on) Mk.AmberTile else Mk.Track, Pill)
                .padding(horizontal = 11.dp, vertical = 4.dp)
        )
    }
}

/**
 * Avance de una medalla pendiente con datos reales de `GET /api/logros`
 * (`progreso_actual` de `progreso_meta`). Mismo trazo que las barras de
 * "Tus retos" del mockup: pista #EDE4D4 de 8px, relleno #F1AA52 y el
 * contador "3 de 7" en Figtree 700 11.5px #77826F (gap 11px).
 */
@Composable
private fun MedallaProgreso(logro: LogroUi, modifier: Modifier = Modifier) {
    val actual = logro.progresoActual ?: return
    val meta = logro.progresoMeta ?: return
    Row(
        modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaLevelProgressBar(
            progress = logro.progresoRelativo ?: 0f,
            modifier = Modifier.weight(1f),
            trackColor = Mk.HabitTrack,
            fillColor = Mk.Amber
        )
        Spacer(Modifier.width(11.dp))
        Text("$actual de $meta", style = figtree(700, 11.5f), color = Mk.SandText)
    }
}

/** Insignia SVG de `assets/vitta/insignias`; bloqueada = filter:grayscale(1) opacity(.38). */
@Composable
private fun InsigniaImage(insignia: Insignia, bloqueada: Boolean, size: Dp) {
    val gris = remember { ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) }
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data("file:///android_asset/" + VittaIconAssets.insignia(insignia.assetName))
            .build(),
        contentDescription = null,
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit,
        alpha = if (bloqueada) 0.38f else 1f,
        colorFilter = if (bloqueada) gris else null
    )
}


/** "Siete de trece medallas…": el mockup escribe las cantidades en letras. */
private fun enPalabras(n: Int, mayuscula: Boolean = false): String {
    val palabras = listOf(
        "cero", "una", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez",
        "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte"
    )
    val s = palabras.getOrNull(n) ?: n.toString()
    return if (mayuscula) s.replaceFirstChar { it.uppercase() } else s
}
