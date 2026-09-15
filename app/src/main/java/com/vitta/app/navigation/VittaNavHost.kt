package com.vitta.app.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.ui.screens.auth.LoginScreen
import com.vitta.app.ui.screens.auth.RegisterScreen
import com.vitta.app.ui.screens.habits.HabitConfigScreen
import com.vitta.app.ui.screens.habits.HabitSelectionScreen
import com.vitta.app.ui.screens.home.FirstHabitTeaserScreen
import com.vitta.app.ui.screens.onboarding.OnboardingScreen

object VittaRoutes {
    const val Onboarding = "onboarding"
    const val Login = "login"
    const val Register = "register"
    const val HomeGate = "home_gate"       // decide si mostrar el teaser o ya hay hábitos
    const val FirstHabitTeaser = "first_habit_teaser"
    const val HabitSelection = "habit_selection"
    const val HabitConfig = "habit_config"
}

@Composable
fun VittaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = VittaRoutes.Onboarding
) {
    // Guarda temporalmente los hábitos elegidos entre HabitSelection y
    // HabitConfig — más adelante, cuando exista Home real, esto se puede
    // mover a un ViewModel compartido con scope de navegación.
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

        // Pantalla "invisible": consulta si el usuario ya tiene hábitos y
        // decide a cuál pantalla mandarlo. No dibuja nada propio.
        composable(VittaRoutes.HomeGate) {
            val repository = remember { com.vitta.app.data.repository.HabitRepository() }
            LaunchedEffect(Unit) {
                when (val result = repository.listHabits()) {
                    is HabitsResult.Success -> {
                        val destination = if (result.habits.isEmpty()) {
                            VittaRoutes.FirstHabitTeaser
                        } else {
                            VittaRoutes.FirstHabitTeaser // TODO: cambiar por Home real cuando exista
                        }
                        navController.navigate(destination) {
                            popUpTo(VittaRoutes.HomeGate) { inclusive = true }
                        }
                    }
                    is HabitsResult.Error -> {
                        // Si falla la consulta (ej. sin internet), no dejamos
                        // a la usuaria atascada: la mandamos igual al teaser.
                        navController.navigate(VittaRoutes.FirstHabitTeaser) {
                            popUpTo(VittaRoutes.HomeGate) { inclusive = true }
                        }
                    }
                }
            }
            androidx.compose.material3.CircularProgressIndicator()
        }

        composable(VittaRoutes.FirstHabitTeaser) {
            FirstHabitTeaserScreen(
                userName = "", // TODO: pásale el nombre real cuando lo tengamos guardado localmente
                onChooseFirstHabit = { navController.navigate(VittaRoutes.HabitSelection) }
            )
        }

        composable(VittaRoutes.HabitSelection) {
            HabitSelectionScreen(
                onContinue = { habits ->
                    pendingHabits = habits
                    navController.navigate(VittaRoutes.HabitConfig)
                },
                onCreateCustom = { /* TODO: pantalla de hábito propio, la armamos después */ }
            )
        }

        composable(VittaRoutes.HabitConfig) {
            HabitConfigScreen(
                selectedHabits = pendingHabits,
                onAllSaved = {
                    // TODO: navegar al Home real cuando exista. Por ahora
                    // volvemos al teaser, que ya no debería reaparecer
                    // porque listHabits() ya no estará vacío.
                    navController.navigate(VittaRoutes.HomeGate) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}