package com.vitta.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.repository.AuthRepository
import com.vitta.app.ui.components.brand.VittaLogo
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.buttons.VittaTextButton
import com.vitta.app.ui.components.forms.VittaAuthTextField
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
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

    LaunchedEffect(state.justRegistered) {
        if (state.justRegistered) {
            delay(1600)
            onRegisterSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(VittaColorRoles.background)) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = (-50).dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceVariant)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = VittaSpacing.Xxl),
            verticalArrangement = Arrangement.Center
        ) {
            VittaLogo(size = 40.dp)
            Spacer(Modifier.height(VittaSpacing.Lg))

            Text("Crea tu cuenta", style = VittaTextStyles.displayLarge, color = VittaColorRoles.textPrimary)
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(
                "Tus hábitos y tu progreso quedan guardados en tu cuenta.",
                style = VittaTextStyles.body,
                color = VittaColorRoles.textSecondary
            )

            Spacer(Modifier.height(VittaSpacing.Xxl))

            VittaAuthTextField(state.nombre, viewModel::onNombreChange, "Nombre", errorText = state.nombreError)
            Spacer(Modifier.height(VittaSpacing.Lg))

            VittaAuthTextField(state.correo, viewModel::onCorreoChange, "Correo", errorText = state.correoError)
            Spacer(Modifier.height(VittaSpacing.Lg))

            VittaAuthTextField(
                state.password, viewModel::onPasswordChange, "Contraseña",
                isPassword = true, errorText = state.passwordError
            )
            if (state.passwordError == null) {
                Spacer(Modifier.height(VittaSpacing.Xxs))
                Text("Mínimo 8 caracteres.", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            }
            Spacer(Modifier.height(VittaSpacing.Lg))

            VittaAuthTextField(
                state.confirmPassword, viewModel::onConfirmPasswordChange, "Confirmar contraseña",
                isPassword = true, errorText = state.confirmPasswordError
            )

            if (state.generalError != null) {
                Spacer(Modifier.height(VittaSpacing.Sm))
                Text(
                    state.generalError ?: "", style = VittaTextStyles.bodySmall, color = VittaColors.Error,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(VittaSpacing.Xxl))

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VittaColorRoles.primary)
                }
            } else {
                VittaPrimaryButton(text = "Crear cuenta", onClick = { viewModel.register(onRegisterSuccess) })
            }

            Spacer(Modifier.height(VittaSpacing.Md))
            VittaTextButton(text = "Ya tengo cuenta", onClick = onNavigateToLogin)
        }

        // Tarjeta de éxito flotante, aparece 1.6s y se navega sola.
        AnimatedVisibility(
            visible = state.justRegistered,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center).padding(VittaSpacing.Xxl)
        ) {
            Column(
                modifier = Modifier
                    .background(VittaColors.Surface, VittaShapes.large)
                    .padding(VittaSpacing.Xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = VittaColorRoles.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(VittaSpacing.Md))
                Text("¡Cuenta creada!", style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
                Text("Ya puedes empezar con tus hábitos", style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
            }
        }
    }
}