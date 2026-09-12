package com.vitta.app.ui.components.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSizes
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/** The five bottom-navigation destinations from the mockup's nav bar. */
enum class VittaDestination(
    val label: String,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    Home("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    Today("Hoy", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    Progress("Progreso", Icons.Filled.TrendingUp, Icons.Outlined.TrendingUp),
    Achievements("Logros", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    Profile("Perfil", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * The 5-tab bottom navigation bar. Purely visual in this phase — [onSelect]
 * is wired up but no screen navigation happens until real screens exist.
 */
@Composable
fun VittaBottomNavBar(
    selected: VittaDestination,
    onSelect: (VittaDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(VittaSizes.BottomNavHeight)
            .background(VittaColorRoles.surface)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
    ) {
        VittaDestination.entries.forEach { destination ->
            val isSelected = destination == selected
            Column(
                modifier = Modifier
                    .selectable(selected = isSelected, onClick = { onSelect(destination) })
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (isSelected) destination.filledIcon else destination.outlinedIcon,
                    contentDescription = destination.label,
                    tint = if (isSelected) VittaColorRoles.primary else VittaColorRoles.textDisabled,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = destination.label,
                    style = VittaTextStyles.caption,
                    color = if (isSelected) VittaColorRoles.textPrimary else VittaColorRoles.textDisabled
                )
            }
        }
    }
}

/**
 * A two/three-option segmented control, e.g. the "Semana · Mes" toggle on
 * the Progress screen.
 */
@Composable
fun VittaSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(VittaColorRoles.surfaceMuted, VittaShapes.pill)
            .padding(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (isSelected) VittaColorRoles.surface else Color.Transparent,
                        VittaShapes.pill
                    )
                    .selectable(selected = isSelected, onClick = { onSelect(index) })
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = VittaTextStyles.subtitle,
                    color = if (isSelected) VittaColorRoles.textPrimary else VittaColorRoles.textMuted
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaBottomNavBarPreview() {
    VittaTheme {
        VittaBottomNavBar(selected = VittaDestination.Home, onSelect = {})
    }
}
