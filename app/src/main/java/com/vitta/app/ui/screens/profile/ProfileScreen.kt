package com.vitta.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

private val ProfileMaxWidth = 640.dp

/**
 * Sección "Yo": datos de la cuenta que la app ya conoce (nombre y correo
 * devueltos por el login/registro), resumen de progreso, créditos y
 * cierre de sesión con confirmación. No se muestra nada que el backend
 * no provea (no hay endpoint de perfil ni de preferencias).
 */
@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String?,
    activeHabits: Int,
    bestStreak: Int,
    onLogout: () -> Unit
) {
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val version = remember {
        runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "—"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = ProfileMaxWidth)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(VittaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Lg)
        ) {
            Text("Yo", style = VittaTextStyles.displayLarge, color = VittaColorRoles.textPrimary, modifier = Modifier.semantics { heading() })

            // ---- Tarjeta de perfil ----
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(VittaColorRoles.surface, VittaShapes.extraLarge)
                    .border(1.dp, VittaColorRoles.border, VittaShapes.extraLarge)
                    .padding(VittaSpacing.Xl),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(VittaColorRoles.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userName.trim().take(1).uppercase().ifBlank { "?" },
                        style = VittaTextStyles.displayLarge,
                        color = VittaColorRoles.onPrimary
                    )
                }
                Spacer(Modifier.width(VittaSpacing.Lg))
                Column(Modifier.weight(1f)) {
                    Text(userName.ifBlank { "Usuario de Vitta" }, style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                    if (!userEmail.isNullOrBlank()) {
                        Text(userEmail, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textSecondary)
                    }
                }
            }

            // ---- Progreso ----
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
                ProfileStat(R.drawable.ic8_checklist, "$activeHabits", if (activeHabits == 1) "hábito activo" else "hábitos activos", Modifier.weight(1f))
                ProfileStat(R.drawable.ic8_trophy, "$bestStreak", "mejor racha (días)", Modifier.weight(1f))
            }

            // ---- Cuenta ----
            ProfileSection("Cuenta") {
                ProfileRow(Icons.Outlined.Person, "Nombre", userName.ifBlank { "—" })
                ProfileRow(
                    Icons.Outlined.Email,
                    "Correo",
                    userEmail?.ifBlank { null } ?: "Vuelve a iniciar sesión para verlo"
                )
            }

            // ---- Acerca de ----
            ProfileSection("Acerca de Vitta") {
                ProfileRow(Icons.Outlined.Info, "Versión", version)
                // Atribución exigida por la licencia gratuita de Icons8.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clickable(role = Role.Button, onClickLabel = "Abrir icons8.com") {
                            uriHandler.openUri("https://icons8.com")
                        }
                        .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(painterResource(R.drawable.ic8_paint_palette), contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(VittaSpacing.Md))
                    Column(Modifier.weight(1f)) {
                        Text("Iconos por Icons8", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                        Text("icons8.com · licencia gratuita con atribución", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textSecondary)
                    }
                    Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null, tint = VittaColorRoles.textSecondary)
                }
            }

            // ---- Cerrar sesión ----
            OutlinedButton(
                onClick = { confirmLogout = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = VittaShapes.pill,
                border = BorderStroke(1.5.dp, VittaColors.Error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VittaColors.Error)
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                Spacer(Modifier.width(VittaSpacing.Sm))
                Text("Cerrar sesión", style = VittaTextStyles.button)
            }
            Spacer(Modifier.height(VittaSpacing.Lg))
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            containerColor = VittaColorRoles.surface,
            titleContentColor = VittaColorRoles.textPrimary,
            textContentColor = VittaColorRoles.textSecondary,
            icon = { Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = VittaColors.Error) },
            title = { Text("¿Deseas cerrar sesión?", style = VittaTextStyles.title) },
            text = { Text("Tus hábitos quedan guardados en tu cuenta. Podrás volver a entrar cuando quieras.", style = VittaTextStyles.body) },
            dismissButton = {
                TextButton(
                    onClick = { confirmLogout = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = VittaColorRoles.textSecondary)
                ) { Text("Cancelar", style = VittaTextStyles.button) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLogout = false
                        onLogout()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = VittaColors.Error)
                ) { Text("Cerrar sesión", style = VittaTextStyles.button) }
            }
        )
    }
}

@Composable
private fun ProfileStat(icon: Int, value: String, label: String, modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .border(1.dp, VittaColorRoles.border, VittaShapes.large)
            .padding(VittaSpacing.Md)
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(VittaSpacing.Xs))
        Text(value, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        Text(label, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
    }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            title,
            style = VittaTextStyles.subtitle,
            color = VittaColorRoles.textSecondary,
            modifier = Modifier
                .padding(start = VittaSpacing.Xs, bottom = VittaSpacing.Sm)
                .semantics { heading() }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColorRoles.surface, VittaShapes.large)
                .border(1.dp, VittaColorRoles.border, VittaShapes.large),
            content = content
        )
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md)
            .semantics(mergeDescendants = true) {},
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = VittaColorRoles.textSecondary)
        Spacer(Modifier.width(VittaSpacing.Md))
        Text(label, style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
        Spacer(Modifier.width(VittaSpacing.Md))
        Text(
            value,
            style = VittaTextStyles.subtitle,
            color = VittaColorRoles.textPrimary,
            textAlign = androidx.compose.ui.text.style.TextAlign.End,
            maxLines = 2,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
