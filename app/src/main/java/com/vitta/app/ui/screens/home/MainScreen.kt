package com.vitta.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EmojiEvents
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
import com.vitta.app.ui.screens.achievements.AchievementsScreen
import com.vitta.app.ui.screens.profile.ProfileScreen
import com.vitta.app.ui.screens.progress.ProgressScreen
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.figtree

/** Las cinco pestañas de la barra inferior del mockup (Inicio, Hoy, Progreso, Logros y Yo). */
private enum class MainTab(val label: String, val selectedIcon: ImageVector, val icon: ImageVector) {
    Home("Inicio", Icons.Filled.Home, Icons.Outlined.Home),
    Today("Hoy", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    Progress("Progreso", Icons.AutoMirrored.Filled.TrendingUp, Icons.AutoMirrored.Outlined.TrendingUp),
    Achievements("Logros", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    Me("Yo", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * Contenedor principal tras iniciar sesión. Inicio y Hoy comparten el mismo
 * [ChecklistViewModel], así lo que se registra en una pestaña se ve en la otra.
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
    onOpenHelp: () -> Unit,
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(MainTab.Home) }
    val checklistViewModel: ChecklistViewModel = viewModel()

    LaunchedEffect(refreshRequests) {
        if (refreshRequests > 0) checklistViewModel.load()
    }

    Scaffold(
        containerColor = Mk.Screen,
        bottomBar = {
            // Barra del mockup: fondo #FFFCF8, pestaña activa #EEF0E3 con texto/ícono #56633F.
            NavigationBar(containerColor = Mk.Paper, tonalElevation = 0.dp) {
                MainTab.entries.forEach { item ->
                    val selected = tab == item
                    NavigationBarItem(
                        selected = selected,
                        onClick = { tab = item },
                        icon = { Icon(if (selected) item.selectedIcon else item.icon, contentDescription = null) },
                        label = { Text(item.label, style = figtree(if (selected) 700 else 600, 10.5f)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Mk.OliveDeep,
                            selectedTextColor = Mk.OliveDeep,
                            indicatorColor = Mk.OliveTile,
                            unselectedIconColor = Mk.Ink.copy(alpha = 0.5f),
                            unselectedTextColor = Mk.Ink.copy(alpha = 0.5f)
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
                    onEditHabit = onEditHabit,
                    onVerLogros = { tab = MainTab.Achievements }
                )
            }
            MainTab.Today -> Box(modifier) {
                TodayScreen(
                    viewModel = checklistViewModel,
                    onAddHabit = onAddHabit,
                    onEditHabit = onEditHabit,
                    onVerLogros = { tab = MainTab.Achievements }
                )
            }
            MainTab.Progress -> Box(modifier) {
                ProgressScreen()
            }
            MainTab.Achievements -> Box(modifier) {
                AchievementsScreen()
            }
            MainTab.Me -> Box(modifier) {
                ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    onAddHabit = onAddHabit,
                    onOpenHelp = onOpenHelp,
                    onLogout = onLogout
                )
            }
        }
    }
}
