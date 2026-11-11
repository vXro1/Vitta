package com.vitta.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.figtree
import com.vitta.app.ui.theme.mockShadow

private val R22 = RoundedCornerShape(22.dp)

/**
 * Aviso inferior con el estilo de la app (tarjeta marfil, borde oliva y la
 * mascota de Vitta) en lugar del snackbar negro de Material. Se usa para
 * "Registro deshecho", "Nota guardada", "Hábito desactivado", "Contraseña
 * actualizada", "Datos de tu cuenta actualizados", etc.
 */
@Composable
fun VittaSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState, modifier.padding(horizontal = 16.dp, vertical = 12.dp)) { data ->
        VittaAppToast(data.visuals.message)
    }
}

/** Tono del aviso según el mensaje: los errores se ven en terracota, el resto en oliva. */
private fun esError(mensaje: String): Boolean {
    val m = mensaje.lowercase()
    return "no se pudo" in m || "error" in m || "no es correcta" in m
}

@Composable
fun VittaAppToast(mensaje: String) {
    val error = esError(mensaje)
    val acento = if (error) Mk.Danger else Mk.Olive
    Row(
        Modifier
            .widthIn(max = 560.dp)
            .fillMaxWidth()
            .mockShadow(R22)
            .clip(R22)
            .background(Mk.Paper)
            .border(1.5.dp, acento.copy(alpha = 0.55f), R22)
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Franja de color a la izquierda, como las tarjetas de estado de la app.
        Box(Modifier.width(6.dp).fillMaxHeight().background(acento))
        Spacer(Modifier.width(10.dp))
        if (error) {
            Box(
                Modifier.size(40.dp).background(Mk.Danger.copy(alpha = 0.10f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = Mk.Danger, modifier = Modifier.size(22.dp))
            }
        } else {
            Box(Modifier.size(44.dp).background(Mk.OliveTile, CircleShape), contentAlignment = Alignment.BottomCenter) {
                VittaMascot(state = MascotState.Peek, size = MascotSize.Small, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(vertical = 12.dp)) {
            Text(if (error) "Algo salió mal" else "¡Listo!", style = figtree(700, 12f), color = acento)
            Text(mensaje, style = figtree(600, 14f, 1.35f), color = Mk.Ink)
        }
        Spacer(Modifier.width(14.dp))
    }
}


