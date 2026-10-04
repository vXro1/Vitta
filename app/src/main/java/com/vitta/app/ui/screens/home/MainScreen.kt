package com.vitta.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.screens.profile.ProfileScreen
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaTextStyles

private enum class MainTab(val label: String, val selectedIcon: ImageVector, val icon: ImageVector) {
    Home("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    Me("Yo", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * Contenedor principal tras iniciar sesión: barra inferior con "Inicio" y
 * "Yo". Solo existen las pestañas con pantallas reales (no se muestran
 * Progreso/Logros porque aún no están implementadas).
 *
 * @param refreshRequests cambia de valor cuando otra pantalla (p. ej. editar
 *   hábito) pide recargar la lista al volver.
 */
@Composable
fun MainScreen(
    userName: String,
    userEmail: String?,
    refreshRequests: Int,
    onAddHabit: () -> Unit,
    onEditHabit: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.Home) }
    val checklistViewModel: ChecklistViewModel = viewModel()
    val checklistState by checklistViewModel.uiState.collectAsState()

    LaunchedEffect(refreshRequests) {
        if (refreshRequests > 0) checklistViewModel.load()
    }

    Scaffold(
        containerColor = VittaColorRoles.background,
        bottomBar = {
            NavigationBar(containerColor = VittaColorRoles.surface, tonalElevation = 0.dp) {
                MainTab.entries.forEach { item ->
                    val selected = tab == item
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = item },
                        icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null) },
                        label = { Text(item.label, style = VittaTextStyles.bodySmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = VittaColorRoles.primaryPressed,
                            selectedTextColor = VittaColorRoles.primaryPressed,
                            indicatorColor = VittaColorRoles.surfaceTint,
                            unselectedIconColor = VittaColorRoles.textMuted,
                            unselectedTextColor = VittaColorRoles.textMuted
                        )
                    )
                }
            }
        }
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when (tab) {
            MainTab.Home -> Box(modifier) {
                ChecklistScreen(
                    userName = userName,
                    viewModel = checklistViewModel,
                    onAddHabit = onAddHabit,
                    onEditHabit = onEditHabit
                )
            }
            MainTab.Me -> Box(modifier) {
                ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    activeHabits = checklistState.cards.size,
                    bestStreak = checklistState.bestStreakEver,
                    onLogout = onLogout
                )
            }
        }
    }
}
