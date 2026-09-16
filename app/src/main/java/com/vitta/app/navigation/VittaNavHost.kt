package com.vitta.app.navigation

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.ui.screens.auth.LoginScreen
import com.vitta.app.ui.screens.auth.RegisterScreen
import com.vitta.app.ui.screens.design.DesignSystemScreen
import com.vitta.app.ui.screens.habits.CustomHabitScreen
import com.vitta.app.ui.screens.habits.HabitConfigScreen
import com.vitta.app.ui.screens.habits.HabitSelectionScreen
import com.vitta.app.ui.screens.home.FirstHabitTeaserScreen
import com.vitta.app.ui.screens.onboarding.OnboardingScreen
import kotlinx.coroutines.flow.first

object VittaRoutes {
    const val Onboarding = "onboarding"
    const val Login = "login"
    const val Register = "register"
    const val HomeGate = "home_gate"
    const val FirstHabitTeaser = "first_habit_teaser"
    const val HabitSelection = "habit_selection"
    const val HabitConfig = "habit_config"
    const val CustomHabit = "custom_habit"
    const val DesignSystem = "design_system" // placeholder de "Home" hasta que exista la pantalla real
}

@Composable
fun VittaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = VittaRoutes.Onboarding
) {
    val context = LocalContext.current
    var pendingHabits by remember { mutableStateOf<List<PredefinedHabit>>(emptyList()) }

    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        composable(VittaRoutes.Onboarding) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(VittaRoutes.Login) {
                        popUpTo(VittaRoutes.Onboarding) { inclusive = true }
                    }
                }
            )
        }

        composable(VittaRoutes.Login) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(VittaRoutes.HomeGate) {
                        popUpTo(VittaRoutes.Login) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(VittaRoutes.Register) }
            )
        }

        composable(VittaRoutes.Register) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(VittaRoutes.HomeGate) {
                        popUpTo(VittaRoutes.Login) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // Consulta si el usuario ya tiene hábitos y decide a dónde mandarlo.
        // Con hábitos → placeholder de Home. Sin hábitos → pantalla "primer día".
        composable(VittaRoutes.HomeGate) {
            val repository = remember { HabitRepository() }
            LaunchedEffect(Unit) {
                val destination = when (val result = repository.listHabits()) {
                    is HabitsResult.Success ->
                        if (result.habits.isEmpty()) VittaRoutes.FirstHabitTeaser else VittaRoutes.DesignSystem
                    is HabitsResult.Error -> VittaRoutes.FirstHabitTeaser
                }
                navController.navigate(destination) {
                    popUpTo(VittaRoutes.HomeGate) { inclusive = true }
                }
            }
            CircularProgressIndicator()
        }

        composable(VittaRoutes.FirstHabitTeaser) {
            var userName by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                userName = TokenManager(context).nombreFlow.first() ?: ""
            }
            FirstHabitTeaserScreen(
                userName = userName,
                onChooseFirstHabit = { navController.navigate(VittaRoutes.HabitSelection) }
            )
        }

        composable(VittaRoutes.HabitSelection) {
            HabitSelectionScreen(
                onContinue = { habits ->
                    pendingHabits = habits
                    navController.navigate(VittaRoutes.HabitConfig)
                },
                onCreateCustom = { navController.navigate(VittaRoutes.CustomHabit) }
            )
        }

        composable(VittaRoutes.HabitConfig) {
            HabitConfigScreen(
                selectedHabits = pendingHabits,
                onAllSaved = {
                    navController.navigate(VittaRoutes.HomeGate) { popUpTo(0) }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(VittaRoutes.CustomHabit) {
            CustomHabitScreen(
                onSaved = {
                    navController.navigate(VittaRoutes.HomeGate) { popUpTo(0) }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(VittaRoutes.HabitSelection) {
            HabitSelectionScreen(
                onContinue = { habits ->
                    pendingHabits = habits
                    navController.navigate(VittaRoutes.HabitConfig)
                },
                onCreateCustom = { alreadySelected ->
                    pendingHabits = alreadySelected
                    navController.navigate(VittaRoutes.CustomHabit)
                }
            )
        }

        composable(VittaRoutes.CustomHabit) {
            CustomHabitScreen(
                onSaved = {
                    // Si quedaban predefinidos ya marcados antes de crear el
                    // personalizado, ahora sí les preguntamos meta/frecuencia.
                    if (pendingHabits.isNotEmpty()) {
                        navController.navigate(VittaRoutes.HabitConfig)
                    } else {
                        navController.navigate(VittaRoutes.HomeGate) { popUpTo(0) }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(VittaRoutes.DesignSystem) {
            var userName by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                userName = com.vitta.app.data.local.TokenManager(context).nombreFlow.first() ?: ""
            }
            com.vitta.app.ui.screens.home.ChecklistScreen(userName = userName)
        }
    }
}