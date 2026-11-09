package com.vitta.app.ui.common

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaTextStyles

/**
 * Componente común de los formularios (HAL-01 y HAL-04).
 *
 * - Intercepta el gesto/botón de atrás del sistema (incluido el gesto del
 *   borde izquierdo de la pantalla, que es lo que en el emulador parecía
 *   "tocar fuera") mientras el formulario tenga cambios, y muestra el modal
 *   "¿Seguro que quieres salir? Perderás lo que llevas".
 * - Devuelve la acción que deben usar los botones "atrás" visibles de la
 *   pantalla, para que también pidan confirmación.
 *
 * Sin cambios, salir no pregunta nada.
 */
@Composable
fun rememberExitGuard(
    hasChanges: Boolean,
    onExit: () -> Unit,
    stayLabel: String = "Seguir con el registro"
): () -> Unit {
    var asking by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = hasChanges) { asking = true }

    if (asking) {
        AlertDialog(
            onDismissRequest = { asking = false },
            containerColor = VittaColorRoles.surface,
            title = {
                Text("¿Seguro que quieres salir?", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
            },
            text = {
                Text("Perderás lo que llevas.", style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = { asking = false }) {
                    Text(stayLabel, style = VittaTextStyles.button, color = VittaColorRoles.primaryPressed)
                }
            },
            dismissButton = {
                TextButton(onClick = { asking = false; onExit() }) {
                    Text("Salir", style = VittaTextStyles.button, color = VittaColors.Error)
                }
            }
        )
    }

    return { if (hasChanges) asking = true else onExit() }
}
