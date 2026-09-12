package com.vitta.app.ui.components.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSizes
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * Vitta's pill-shaped button family. All four variants share the same
 * height/shape/typography rules from the mockup's `<button>` styles —
 * only color and border change between them.
 *
 * Every variant supports [enabled]/[loading] so screens never need to
 * hand-roll a disabled or spinner state.
 */
object VittaButtonDefaults {
    val Height = VittaSizes.ButtonHeightMedium
    val ContentPadding = PaddingValues(horizontal = 24.dp)
}

@Composable
fun VittaPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillWidth: Boolean = true,
    icon: ImageVector? = null
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(VittaButtonDefaults.Height),
        enabled = enabled && !loading,
        shape = VittaShapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = VittaColorRoles.primary,
            contentColor = VittaColorRoles.onPrimary,
            disabledContainerColor = VittaColorRoles.primary.copy(alpha = 0.4f),
            disabledContentColor = VittaColorRoles.onPrimary.copy(alpha = 0.7f)
        ),
        contentPadding = VittaButtonDefaults.ContentPadding
    ) {
        VittaButtonContent(text = text, icon = icon, loading = loading, style = VittaTextStyles.buttonDisplay)
    }
}

@Composable
fun VittaSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillWidth: Boolean = true,
    icon: ImageVector? = null
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(VittaButtonDefaults.Height),
        enabled = enabled && !loading,
        shape = VittaShapes.pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = VittaColorRoles.surfaceMuted,
            contentColor = VittaColorRoles.textPrimary,
            disabledContainerColor = VittaColorRoles.surfaceMuted.copy(alpha = 0.5f),
            disabledContentColor = VittaColorRoles.textDisabled
        ),
        contentPadding = VittaButtonDefaults.ContentPadding
    ) {
        VittaButtonContent(text = text, icon = icon, loading = loading, style = VittaTextStyles.button)
    }
}

@Composable
fun VittaOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    fillWidth: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier)
            .height(VittaButtonDefaults.Height),
        enabled = enabled && !loading,
        shape = VittaShapes.pill,
        border = ButtonDefaults.outlinedButtonBorder(enabled).copy(width = 1.5.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = VittaColorRoles.textSecondary,
            disabledContentColor = VittaColorRoles.textDisabled
        ),
        contentPadding = VittaButtonDefaults.ContentPadding
    ) {
        VittaButtonContent(text = text, icon = icon, loading = loading, style = VittaTextStyles.button)
    }
}

@Composable
fun VittaTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = VittaColorRoles.textSecondary,
            disabledContentColor = VittaColorRoles.textDisabled
        )
    ) {
        Text(text = text, style = VittaTextStyles.button)
    }
}

/** Small circular button for a single icon action (e.g. the reward-sheet close "X"). */
@Composable
fun VittaIconCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    containerColor: androidx.compose.ui.graphics.Color = VittaColorRoles.surfaceVariant,
    contentColor: androidx.compose.ui.graphics.Color = VittaColorRoles.textPrimary
) {
    androidx.compose.material3.IconButton(
        onClick = onClick,
        modifier = modifier.size(size),
        colors = androidx.compose.material3.IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

@Composable
private fun RowScope.VittaButtonContent(
    text: String,
    icon: ImageVector?,
    loading: Boolean,
    style: androidx.compose.ui.text.TextStyle
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            strokeWidth = 2.dp,
            color = LocalContentColor.current
        )
        androidx.compose.foundation.layout.Spacer(Modifier.width(VittaSpacingDefault))
    } else if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        androidx.compose.foundation.layout.Spacer(Modifier.width(VittaSpacingDefault))
    }
    Text(text = text, style = style)
}

private val VittaSpacingDefault = 8.dp

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaButtonsPreview() {
    VittaTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            VittaPrimaryButton(text = "Guardar hábito", onClick = {})
            VittaSecondaryButton(text = "Ver mis logros", onClick = {})
            VittaOutlinedButton(text = "Omitir", onClick = {})
            VittaTextButton(text = "Cancelar", onClick = {})
            VittaPrimaryButton(text = "Guardando…", onClick = {}, loading = true)
            VittaPrimaryButton(text = "Bloqueado", onClick = {}, enabled = false)
        }
    }
}
