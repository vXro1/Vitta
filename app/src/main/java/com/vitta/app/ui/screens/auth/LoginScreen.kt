package com.vitta.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
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
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { AuthRepository(TokenManager(context)) }
    val viewModel: LoginViewModel = viewModel(factory = AuthViewModelFactory(repository))
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceVariant)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VittaSpacing.Xxl),
            verticalArrangement = Arrangement.Center
        ) {
            VittaLogo(size = 48.dp)
            Spacer(Modifier.height(VittaSpacing.Xl))

            Text(
                text = "Inicia sesión",
                style = VittaTextStyles.displayLarge,
                color = VittaColorRoles.textPrimary
            )
            Spacer(Modifier.height(VittaSpacing.Xs))
            Text(
                text = "Retoma tu progreso donde lo dejaste.",
                style = VittaTextStyles.body,
                color = VittaColorRoles.textSecondary
            )

            Spacer(Modifier.height(VittaSpacing.Xxxl))

            VittaAuthTextField(
                value = state.correo,
                onValueChange = viewModel::onCorreoChange,
                label = "Correo",
                errorText = state.correoError
            )
            Spacer(Modifier.height(VittaSpacing.Lg))

            VittaAuthTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                label = "Contraseña",
                isPassword = true,
                errorText = state.passwordError
            )

            if (state.generalError != null) {
                Spacer(Modifier.height(VittaSpacing.Sm))
                Text(
                    text = state.generalError ?: "",
                    style = VittaTextStyles.bodySmall,
                    color = VittaColors.Error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(VittaSpacing.Xxl))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = VittaColorRoles.primary)
                }
            } else {
                VittaPrimaryButton(
                    text = "Iniciar sesión",
                    onClick = { viewModel.login(onSuccess = onLoginSuccess) }
                )
            }

            Spacer(Modifier.height(VittaSpacing.Md))

            VittaTextButton(
                text = "¿No tienes cuenta? Crea una",
                onClick = onNavigateToRegister
            )
        }
    }
}