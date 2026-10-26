package com.vitta.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vitta.app.ui.components.navigation.VittaBottomNavBar
import com.vitta.app.ui.components.navigation.VittaDestination
import com.vitta.app.ui.screens.profile.ProfileScreen
import com.vitta.app.ui.screens.progress.ProgressScreen
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.screens.today.TodayScreen

@Composable
fun MainScreen(
    userName: String,
    userEmail: String?,
    refreshRequests: Int,
    onAddHabit: () -> Unit,
    onEditHabit: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(VittaDestination.Home) }
    val checklistViewModel: ChecklistViewModel = viewModel()
    val checklistState by checklistViewModel.uiState.collectAsState()

    LaunchedEffect(refreshRequests) {
        if (refreshRequests > 0) checklistViewModel.load()
    }

    Scaffold(
        containerColor = VittaColorRoles.background,
        bottomBar = {
            VittaBottomNavBar(
                selected = tab,
                onSelect = { tab = it }
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
                    onEditHabit = onEditHabit
                )
            }
            VittaDestination.Today -> TodayScreen(modifier)
            VittaDestination.Progress -> Box(modifier) {
                ProgressScreen()
            }
            VittaDestination.Achievements -> ComingSoon("Logros", modifier)
            VittaDestination.Profile -> Box(modifier) {
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

@Composable
private fun ComingSoon(title: String, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text("$title: próximamente", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
    }
}