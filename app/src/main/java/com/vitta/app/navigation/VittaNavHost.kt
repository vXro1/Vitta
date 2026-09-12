package com.vitta.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vitta.app.ui.screens.design.DesignSystemScreen

/**
 * Route names for the app. Only [DesignSystem] is wired to a real screen
 * right now — the rest of Vitta's screens (Onboarding, Home, Habits, etc.)
 * get added here as they're built, each as its own `composable(route) { }`
 * entry, reusing the components from `ui/components/`.
 */
object VittaRoutes {
    const val DesignSystem = "design_system"
}

/**
 * App-wide navigation graph. Currently a single destination (the design
 * system catalog) so the project has a real, working NavHost to extend
 * instead of a placeholder that gets thrown away later.
 */
@Composable
fun VittaNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = VittaRoutes.DesignSystem
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(VittaRoutes.DesignSystem) {
            DesignSystemScreen()
        }
    }
}
