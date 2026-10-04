package com.vitta.app.ui.components.forms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Campo de texto para Login/Registro: label arriba, caja redondeada,
 * borde rojo + mensaje de error debajo cuando hay un problema, y ojito
 * para mostrar/ocultar la contraseña cuando isPassword = true.
 *
 * Todos los colores (texto, placeholder, iconos, cursor) se fijan a mano:
 * si se dejan los de Material, en modo oscuro el texto sale crema sobre
 * la caja crema y "desaparece".
 */
@Composable
fun VittaAuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    errorText: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardType: KeyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Email,
    imeAction: ImeAction = ImeAction.Next,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isError = errorText != null
    val isFocused by interactionSource.collectIsFocusedAsState()
    // Conserva el último error para que el texto no se vacíe mientras se oculta.
    var lastErrorText by remember { mutableStateOf(errorText) }
    if (errorText != null) lastErrorText = errorText

    val iconTint = when {
        isError -> VittaColors.Error
        isFocused -> VittaColorRoles.primaryPressed
        else -> VittaColorRoles.textSecondary
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = VittaTextStyles.subtitle,
            color = if (isError) VittaColors.Error else VittaColorRoles.textPrimary
        )
        Spacer(Modifier.height(VittaSpacing.Sm))

        CompositionLocalProvider(
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = VittaColorRoles.primary,
                backgroundColor = VittaColors.SelectionTint
            )
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .semantics { if (errorText != null) error(errorText) },
                enabled = enabled,
                singleLine = true,
                isError = isError,
                shape = VittaShapes.medium,
                textStyle = VittaTextStyles.body.copy(fontSize = VittaTextStyles.subtitle.fontSize),
                placeholder = placeholder?.let {
                    { Text(it, style = VittaTextStyles.body) }
                },
                leadingIcon = leadingIcon?.let {
                    { Icon(it, contentDescription = null, tint = iconTint) }
                },
                visualTransformation = if (isPassword && !passwordVisible) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = imeAction,
                    autoCorrectEnabled = !isPassword && keyboardType != KeyboardType.Email
                ),
                keyboardActions = keyboardActions,
                interactionSource = interactionSource,
                trailingIcon = if (isPassword) {
                    {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = VittaColorRoles.textSecondary
                            )
                        }
                    }
                } else null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = VittaColorRoles.textPrimary,
                    unfocusedTextColor = VittaColorRoles.textPrimary,
                    errorTextColor = VittaColorRoles.textPrimary,
                    disabledTextColor = VittaColorRoles.textSecondary,
                    focusedContainerColor = VittaColors.Surface,
                    unfocusedContainerColor = VittaColors.Surface,
                    errorContainerColor = VittaColors.Surface,
                    disabledContainerColor = VittaColors.SurfaceMuted,
                    focusedBorderColor = VittaColorRoles.primary,
                    unfocusedBorderColor = VittaColorRoles.border,
                    errorBorderColor = VittaColors.Error,
                    disabledBorderColor = VittaColors.BorderMuted,
                    cursorColor = VittaColorRoles.primaryPressed,
                    errorCursorColor = VittaColors.Error,
                    focusedPlaceholderColor = VittaColorRoles.textSecondary.copy(alpha = 0.8f),
                    unfocusedPlaceholderColor = VittaColorRoles.textSecondary.copy(alpha = 0.8f),
                    errorPlaceholderColor = VittaColorRoles.textSecondary.copy(alpha = 0.8f),
                    disabledPlaceholderColor = VittaColorRoles.textDisabled,
                    focusedLeadingIconColor = VittaColorRoles.primaryPressed,
                    unfocusedLeadingIconColor = VittaColorRoles.textSecondary,
                    errorLeadingIconColor = VittaColors.Error,
                    focusedTrailingIconColor = VittaColorRoles.textSecondary,
                    unfocusedTrailingIconColor = VittaColorRoles.textSecondary,
                    errorTrailingIconColor = VittaColorRoles.textSecondary
                )
            )
        }

        AnimatedVisibility(
            visible = isError,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .padding(top = VittaSpacing.Xs)
                    .semantics { liveRegion = LiveRegionMode.Polite },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.ErrorOutline,
                    contentDescription = null,
                    tint = VittaColors.Error,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(VittaSpacing.Xxs))
                Text(
                    text = lastErrorText.orEmpty(),
                    style = VittaTextStyles.bodySmall,
                    color = VittaColors.Error
                )
            }
        }
    }
}
