package com.vitta.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.repository.AuthRepository
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.buttons.VittaTextButton
import com.vitta.app.ui.components.forms.VittaTextField
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AuthRepository(TokenManager(context)) }
    val viewModel: RegisterViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
            .padding(horizontal = VittaSpacing.Xxl),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Crea tu cuenta",
            style = VittaTextStyles.heading,
            color = VittaColorRoles.textPrimary
        )
        Spacer(Modifier.height(VittaSpacing.Xs))
        Text(
            text = "Tus hábitos y tu progreso quedan guardados en tu cuenta.",
            style = VittaTextStyles.body,
            color = VittaColorRoles.textSecondary
        )

        Spacer(Modifier.height(VittaSpacing.Xxxl))

        VittaTextField(
            value = state.nombre,
            onValueChange = viewModel::onNombreChange,
            label = "Nombre"
        )
        Spacer(Modifier.height(VittaSpacing.Lg))

        VittaTextField(
            value = state.correo,
            onValueChange = viewModel::onCorreoChange,
            label = "Correo"
        )
        Spacer(Modifier.height(VittaSpacing.Lg))

        VittaTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = "Contraseña"
        )
        Spacer(Modifier.height(VittaSpacing.Xxs))
        Text(
            text = "Mínimo 8 caracteres.",
            style = VittaTextStyles.caption,
            color = VittaColorRoles.textMuted
        )

        if (state.generalError != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(
                text = state.generalError ?: "",
                style = VittaTextStyles.bodySmall,
                color = VittaColors.Error
            )
        }

        Spacer(Modifier.height(VittaSpacing.Xxl))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VittaColorRoles.primary)
            }
        } else {
            VittaPrimaryButton(
                text = "Crear cuenta",
                onClick = { viewModel.register(onSuccess = onRegisterSuccess) }
            )
        }

        Spacer(Modifier.height(VittaSpacing.Md))

        VittaTextButton(
            text = "¿Ya tienes cuenta? Inicia sesión",
            onClick = onNavigateToLogin
        )
    }
}