package com.vitta.app.ui.common

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Nota completa de un día (HAL-16): el resumen solo muestra "Ver bitácora"
 * y la nota se lee aquí, con scroll si es larga.
 */
@Composable
fun VittaNoteDialog(
    titulo: String,
    fecha: String,
    nota: String,
    onDismiss: () -> Unit,
    onVerTodas: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VittaColorRoles.surface,
        title = {
            androidx.compose.foundation.layout.Column {
                Text(titulo, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
                Text(fecha, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
        },
        text = {
            Text(
                "“$nota”",
                style = VittaTextStyles.body.copy(fontStyle = FontStyle.Italic),
                color = VittaColorRoles.textSecondary,
                modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", style = VittaTextStyles.button, color = VittaColorRoles.primaryPressed)
            }
        },
        dismissButton = onVerTodas?.let { verTodas ->
            {
                TextButton(onClick = { onDismiss(); verTodas() }) {
                    Text("Ver toda la bitácora", style = VittaTextStyles.button, color = VittaColorRoles.textSecondary)
                }
            }
        }
    )
}
