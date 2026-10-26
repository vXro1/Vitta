package com.vitta.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vitta.app.data.remote.dto.UserPointsDto
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.components.progress.Insignia
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow

private val R28 = RoundedCornerShape(28.dp)
private val R24 = RoundedCornerShape(24.dp)
private val Pill = RoundedCornerShape(percent = 50)
private val MaxWidth = 640.dp

/**
 * Pestaña Perfil, maquetada según el mockup (`vitta-app-maquetado--main`).
 * Nombre, correo, hábitos activos, medallas y VitaPuntos salen de
 * `GET /api/users/me`; la mejor racha la calcula Inicio. Las medallas
 * completas están en la pestaña Logros ([AchievementsScreen]).
 */
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String?,
    activeHabits: Int,
    bestStreak: Int,
    onLogout: () -> Unit,
    onOpenAchievements: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val perfil = state.perfil
    val nombre = perfil?.nombre ?: userName
    val correo = perfil?.correo ?: userEmail
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "—"
    }

    LaunchedEffect(Unit) {
        viewModel.cargarPerfil()
        viewModel.cargarLogros()
    }

    Box(Modifier.fillMaxSize().background(Mk.Screen), contentAlignment = Alignment.TopCenter) {
        // padding:16px 20px 24px
        Column(
            Modifier
                .widthIn(max = MaxWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
        ) {
            // ---- Cabecera: display:flex;align-items:center;gap:16px ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(84.dp).clip(CircleShape).background(Mk.Sand),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    VittaMascot(
                        state = MascotState.GoodHabits,
                        size = MascotSize.Small,
                        modifier = Modifier.offset(y = 6.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        nombre.ifBlank { "Usuario de Vitta" },
                        style = caprasimo(23f, 1.1f, -0.02f),
                        color = Mk.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics { heading() }
                    )
                    if (!correo.isNullOrBlank()) {
                        Text(
                            correo,
                            style = figtree(500, 13f),
                            color = Mk.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                }
            }

            // ---- Resumen: display:flex;gap:10px ----
            Row(
                Modifier.padding(top = 20.dp).fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    "${perfil?.habitos_activos ?: activeHabits}",
                    "Hábitos\nactivos", Mk.OliveTile, Mk.OliveDeep, Mk.OliveText, Modifier.weight(1f)
                )
                StatTile(
                    perfil?.logros_obtenidos?.toString()
                        ?: if (state.cargandoLogros && state.logros.isEmpty()) "—" else "${state.obtenidos}",
                    "Medallas\nobtenidas", Mk.AmberTile, Mk.AmberDeep, Mk.AmberText, Modifier.weight(1f)
                )
                StatTile("$bestStreak", "Mejor\nracha", Mk.Sand, Mk.Ink, Mk.SandText, Modifier.weight(1f))
            }

            perfil?.puntos?.let { puntos ->
                SectionTitle("Tu nivel")
                LevelCard(puntos)
            }

            // ---- Lista tipo menú del mockup ----
            Column(
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .mockShadow(R28)
                    .clip(R28)
                    .background(Mk.Paper)
            ) {
                MenuRow(
                    Icons.Outlined.EmojiEvents,
                    "Mis logros",
                    trailing = if (state.logros.isEmpty()) "Ver" else "${state.obtenidos} de ${state.logros.size}",
                    onClickLabel = "Abrir logros",
                    onClick = onOpenAchievements
                )
                MenuDivider()
                MenuRow(Icons.Outlined.Person, "Cuenta", trailing = correo?.ifBlank { null } ?: "—")
                MenuDivider()
                MenuRow(Icons.Outlined.Info, "Versión", trailing = version)
                MenuDivider()
                // Atribución exigida por la licencia gratuita de Icons8.
                MenuRow(
                    Icons.AutoMirrored.Outlined.OpenInNew,
                    "Iconos por Icons8",
                    trailing = "icons8.com",
                    onClickLabel = "Abrir icons8.com",
                    onClick = { uriHandler.openUri("https://icons8.com") }
                )
                MenuDivider()
                MenuRow(
                    Icons.AutoMirrored.Outlined.Logout,
                    "Cerrar sesión",
                    tint = Mk.Danger,
                    onClick = { confirmLogout = true }
                )
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = Mk.Paper,
            shape = R28,
            icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Mk.Danger) },
            title = { Text("¿Deseas cerrar sesión?", style = caprasimo(21f, 1.15f, -0.015f), color = Mk.Ink) },
            text = {
                Text(
                    "Tus hábitos quedan guardados en tu cuenta. Podrás volver a entrar cuando quieras.",
                    style = figtree(500, 13.5f, 1.5f),
                    color = Mk.Body
                )
            },
            dismissButton = {
                TextButton(onClick = { confirmLogout = false }) {
                    Text("Cancelar", style = figtree(600, 14.5f), color = Mk.SandText)
                }
            },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; onLogout() }) {
                    Text("Cerrar sesión", style = figtree(600, 14.5f), color = Mk.Danger)
                }
            }
        )
    }
}

/** VitaPuntos y nivel reales (spec v2.0 §6.3–6.4), en la tarjeta oscura del mockup. */
@Composable
private fun LevelCard(puntos: UserPointsDto) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(Mk.Night, R28)
            .padding(20.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("NIVEL ${puntos.nivel}", style = figtree(600, 11f, 1f, 0.09f), color = Mk.Amber)
                Text(
                    puntos.nombre_nivel,
                    style = caprasimo(22f, 1.15f),
                    color = Mk.NightText,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${puntos.puntos_totales}", style = caprasimo(26f, 1f), color = Mk.NightText)
                Text("VitaPuntos", style = figtree(600, 12f), color = Mk.NightSub)
            }
        }
        Box(
            Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .height(8.dp)
                .clip(Pill)
                .background(Mk.NightTrack)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(puntos.progresoNivel.coerceIn(0f, 1f))
                    .background(Mk.Amber, Pill)
            )
        }
        Text(
            puntos.puntos_para_siguiente?.let { "Te faltan $it VitaPuntos para el siguiente nivel. Cada hábito cumplido suma 10." }
                ?: "Llegaste al nivel máximo. ¡Eres un ejemplo de constancia!",
            style = figtree(500, 12.5f, 1.4f),
            color = Mk.NightSub,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

/** h3: Caprasimo 17px, letter-spacing -.015em, margin 26px 2px 12px. */
@Composable
internal fun SectionTitle(text: String) {
    Text(
        text,
        style = caprasimo(17f, 1.2f, -0.015f),
        color = Mk.Ink,
        modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 26.dp, bottom = 12.dp).semantics { heading() }
    )
}

/** flex:1;border-radius:24px;padding:16px 14px — número Caprasimo 25px + etiqueta Figtree 600 11.5px. */
@Composable
private fun StatTile(value: String, label: String, bg: Color, valueColor: Color, labelColor: Color, modifier: Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .background(bg, R24)
            .padding(horizontal = 14.dp, vertical = 16.dp)
            .semantics(mergeDescendants = true) {}
    ) {
        Text(value, style = caprasimo(25f, 1f), color = valueColor)
        Text(label, style = figtree(600, 11.5f, 1.3f), color = labelColor, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
internal fun LogrosSection(state: ProfileUiState, onRetry: () -> Unit) {
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
                "${enPalabras(state.obtenidos, mayuscula = true)} de ${enPalabras(state.logros.size)} medallas desbloqueadas. " +
                    "Las bloqueadas muestran lo que falta.",
                style = figtree(400, 14f, 1.5f),
                color = Mk.Body,
                modifier = Modifier.widthIn(max = 300.dp)
            )
            val ultima = state.ultima
            val cercana = state.masCercana
            if (ultima != null) UltimaMedallaCard(ultima)
            else if (cercana != null) MasCercanaCard(cercana)

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

/** Fila de la lista del mockup: min-height:62px;padding:0 18px;gap:14px; ícono #778661 21px. */
@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    tint: Color = Mk.Olive,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 62.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClickLabel = onClickLabel, onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = figtree(600, 14.5f), color = if (tint == Mk.Danger) Mk.Danger else Mk.Ink)
        Spacer(Modifier.width(14.dp))
        if (trailing != null) {
            Text(
                trailing,
                style = figtree(600, 13f),
                color = Mk.Muted,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun MenuDivider() = HorizontalDivider(thickness = 1.dp, color = Mk.Divider)

/** "Siete de trece medallas…": el mockup escribe las cantidades en letras. */
private fun enPalabras(n: Int, mayuscula: Boolean = false): String {
    val palabras = listOf(
        "cero", "una", "dos", "tres", "cuatro", "cinco", "seis", "siete", "ocho", "nueve", "diez",
        "once", "doce", "trece", "catorce", "quince", "dieciséis", "diecisiete", "dieciocho", "diecinueve", "veinte"
    )
    val s = palabras.getOrNull(n) ?: n.toString()
    return if (mayuscula) s.replaceFirstChar { it.uppercase() } else s
}
