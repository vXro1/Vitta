package com.vitta.app.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.common.rememberExitGuard
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.forms.VittaAuthTextField
import com.vitta.app.ui.screens.habits.HabitInfoNote
import com.vitta.app.ui.screens.habits.HabitScreenHeader
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Cuenta (desde «Yo»): cambiar nombre, correo y contraseña. Todo se guarda
 * en el backend con los endpoints existentes y se valida con las mismas
 * reglas del registro (HAL-02 y HAL-03).
 */
@Composable
fun AccountScreen(onBack: () -> Unit, viewModel: AccountViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbar = remember { SnackbarHostState() }
    val exit = rememberExitGuard(hasChanges = state.hasChanges, onExit = onBack, stayLabel = "Seguir editando")

    LaunchedEffect(state.mensaje) {
        val m = state.mensaje ?: return@LaunchedEffect
        snackbar.showSnackbar(m)
        viewModel.consumirMensaje()
    }

    Box(Modifier.fillMaxSize().background(VittaColorRoles.background)) {
        Column(Modifier.fillMaxSize().imePadding()) {
            HabitScreenHeader(title = "Cuenta", subtitle = "Tus datos se guardan en tu cuenta", onBack = exit)
            when {
                state.cargando -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VittaColorRoles.primary)
                }
                state.errorCarga != null -> Column(Modifier.padding(VittaSpacing.Lg)) {
                    VittaAuthErrorBanner(state.errorCarga ?: "")
                    Spacer(Modifier.height(VittaSpacing.Lg))
                    VittaPrimaryButton(text = "Reintentar", onClick = viewModel::cargar)
                }
                else -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm),
                    verticalArrangement = Arrangement.spacedBy(VittaSpacing.Lg)
                ) {
                    Section("Datos personales") {
                        VittaAuthTextField(
                            value = state.nombre,
                            onValueChange = viewModel::onNombre,
                            label = "Nombre",
                            leadingIcon = Icons.Outlined.Person,
                            keyboardType = KeyboardType.Text,
                            errorText = state.nombreError,
                            enabled = !state.guardandoPerfil
                        )
                        Spacer(Modifier.height(VittaSpacing.Md))
                        VittaAuthTextField(
                            value = state.correo,
                            onValueChange = viewModel::onCorreo,
                            label = "Correo electrónico",
                            leadingIcon = Icons.Outlined.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            errorText = state.correoError,
                            enabled = !state.guardandoPerfil
                        )
                        state.errorPerfil?.let {
                            Spacer(Modifier.height(VittaSpacing.Sm))
                            VittaAuthErrorBanner(it)
                        }
                        Spacer(Modifier.height(VittaSpacing.Lg))
                        VittaPrimaryButton(
                            text = if (state.guardandoPerfil) "Guardando…" else "Guardar datos",
                            loading = state.guardandoPerfil,
                            enabled = state.puedeGuardarPerfil,
                            onClick = { focusManager.clearFocus(); viewModel.guardarPerfil() }
                        )
                    }

                    Section("Contraseña") {
                        VittaAuthTextField(
                            value = state.actual,
                            onValueChange = viewModel::onActual,
                            label = "Contraseña actual",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            errorText = state.actualError,
                            enabled = !state.guardandoClave
                        )
                        Spacer(Modifier.height(VittaSpacing.Md))
                        VittaAuthTextField(
                            value = state.nueva,
                            onValueChange = viewModel::onNueva,
                            label = "Contraseña nueva",
                            placeholder = "Letras, una mayúscula y un número",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            errorText = state.nuevaError,
                            enabled = !state.guardandoClave
                        )
                        // Requisitos obligatorios debajo de la caja: letras, mayúscula y número.
                        com.vitta.app.ui.components.auth.VittaPasswordRequirementsList(
                            requirements = com.vitta.app.ui.components.auth.vittaPasswordRequirements(state.nueva),
                            modifier = Modifier.padding(top = VittaSpacing.Sm)
                        )
                        Spacer(Modifier.height(VittaSpacing.Md))
                        VittaAuthTextField(
                            value = state.confirmar,
                            onValueChange = viewModel::onConfirmar,
                            label = "Confirmar contraseña nueva",
                            leadingIcon = Icons.Outlined.Lock,
                            isPassword = true,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            errorText = state.confirmarError,
                            enabled = !state.guardandoClave
                        )
                        state.errorClave?.let {
                            Spacer(Modifier.height(VittaSpacing.Sm))
                            VittaAuthErrorBanner(it)
                        }
                        Spacer(Modifier.height(VittaSpacing.Lg))
                        VittaPrimaryButton(
                            text = if (state.guardandoClave) "Cambiando…" else "Cambiar contraseña",
                            loading = state.guardandoClave,
                            enabled = state.puedeCambiarClave,
                            onClick = { focusManager.clearFocus(); viewModel.cambiarClave() }
                        )
                    }

                    HabitInfoNote("Al cambiar la contraseña tu sesión sigue abierta en este teléfono; en otros dispositivos tendrás que volver a entrar.")
                    Spacer(Modifier.height(VittaSpacing.Lg))
                }
            }
        }
        com.vitta.app.ui.common.VittaSnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .border(1.dp, VittaColorRoles.border, VittaShapes.large)
            .padding(VittaSpacing.Lg)
    ) {
        Text(
            title,
            style = VittaTextStyles.title,
            color = VittaColorRoles.textPrimary,
            modifier = Modifier.semantics { heading() }.padding(bottom = VittaSpacing.Md)
        )
        content()
    }
}
