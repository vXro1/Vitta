package com.vitta.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree

private val R28 = RoundedCornerShape(28.dp)
private val Pill = RoundedCornerShape(percent = 50)

/**
 * Confirmación de acciones con riesgo (eliminar o desactivar un hábito) con
 * el estilo de la app: Vitta preocupado, una etiqueta de riesgo, lo que se
 * pierde y lo que se conserva, y dos botones claros. La opción segura
 * ("Conservar…") es la principal.
 */
@Composable
fun VittaDangerDialog(
    titulo: String,
    mensaje: String,
    confirmar: String,
    cancelar: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    etiqueta: String = "ACCIÓN CON RIESGO",
    conserva: String? = null,
    peligroAlto: Boolean = true
) {
    val acento = if (peligroAlto) Mk.Danger else Mk.Amber
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(R28)
                .background(Mk.Paper)
                .border(2.dp, acento.copy(alpha = 0.35f), R28)
        ) {
            // Cabecera: Vitta preocupado sobre un fondo de alerta.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(acento.copy(alpha = 0.10f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                VittaMascot(state = MascotState.NoGym, size = MascotSize.Large, modifier = Modifier.offset(y = 8.dp))
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .size(44.dp)
                        .background(acento, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = Mk.NightText, modifier = Modifier.size(26.dp))
                }
            }
            Column(Modifier.padding(horizontal = 22.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    etiqueta,
                    style = figtree(700, 11f, 1f, 0.08f),
                    color = acento,
                    modifier = Modifier
                        .background(acento.copy(alpha = 0.10f), Pill)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
                Text(
                    titulo,
                    style = caprasimo(21f, 1.15f, -0.015f),
                    color = Mk.Ink,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Text(
                    mensaje,
                    style = figtree(500, 14f, 1.45f),
                    color = Mk.Body,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (conserva != null) {
                    Row(
                        Modifier
                            .padding(top = 14.dp)
                            .fillMaxWidth()
                            .background(Mk.OliveTile, RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✓", style = figtree(700, 15f), color = Mk.OliveDeep)
                        Spacer(Modifier.width(10.dp))
                        Text(conserva, style = figtree(500, 13f, 1.4f), color = Mk.Ink)
                    }
                }
                Column(Modifier.padding(top = 18.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(Pill)
                            .background(Mk.OliveDeep)
                            .clickable(role = Role.Button, onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cancelar, style = caprasimo(16f, 1.2f), color = Mk.NightText)
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .clip(Pill)
                            .border(1.5.dp, acento, Pill)
                            .clickable(role = Role.Button, onClick = onConfirm),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(confirmar, style = figtree(700, 15f), color = acento)
                    }
                }
            }
        }
    }
}
