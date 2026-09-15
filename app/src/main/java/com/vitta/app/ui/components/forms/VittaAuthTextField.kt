package com.vitta.app.ui.components.forms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Campo de texto para Login/Registro: label arriba, caja redondeada,
 * borde rojo + mensaje de error debajo cuando hay un problema, y ojito
 * para mostrar/ocultar la contraseña cuando isPassword = true.
 */
@Composable
fun VittaAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    errorText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isError = errorText != null

    Column(modifier = modifier) {
        Text(
            text = label,
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textSecondary
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            shape = VittaShapes.large,
            textStyle = VittaTextStyles.body,
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email
            ),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                            tint = VittaColorRoles.textMuted
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = VittaColors.Surface,
                unfocusedContainerColor = VittaColors.Surface,
                errorContainerColor = VittaColors.Surface,
                focusedBorderColor = VittaColorRoles.primary,
                unfocusedBorderColor = VittaColorRoles.border,
                errorBorderColor = VittaColors.Error,
                cursorColor = VittaColorRoles.primary
            )
        )
        if (isError) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorText,
                style = VittaTextStyles.caption,
                color = VittaColors.Error
            )
        }
    }
}