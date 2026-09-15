package com.vitta.app.data.mock

import com.vitta.app.ui.screens.habits.HabitIcon
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.progress.Insignia

/**
 * Local, in-memory sample data for previewing the design system and, later,
 * wiring up screens before any real backend exists. Nothing here is
 * persisted — see the class docs in each screen for how it's used.
 */

data class MockHabit(
    val icon: HabitIcon,
    val title: String,
    val subtitle: String,
    val done: Boolean = false
)

val mockTodayHabits = listOf(
    MockHabit(HabitIcon.Water, "Tomar agua", "Vas 3 de 8 vasos"),
    MockHabit(HabitIcon.Yoga, "Hacer yoga", "10 minutos · 07:00"),
    MockHabit(HabitIcon.Read, "Leer", "15 de 20 minutos"),
    MockHabit(HabitIcon.Walk, "Caminar", "0 de 20 minutos")
)

data class MockReward(
    val area: String,
    val title: String,
    val cost: Int,
    val unlocked: Boolean,
    val mascot: MascotState
)

val mockRewards = listOf(
    MockReward("BIENESTAR", "Rutina de yoga en 5 minutos", cost = 80, unlocked = true, mascot = MascotState.Yoga),
    MockReward("HÁBITOS", "Diez desayunos sencillos", cost = 60, unlocked = true, mascot = MascotState.GoodHabits),
    MockReward("MOVIMIENTO", "Caminatas de 20 minutos", cost = 80, unlocked = false, mascot = MascotState.Strong)
)

data class MockStreakInsignia(
    val insignia: Insignia,
    val name: String,
    val requirement: String,
    val unlocked: Boolean
)

val mockStreakInsignias = listOf(
    MockStreakInsignia(Insignia.Days7, "Primer Impulso", "7 días seguidos", unlocked = true),
    MockStreakInsignia(Insignia.Days30, "Constancia Vita", "30 días seguidos", unlocked = true),
    MockStreakInsignia(Insignia.Days50, "Ritmo Vita", "50 días seguidos", unlocked = false),
    MockStreakInsignia(Insignia.Days100, "Espíritu Vita", "100 días seguidos", unlocked = false)
)

val mockWeek = listOf(0.5f, 0.75f, 1f, 0.6f, 0.4f, 0.8f, 0.3f)
val mockWeekLabels = listOf("L", "M", "X", "J", "V", "S", "D")

/** Mirrors the mockup's `MAPA` array: 5 weeks × 7 days, each 0..3. */
val mockMonthHeatmap = listOf(
    2, 3, 1, 3, 3, 2, 0, 3, 3, 2, 3, 1, 3, 2,
    3, 2, 3, 3, 3, 1, 2, 3, 3, 3, 2, 3, 3, 3,
    3, 2, 3, 1, 3, 3, 0
)

val mockStoreFilters = listOf("Todo", "Bienestar", "Lectura", "Movimiento")

data class MockUnlockedReward(
    val mascot: MascotState,
    val area: String,
    val name: String,
    val meta: String
)

val mockUnlockedRewards = listOf(
    MockUnlockedReward(MascotState.Yoga, "BIENESTAR", "Rutina de yoga en 5 minutos", "4 piezas · Abrir")
)

data class MockStoreItem(
    val mascot: MascotState,
    val area: String,
    val name: String,
    val description: String
)

val mockStoreItems = listOf(
    MockStoreItem(MascotState.Strong, "MOVIMIENTO", "Caminatas de 20 minutos", "Seis recorridos con ritmo sugerido."),
    MockStoreItem(MascotState.Reader, "LECTURA", "Diez cuentos cortos", "Historias de cinco minutos para antes de dormir.")
)
