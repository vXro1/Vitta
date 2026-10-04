package com.vitta.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.vitta.app.ui.components.auth.VittaAuthSuccessCard
import com.vitta.app.ui.components.auth.VittaPasswordRequirementsList
import com.vitta.app.ui.components.auth.VittaRequirementRow
import com.vitta.app.ui.components.auth.vittaPasswordRequirements
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.forms.VittaAuthTextField
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaSpacing
import kotlinx.coroutines.delay

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AuthRepository(TokenManager(context)) }
    val viewModel: RegisterViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    val passwordInteraction = remember { MutableInteractionSource() }
    val passwordFocused by passwordInteraction.collectIsFocusedAsState()
    val requirements = remember(state.password) { vittaPasswordRequirements(state.password) }

    LaunchedEffect(state.justRegistered) {
        if (state.justRegistered) {
            delay(1600)
            onRegisterSuccess()
        }
    }

    val submit = {
        focusManager.clearFocus()
        viewModel.register(onRegisterSuccess)
    }
    val fieldsEnabled = !state.isLoading && !state.justRegistered

    VittaAuthScaffold(
        onBack = onNavigateToLogin,
        backContentDescription = "Volver a iniciar sesión",
        overlay = {
            // Tarjeta de éxito flotante, aparece 1.6s y se navega sola.
            AnimatedVisibility(
                visible = state.justRegistered,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VittaColorRoles.scrim)
                        .padding(VittaSpacing.Xxl),
                    contentAlignment = Alignment.Center
                ) {
                    VittaAuthSuccessCard(
                        title = "¡Cuenta creada!",
                        message = "Ya puedes empezar con tus hábitos"
                    )
                }
            }
        }
    ) {
        VittaAuthHeader(
            title = "Crea tu cuenta",
            subtitle = "Tus hábitos y tu progreso quedan guardados en tu cuenta."
        )

        Spacer(Modifier.height(VittaSpacing.Xxl))

        VittaAuthTextField(
            value = state.nombre,
            onValueChange = viewModel::onNombreChange,
            label = "Nombre",
            placeholder = "¿Cómo te llamas?",
            leadingIcon = Icons.Outlined.Person,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            errorText = state.nombreError,
            enabled = fieldsEnabled
        )
        Spacer(Modifier.height(VittaSpacing.Xl))

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
            enabled = fieldsEnabled
        )
        Spacer(Modifier.height(VittaSpacing.Xl))

        VittaAuthTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Contraseña",
            placeholder = "Crea una contraseña segura",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            errorText = state.passwordError,
            enabled = fieldsEnabled,
            interactionSource = passwordInteraction
        )

        // Checklist en vivo: aparece al enfocar/escribir la contraseña o si
        // hubo un error en ella, para que el usuario sepa qué le falta.
        AnimatedVisibility(
            visible = passwordFocused || state.password.isNotEmpty() || state.passwordError != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            VittaPasswordRequirementsList(
                requirements = requirements,
                modifier = Modifier.padding(top = VittaSpacing.Md)
            )
        }
        Spacer(Modifier.height(VittaSpacing.Xl))

        VittaAuthTextField(
            value = state.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirmar contraseña",
            placeholder = "Repite tu contraseña",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { submit() }),
            errorText = state.confirmPasswordError,
            enabled = fieldsEnabled
        )
        AnimatedVisibility(
            visible = state.confirmPassword.isNotEmpty() && state.confirmPasswordError == null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val matches = state.confirmPassword == state.password
            VittaRequirementRow(
                label = if (matches) "Las contraseñas coinciden" else "Las contraseñas aún no coinciden",
                met = matches,
                modifier = Modifier.padding(top = VittaSpacing.Xs)
            )
        }

        state.generalError?.let {
            Spacer(Modifier.height(VittaSpacing.Lg))
            VittaAuthErrorBanner(it)
        }

        Spacer(Modifier.height(VittaSpacing.Xxl))

        VittaPrimaryButton(
            text = if (state.isLoading) "Creando cuenta…" else "Crear cuenta",
            onClick = submit,
            loading = state.isLoading,
            enabled = !state.justRegistered
        )

        Spacer(Modifier.height(VittaSpacing.Lg))

        VittaAuthFooterLink(
            question = "¿Ya tienes cuenta?",
            action = "Inicia sesión",
            onClick = onNavigateToLogin
        )
    }
}
