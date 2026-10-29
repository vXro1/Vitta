package com.vitta.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.components.progress.VittaLevelProgressBar
import com.vitta.app.ui.screens.home.formatPoints
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

private val R28 = RoundedCornerShape(28.dp)
private val R24 = RoundedCornerShape(24.dp)
private val MaxWidth = 640.dp

/**
 * Sección "Yo" (pantalla `esPerfil` del mockup) con datos reales:
 * - `GET /api/users/me`: nombre, fecha de alta, hábitos activos, medallas obtenidas y VitaPuntos/nivel.
 * - `GET /api/dashboard`: mejor racha alcanzada.
 * Las medallas viven en su propia pestaña «Logros».
 */
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String?,
    onAddHabit: () -> Unit,
    onOpenHelp: () -> Unit,
    onLogout: () -> Unit,
    onOpenAchievements: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "—"
    }

    LaunchedEffect(Unit) { viewModel.cargarPerfil() }

    val perfil = state.perfil
    val nombre = perfil?.nombre ?: userName
    val correo = perfil?.correo ?: userEmail
    val desde = perfil?.creado_en?.let { contigoDesde(it) }

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
                    val sub = desde ?: correo
                    if (!sub.isNullOrBlank()) {
                        Text(
                            sub,
                            style = figtree(500, 13f),
                            color = Mk.Muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                }
            }

            if (state.errorPerfil != null && perfil == null) {
                Column(
                    Modifier.padding(top = 20.dp).fillMaxWidth().mockShadow(R28).background(Mk.Paper, R28)
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.errorPerfil ?: "", style = figtree(500, 13.5f, 1.5f), color = Mk.Body, textAlign = TextAlign.Center)
                    TextButton(onClick = viewModel::cargarPerfil) { Text("Reintentar", style = figtree(600, 14.5f), color = Mk.Olive) }
                }
            }

            // ---- Nivel: tarjeta oscura (background:#2D403E;border-radius:28px;padding:20px) ----
            perfil?.puntos?.let { p ->
                Column(Modifier.padding(top = 20.dp).fillMaxWidth().background(Mk.Night, R28).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("NIVEL ${p.nivel}", style = figtree(600, 11f, 1f, 0.09f), color = Mk.Amber)
                            Text(p.nombre_nivel, style = caprasimo(22f, 1.1f), color = Mk.NightText, modifier = Modifier.padding(top = 7.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatPoints(p.puntos_totales), style = caprasimo(22f, 1.1f), color = Mk.Amber)
                            Text("VitaPuntos", style = figtree(600, 11.5f), color = Mk.NightSub, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    VittaLevelProgressBar(
                        progress = p.progresoNivel,
                        modifier = Modifier.padding(top = 16.dp).height(9.dp),
                        trackColor = Mk.NightTrack,
                        fillColor = Mk.Amber
                    )
                    Text(
                        p.puntos_para_siguiente?.let { "${formatPoints(it)} para el siguiente nivel" } ?: "Llegaste al nivel máximo",
                        style = figtree(500, 12f),
                        color = Mk.NightSub,
                        modifier = Modifier.padding(top = 9.dp)
                    )
                }
            }

            // ---- Resumen: display:flex;gap:10px ----
            val cargando = state.cargandoPerfil && perfil == null
            Row(
                Modifier.padding(top = 12.dp).fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatTile(
                    if (cargando) "—" else "${perfil?.habitos_activos ?: "—"}",
                    "Hábitos\nactivos", Mk.OliveTile, Mk.OliveDeep, Mk.OliveText, Modifier.weight(1f)
                )
                StatTile(
                    if (cargando) "—" else "${perfil?.logros_obtenidos ?: "—"}",
                    "Medallas\nobtenidas", Mk.AmberTile, Mk.AmberDeep, Mk.AmberText, Modifier.weight(1f)
                )
                StatTile(
                    state.mejorRacha?.toString() ?: "—",
                    "Mejor\nracha", Mk.Sand, Mk.Ink, Mk.SandText, Modifier.weight(1f)
                )
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
                    Icons.AutoMirrored.Outlined.PlaylistAdd,
                    "Agregar un hábito",
                    chevron = true,
                    onClickLabel = "Crear un hábito nuevo",
                    onClick = onAddHabit
                )
                MenuDivider()
                MenuRow(
                    Icons.Outlined.EmojiEvents,
                    "Mis logros",
                    trailing = perfil?.logros_obtenidos?.let { "$it obtenidos" },
                    chevron = true,
                    onClickLabel = "Abrir logros",
                    onClick = onOpenAchievements
                )
                MenuDivider()
                MenuRow(Icons.Outlined.Person, "Cuenta", trailing = correo?.ifBlank { null } ?: "—")
                MenuDivider()
                MenuRow(
                    Icons.AutoMirrored.Outlined.HelpOutline,
                    "Ayuda",
                    chevron = true,
                    onClickLabel = "Abrir la guía de uso",
                    onClick = onOpenHelp
                )
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

            Text(
                "Al eliminar un hábito se conserva el historial ya registrado. Los puntos y las medallas obtenidas no se retiran.",
                style = figtree(400, 12.5f, 1.5f),
                color = Mk.Muted,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 20.dp)
            )
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

/** "Contigo desde junio de 2026" (mockup) a partir de `creado_en`. */
private fun contigoDesde(iso: String): String? = runCatching {
    val fecha = Instant.parse(iso).atZone(ZoneId.systemDefault()).toLocalDate()
    val mes = fecha.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
    "Contigo desde $mes de ${fecha.year}"
}.getOrNull()

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

/** Fila de la lista del mockup: min-height:62px;padding:0 18px;gap:14px; ícono #778661 21px. */
@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    trailing: String? = null,
    tint: Color = Mk.Olive,
    chevron: Boolean = false,
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
        if (chevron) {
            // <svg m9 18 6-6-6-6 stroke:#B8B0A0>
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Mk.Chevron,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MenuDivider() = HorizontalDivider(thickness = 1.dp, color = Mk.Divider)

