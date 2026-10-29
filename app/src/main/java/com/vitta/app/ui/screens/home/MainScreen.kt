package com.vitta.app.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.components.navigation.VittaBottomNavBar
import com.vitta.app.ui.components.navigation.VittaDestination
import com.vitta.app.ui.screens.achievements.AchievementsScreen
import com.vitta.app.ui.screens.profile.ProfileScreen
import com.vitta.app.ui.screens.profile.ProfileViewModel
import com.vitta.app.ui.screens.progress.ProgressScreen
import com.vitta.app.ui.theme.VittaColorRoles

/**
 * Contenedor tras iniciar sesión: barra inferior con Inicio, Hoy, Progreso,
 * Logros y Perfil. Inicio y Hoy comparten el mismo [ChecklistViewModel] (lo
 * que se registra en una se ve en la otra) y Logros/Perfil comparten
 * [ProfileViewModel].
 *
 * @param refreshRequests cambia cuando otra pantalla (p. ej. editar hábito)
 *   pide recargar la lista al volver.
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
    var tab by rememberSaveable { mutableStateOf(VittaDestination.Home) }
    val checklistViewModel: ChecklistViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()
    val checklistState by checklistViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(refreshRequests) {
        if (refreshRequests > 0) checklistViewModel.load()
    }

    // Se consume al terminar de mostrarse: consumirlo antes cambiaría la clave y cancelaría el snackbar.
    LaunchedEffect(checklistState.message) {
        val message = checklistState.message ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        snackbarHostState.showSnackbar(message)
        checklistViewModel.consumeMessage()
    }

    // Atrás desde otra pestaña vuelve a Inicio en vez de cerrar la app.
    BackHandler(enabled = tab != VittaDestination.Home) { tab = VittaDestination.Home }

    val verLogros = { tab = VittaDestination.Achievements }

    Scaffold(
        containerColor = VittaColorRoles.background,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = VittaColorRoles.toastSurface,
                    contentColor = VittaColorRoles.toastOnSurface
                )
            }
        },
        bottomBar = {
            VittaBottomNavBar(
                selected = tab,
                onSelect = { destination ->
                    // Entrar a Inicio/Hoy desde otra pestaña (o volver a tocarla) refresca los hábitos.
                    val checklistTabs = setOf(VittaDestination.Home, VittaDestination.Today)
                    if (destination in checklistTabs && (tab !in checklistTabs || tab == destination)) {
                        checklistViewModel.load()
                    }
                    tab = destination
                }
            )
        }
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        when (tab) {
            VittaDestination.Home -> Box(modifier) {
                ChecklistScreen(
                    userName = userName,
                    viewModel = checklistViewModel,
                    onAddHabit = onAddHabit,
                    onEditHabit = onEditHabit,
                    onVerLogros = verLogros
                )
            }
            VittaDestination.Today -> Box(modifier) {
                TodayScreen(
                    viewModel = checklistViewModel,
                    onAddHabit = onAddHabit,
                    onEditHabit = onEditHabit,
                    onVerLogros = verLogros
                )
            }
            VittaDestination.Progress -> Box(modifier) {
                ProgressScreen()
            }
            VittaDestination.Achievements -> Box(modifier) {
                AchievementsScreen(viewModel = profileViewModel)
            }
            VittaDestination.Profile -> Box(modifier) {
                ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    onAddHabit = onAddHabit,
                    onOpenHelp = onOpenHelp,
                    onOpenAchievements = verLogros,
                    onLogout = onLogout,
                    viewModel = profileViewModel
                )
            }
        }
    }
}
