package com.vitta.app.ui.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/** Vitta's rounded text field, used as-is for search/name inputs and via [VittaPasswordField] for passwords. */
@Composable
fun VittaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = label?.let { { Text(it, style = VittaTextStyles.bodySmall) } },
        placeholder = placeholder?.let { { Text(it, style = VittaTextStyles.body) } },
        isError = isError,
        supportingText = supportingText?.let { { Text(it, style = VittaTextStyles.caption) } },
        singleLine = singleLine,
        shape = VittaShapes.medium,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VittaColorRoles.primary,
            unfocusedBorderColor = VittaColorRoles.border,
            focusedTextColor = VittaColorRoles.textPrimary,
            unfocusedTextColor = VittaColorRoles.textPrimary,
            cursorColor = VittaColorRoles.primary
        )
    )
}

/** A [VittaTextField] with a show/hide toggle for password entry. */
@Composable
fun VittaPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Contraseña"
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label, style = VittaTextStyles.bodySmall) },
        singleLine = true,
        shape = VittaShapes.medium,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VittaColorRoles.primary,
            unfocusedBorderColor = VittaColorRoles.border
        )
    )
}

/** The 7-day pill selector used when scheduling a habit ("L M X J V S D"). */
@Composable
fun VittaDaySelector(
    selectedDays: Set<Int>,
    onDayToggle: (Int) -> Unit,
    modifier: Modifier = Modifier,
    dayLabels: List<String> = listOf("L", "M", "X", "J", "V", "S", "D")
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        dayLabels.forEachIndexed { index, label ->
            val isSelected = index in selectedDays
            Box(
                modifier = Modifier
                    .weight(1f)
                    .size(40.dp)
                    .background(
                        if (isSelected) VittaColorRoles.primary else VittaColorRoles.surfaceMuted,
                        CircleShape
                    )
                    .clickable { onDayToggle(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = VittaTextStyles.subtitle,
                    color = if (isSelected) VittaColorRoles.onPrimary else VittaColorRoles.textMuted
                )
            }
        }
    }
}

/** A settings-style row: label + description on the left, a value or [trailing] control on the right. */
@Composable
fun VittaSettingRow(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .padding(horizontal = VittaSpacing.Xl, vertical = VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
            if (description != null) {
                Text(description, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            }
        }
        trailing()
    }
}

/** A [VittaSettingRow] whose trailing control is a Material 3 switch, themed to Vitta green. */
@Composable
fun VittaSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    VittaSettingRow(title = title, description = description, modifier = modifier) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VittaColorRoles.onPrimary,
                checkedTrackColor = VittaColorRoles.primary
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaFormFieldsPreview() {
    VittaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VittaTextField(value = "", onValueChange = {}, label = "Nombre del hábito")
            VittaDaySelector(selectedDays = setOf(0, 2, 4), onDayToggle = {})
            VittaSwitchRow(title = "Recordatorio", description = "Vitta te avisa a esta hora", checked = true, onCheckedChange = {})
        }
    }
}
