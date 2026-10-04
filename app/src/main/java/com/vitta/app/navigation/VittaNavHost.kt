package com.vitta.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vitta.app.data.local.TokenManager
import com.vitta.app.data.mock.PredefinedHabit
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.ui.screens.auth.LoginScreen
import com.vitta.app.ui.screens.auth.RegisterScreen
import com.vitta.app.ui.screens.habits.CustomHabitScreen
import com.vitta.app.ui.screens.habits.EditHabitScreen
import com.vitta.app.ui.screens.habits.HabitConfigScreen
import com.vitta.app.ui.screens.habits.HabitSelectionScreen
import com.vitta.app.ui.screens.home.FirstHabitTeaserScreen
import com.vitta.app.ui.screens.home.MainScreen
import com.vitta.app.ui.screens.onboarding.OnboardingScreen
import com.vitta.app.ui.theme.VittaColorRoles
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object VittaRoutes {
    const val Onboarding = "onboarding"
    const val Login = "login"
    const val Register = "register"
    const val HomeGate = "home_gate"
    const val FirstHabitTeaser = "first_habit_teaser"
    const val HabitSelection = "habit_selection"
    const val HabitConfig = "habit_config"
    const val CustomHabit = "custom_habit"
    const val EditHabit = "edit_habit/{habitId}"
    const val Home = "home" // Inicio + Yo (antes: placeholder "design_system")

    fun editHabit(habitId: Int) = "edit_habit/$habitId"
}

/** Clave en el SavedStateHandle de Home para pedir que recargue al volver. */
private const val RefreshHomeKey = "refresh_home"

@Composable
fun VittaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = VittaRoutes.Onboarding
) {
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val scope = rememberCoroutineScope()
    var pendingHabits by remember { mutableStateOf<List<PredefinedHabit>>(emptyList()) }

    // Tras crear hábitos se vuelve a Home limpiando la pila (no se puede "volver" al formulario ya guardado).
    val goHomeAfterSaving = {
        pendingHabits = emptyList()
        navController.navigate(VittaRoutes.HomeGate) { popUpTo(0) }
    }

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
                onNavigateToRegister = { navController.navigate(VittaRoutes.Register) },
                // El onboarding se saca de la pila al terminarlo, así que el
                // regreso desde Login lo vuelve a abrir en vez de cerrar la app.
                onBack = {
                    navController.navigate(VittaRoutes.Onboarding) {
                        popUpTo(VittaRoutes.Login) { inclusive = true }
                    }
                }
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
        // Con hábitos → Inicio. Sin hábitos → pantalla "primer día".
        composable(VittaRoutes.HomeGate) {
            val repository = remember { HabitRepository() }
            LaunchedEffect(Unit) {
                val destination = when (val result = repository.listHabits()) {
                    is HabitsResult.Success ->
                        if (result.habits.isEmpty()) VittaRoutes.FirstHabitTeaser else VittaRoutes.Home
                    is HabitsResult.Error -> VittaRoutes.FirstHabitTeaser
                }
                navController.navigate(destination) {
                    popUpTo(VittaRoutes.HomeGate) { inclusive = true }
                }
            }
            Box(Modifier.fillMaxSize().background(VittaColorRoles.background), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VittaColorRoles.primary)
            }
        }

        composable(VittaRoutes.FirstHabitTeaser) {
            var userName by remember { mutableStateOf("") }
            LaunchedEffect(Unit) {
                userName = tokenManager.nombreFlow.first() ?: ""
            }
            FirstHabitTeaserScreen(
                userName = userName,
                onChooseFirstHabit = { navController.navigate(VittaRoutes.HabitSelection) }
            )
        }

        // Nota: antes HabitSelection y CustomHabit estaban registrados dos
        // veces con callbacks distintos (Navigation usaba solo el último).
        // Se dejó una sola definición, con el comportamiento que estaba activo.
        composable(VittaRoutes.HabitSelection) {
            HabitSelectionScreen(
                onContinue = { habits ->
                    pendingHabits = habits
                    navController.navigate(VittaRoutes.HabitConfig)
                },
                onCreateCustom = { alreadySelected ->
                    pendingHabits = alreadySelected
                    navController.navigate(VittaRoutes.CustomHabit)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(VittaRoutes.HabitConfig) {
            HabitConfigScreen(
                selectedHabits = pendingHabits,
                onAllSaved = goHomeAfterSaving,
                onBack = { navController.popBackStack() }
            )
        }

        composable(VittaRoutes.CustomHabit) {
            CustomHabitScreen(
                onSaved = {
                    // Si quedaban predefinidos ya marcados antes de crear el
                    // personalizado, ahora sí les preguntamos meta/frecuencia.
                    if (pendingHabits.isNotEmpty()) {
                        navController.navigate(VittaRoutes.HabitConfig) {
                            // El personalizado ya está guardado: no se puede volver a él.
                            popUpTo(VittaRoutes.CustomHabit) { inclusive = true }
                        }
                    } else {
                        goHomeAfterSaving()
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = VittaRoutes.EditHabit,
            arguments = listOf(navArgument("habitId") { type = NavType.IntType })
        ) { entry ->
            val habitId = entry.arguments?.getInt("habitId") ?: return@composable
            EditHabitScreen(
                habitId = habitId,
                onSaved = {
                    navController.previousBackStackEntry?.savedStateHandle?.let { handle ->
                        handle[RefreshHomeKey] = (handle.get<Int>(RefreshHomeKey) ?: 0) + 1
                    }
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(VittaRoutes.Home) { entry ->
            var userName by remember { mutableStateOf("") }
            var userEmail by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(Unit) {
                userName = tokenManager.nombreFlow.first() ?: ""
                userEmail = tokenManager.correoFlow.first()
            }
            val refresh by entry.savedStateHandle.getStateFlow(RefreshHomeKey, 0).collectAsState()

            MainScreen(
                userName = userName,
                userEmail = userEmail,
                refreshRequests = refresh,
                onAddHabit = { navController.navigate(VittaRoutes.HabitSelection) },
                onEditHabit = { id -> navController.navigate(VittaRoutes.editHabit(id)) },
                onLogout = {
                    scope.launch {
                        tokenManager.clearToken()
                        navController.navigate(VittaRoutes.Login) { popUpTo(0) }
                    }
                }
            )
        }
    }
}
