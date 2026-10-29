package com.vitta.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.vitta.app.ui.components.buttons.VittaOutlinedButton
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaTextStyles
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.repository.AuthRepository
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.auth.VittaAuthFooterLink
import com.vitta.app.ui.components.auth.VittaAuthHeader
import com.vitta.app.ui.components.auth.VittaAuthScaffold
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.forms.VittaAuthTextField
import com.vitta.app.ui.theme.VittaSpacing

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val repository = remember { AuthRepository(TokenManager(context)) }
    val viewModel: LoginViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val submit = {
        focusManager.clearFocus()
        viewModel.login(onSuccess = onLoginSuccess)
    }

    VittaAuthScaffold(
        onBack = onBack,
        backContentDescription = "Volver a la introducción"
    ) {
        VittaAuthHeader(
            title = "Inicia sesión",
            subtitle = "Retoma tu progreso donde lo dejaste."
        )

        Spacer(Modifier.height(VittaSpacing.Xxxl))

        VittaAuthTextField(
            value = state.correo,
            onValueChange = viewModel::onCorreoChange,
            label = "Correo electrónico",
            placeholder = "nombre@correo.com",
            leadingIcon = Icons.Outlined.Email,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            errorText = state.correoError,
            enabled = !state.isLoading
        )
        Spacer(Modifier.height(VittaSpacing.Xl))

        VittaAuthTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Contraseña",
            placeholder = "Escribe tu contraseña",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { submit() }),
            errorText = state.passwordError,
            enabled = !state.isLoading
        )

        state.generalError?.let {
            Spacer(Modifier.height(VittaSpacing.Lg))
            VittaAuthErrorBanner(it)
        }

        Spacer(Modifier.height(VittaSpacing.Xxl))

        VittaPrimaryButton(
            text = if (state.isLoading) "Iniciando sesión…" else "Iniciar sesión",
            onClick = submit,
            loading = state.isLoading,
            enabled = !state.isDemoLoading
        )

        Spacer(Modifier.height(VittaSpacing.Lg))
        DemoDivider()
        Spacer(Modifier.height(VittaSpacing.Lg))

        VittaOutlinedButton(
            text = if (state.isDemoLoading) "Preparando la demo…" else "Entrar con la cuenta demo",
            onClick = {
                focusManager.clearFocus()
                viewModel.enterDemo(onSuccess = onLoginSuccess)
            },
            loading = state.isDemoLoading,
            enabled = !state.isLoading,
            icon = Icons.Outlined.PlayCircle
        )
        Text(
            "Explora Vitta con hábitos y progreso de ejemplo, sin crear una cuenta.",
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = VittaSpacing.Sm)
        )

        Spacer(Modifier.height(VittaSpacing.Xl))

        VittaAuthFooterLink(
            question = "¿No tienes cuenta?",
            action = "Crea una",
            onClick = onNavigateToRegister
        )
    }
}

@Composable
private fun DemoDivider() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        HorizontalDivider(Modifier.weight(1f), color = VittaColorRoles.border)
        Text(
            "o",
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textMuted,
            modifier = Modifier.padding(horizontal = VittaSpacing.Md)
        )
        HorizontalDivider(Modifier.weight(1f), color = VittaColorRoles.border)
    }
}
