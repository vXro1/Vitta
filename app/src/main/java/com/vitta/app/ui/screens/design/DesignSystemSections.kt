package com.vitta.app.ui.screens.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vitta.app.data.mock.mockMonthHeatmap
import com.vitta.app.data.mock.mockRewards
import com.vitta.app.data.mock.mockStoreFilters
import com.vitta.app.data.mock.mockStoreItems
import com.vitta.app.data.mock.mockStreakInsignias
import com.vitta.app.data.mock.mockTodayHabits
import com.vitta.app.data.mock.mockUnlockedRewards
import com.vitta.app.data.mock.mockWeek
import com.vitta.app.data.mock.mockWeekLabels
import com.vitta.app.ui.components.brand.VittaBrandLockup
import com.vitta.app.ui.components.brand.VittaLogo
import com.vitta.app.ui.components.brand.VittaWordmark
import com.vitta.app.ui.components.buttons.VittaOutlinedButton
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.buttons.VittaSecondaryButton
import com.vitta.app.ui.components.buttons.VittaTextButton
import com.vitta.app.ui.components.cards.VittaBalanceCard
import com.vitta.app.ui.components.cards.VittaCenteredInfoCard
import com.vitta.app.ui.components.cards.VittaEmptyStateCard
import com.vitta.app.ui.components.cards.VittaHighlightCard
import com.vitta.app.ui.components.cards.VittaProgressCard
import com.vitta.app.ui.components.cards.VittaStatTile
import com.vitta.app.ui.components.feedback.VittaConfirmDialog
import com.vitta.app.ui.components.feedback.VittaRewardSheet
import com.vitta.app.ui.components.feedback.VittaToast
import com.vitta.app.ui.components.forms.VittaDaySelector
import com.vitta.app.ui.components.forms.VittaSwitchRow
import com.vitta.app.ui.components.forms.VittaTextField
import com.vitta.app.ui.screens.habits.VittaHabitDoneIndicator
import com.vitta.app.ui.screens.habits.VittaHabitRow
import com.vitta.app.ui.screens.habits.VittaWaterTracker
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.components.navigation.VittaBottomNavBar
import com.vitta.app.ui.components.navigation.VittaDestination
import com.vitta.app.ui.components.navigation.VittaSegmentedControl
import com.vitta.app.ui.components.progress.VittaCalendarHeatmap
import com.vitta.app.ui.components.progress.VittaInsigniaBadge
import com.vitta.app.ui.components.progress.VittaLevelProgressBar
import com.vitta.app.ui.components.progress.VittaPointsBadge
import com.vitta.app.ui.components.store.StoreItemState
import com.vitta.app.ui.components.store.VittaFilterChipsRow
import com.vitta.app.ui.components.store.VittaRewardListItem
import com.vitta.app.ui.components.store.VittaStoreItemCard
import com.vitta.app.ui.components.progress.VittaStreakBadge
import com.vitta.app.ui.components.progress.VittaWeeklyBar
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles

// ---------------------------------------------------------------------------
// Brand
// ---------------------------------------------------------------------------

@Composable
internal fun BrandSection() {
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Lg)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Lg)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VittaLogo(size = 56.dp)
                Text("Logo", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VittaWordmark(height = 40.dp)
                Text("Wordmark", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            }
        }
        VittaBrandLockup(logoSize = 40.dp, wordmarkHeight = 30.dp)
    }
}

// ---------------------------------------------------------------------------
// Colors
// ---------------------------------------------------------------------------

private data class SwatchGroup(val title: String, val swatches: List<Pair<String, Color>>)

@Composable
internal fun ColorsSection() {
    val groups = listOf(
        SwatchGroup(
            "Marca",
            listOf(
                "GreenPrimary" to VittaColors.GreenPrimary,
                "GreenHover" to VittaColors.GreenHover,
                "GreenPressed" to VittaColors.GreenPressed,
                "OrangeAccent" to VittaColors.OrangeAccent,
                "GoldAccent" to VittaColors.GoldAccent
            )
        ),
        SwatchGroup(
            "Superficies",
            listOf(
                "Background" to VittaColors.Background,
                "Surface" to VittaColors.Surface,
                "SurfaceVariant" to VittaColors.SurfaceVariant,
                "SurfaceMuted" to VittaColors.SurfaceMuted
            )
        ),
        SwatchGroup(
            "Texto",
            listOf(
                "TextPrimary" to VittaColors.TextPrimary,
                "TextSecondary" to VittaColors.TextSecondary,
                "TextMuted" to VittaColors.TextMuted,
                "TextFaint" to VittaColors.TextFaint
            )
        )
    )
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
        groups.forEach { group ->
            Text(group.title, style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
                items(group.swatches) { (name, color) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(color, VittaShapes.medium)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(name, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Typography
// ---------------------------------------------------------------------------

@Composable
internal fun TypographySection() {
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
        Text("Vitta te está esperando", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        Text("Diez desayunos sencillos", style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
        Text("Tomar agua", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
        Text(
            "Cuatro hábitos listos para registrar. Empieza por el más fácil.",
            style = VittaTextStyles.body,
            color = VittaColorRoles.textSecondary
        )
        Text("Vas 3 de 8 vasos", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        Text("TU SALDO", style = VittaTextStyles.label, color = VittaColorRoles.textDisabled)
        Text("4 piezas · Abrir", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
    }
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------

@Composable
internal fun ButtonsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
        VittaPrimaryButton(text = "Guardar hábito", onClick = {})
        VittaSecondaryButton(text = "Ver mis logros", onClick = {})
        VittaOutlinedButton(text = "Omitir", onClick = {})
        VittaTextButton(text = "Cancelar", onClick = {})
        Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
            VittaPrimaryButton(text = "Cargando", onClick = {}, loading = true, fillWidth = false)
            VittaPrimaryButton(text = "Bloqueado", onClick = {}, enabled = false, fillWidth = false)
            VittaPrimaryButton(text = "Con ícono", onClick = {}, icon = Icons.Filled.Favorite, fillWidth = false)
        }
    }
}

// ---------------------------------------------------------------------------
// Cards
// ---------------------------------------------------------------------------

@Composable
internal fun CardsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
        VittaProgressCard(
            eyebrow = "TU DÍA EMPIEZA AQUÍ",
            title = "Vitta te está esperando",
            description = "Cuatro hábitos listos para registrar. Empieza por el más fácil.",
            illustration = { VittaMascot(state = MascotState.Strong, size = MascotSize.Small) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
            VittaStatTile(
                icon = { VittaStreakBadgeIcon() },
                value = "12",
                label = "días de racha",
                modifier = Modifier.weight(1f)
            )
            VittaStatTile(
                icon = { VittaPointsBadgeIcon() },
                value = "1.250",
                label = "VitaPuntos",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VittaStreakBadgeIcon() {
    Text("🔥", style = VittaTextStyles.title)
}

@Composable
private fun VittaPointsBadgeIcon() {
    Box(modifier = Modifier.size(24.dp).background(VittaColors.GoldAccent, androidx.compose.foundation.shape.CircleShape))
}

// ---------------------------------------------------------------------------
// Habits
// ---------------------------------------------------------------------------

@Composable
internal fun HabitsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
        mockTodayHabits.forEach { habit ->
            VittaHabitRow(
                icon = habit.icon,
                title = habit.title,
                subtitle = habit.subtitle,
                trailing = { if (habit.done) VittaHabitDoneIndicator() }
            )
        }
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text("Tracker de agua", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        VittaWaterTracker(filled = 3, total = 8)
    }
}

// ---------------------------------------------------------------------------
// Progress & achievements
// ---------------------------------------------------------------------------

@Composable
internal fun ProgressSection() {
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
            VittaStreakBadge(days = 12)
            VittaPointsBadge(points = 1250)
        }
        Column {
            Text("Impulsor", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            Spacer(Modifier.height(4.dp))
            VittaLevelProgressBar(progress = 0.4f)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Xs)
        ) {
            mockWeek.forEachIndexed { i, v ->
                VittaWeeklyBar(
                    value = v,
                    label = mockWeekLabels[i],
                    isToday = i == 2,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
            mockStreakInsignias.forEach { item ->
                VittaInsigniaBadge(insignia = item.insignia, unlocked = item.unlocked)
            }
        }
        var rangeIndex by remember { mutableStateOf(0) }
        VittaSegmentedControl(
            options = listOf("Semana", "Mes"),
            selectedIndex = rangeIndex,
            onSelect = { rangeIndex = it }
        )
        VittaCalendarHeatmap(intensities = mockMonthHeatmap)
        VittaHighlightCard(
            label = "ÚLTIMA MEDALLA",
            title = "Constancia Vita",
            description = "30 días seguidos · +100 VitaPuntos",
            trailing = { VittaMascot(state = MascotState.Peek, size = MascotSize.Small) }
        )
        VittaCenteredInfoCard(
            eyebrow = "LA MÁS CERCANA",
            title = "Primer Brote",
            description = "Completa cualquier hábito, una sola vez, y esta medalla es tuya.",
            illustration = { VittaMascot(state = MascotState.EmptyState, size = MascotSize.Small) }
        )
    }
}

// ---------------------------------------------------------------------------
// Vitta Store
// ---------------------------------------------------------------------------

@Composable
internal fun StoreSection() {
    var filter by remember { mutableStateOf(mockStoreFilters.first()) }
    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
        VittaBalanceCard(points = 1250)
        Text("Mis recompensas", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        mockUnlockedRewards.forEach { reward ->
            VittaRewardListItem(
                mascot = reward.mascot,
                area = reward.area,
                name = reward.name,
                meta = reward.meta,
                onClick = {}
            )
        }
        VittaEmptyStateCard(message = "Todavía no has canjeado nada. Lo que desbloquees aparecerá aquí.")
        Text("Para desbloquear", style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
        VittaFilterChipsRow(options = mockStoreFilters, selected = filter, onSelect = { filter = it })
        VittaStoreItemCard(
            mascot = mockStoreItems[0].mascot,
            area = mockStoreItems[0].area,
            name = mockStoreItems[0].name,
            description = mockStoreItems[0].description,
            state = StoreItemState.MissingPoints("Faltan 40 VitaPuntos")
        )
        VittaStoreItemCard(
            mascot = mockStoreItems[1].mascot,
            area = mockStoreItems[1].area,
            name = mockStoreItems[1].name,
            description = mockStoreItems[1].description,
            state = StoreItemState.Locked("Completa 3 sesiones de lectura")
        )
        VittaStoreItemCard(
            mascot = MascotState.Wave,
            area = "BIENESTAR",
            name = "Meditación guiada de 10 minutos",
            description = "Una pausa corta para respirar y volver al día.",
            state = StoreItemState.Unlockable,
            onUnlock = {}
        )
    }
}

// ---------------------------------------------------------------------------
// Mascot
// ---------------------------------------------------------------------------

@Composable
internal fun MascotSection() {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
        items(MascotState.entries) { state ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                VittaMascot(state = state, size = MascotSize.Medium)
                Text(state.name, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Forms
// ---------------------------------------------------------------------------

@Composable
internal fun FormsSection() {
    var name by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf(0, 2, 4)) }
    var reminderOn by remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
        VittaTextField(value = name, onValueChange = { name = it }, label = "Nombre del hábito")
        VittaDaySelector(
            selectedDays = selectedDays,
            onDayToggle = { day ->
                selectedDays = if (day in selectedDays) selectedDays - day else selectedDays + day
            }
        )
        VittaSwitchRow(
            title = "Recordatorio",
            description = "Vitta te avisa a esta hora",
            checked = reminderOn,
            onCheckedChange = { reminderOn = it }
        )
    }
}

// ---------------------------------------------------------------------------
// Navigation
// ---------------------------------------------------------------------------

@Composable
internal fun NavigationSection() {
    var selected by remember { mutableStateOf(VittaDestination.Home) }
    Box(modifier = Modifier.background(VittaColorRoles.surface, VittaShapes.large)) {
        VittaBottomNavBar(selected = selected, onSelect = { selected = it })
    }
}

// ---------------------------------------------------------------------------
// Feedback (toast / dialog / reward sheet)
// ---------------------------------------------------------------------------

@Composable
internal fun FeedbackSection() {
    var showToast by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showReward by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
        VittaOutlinedButton(text = "Mostrar toast", onClick = { showToast = true }, fillWidth = false)
        VittaOutlinedButton(text = "Mostrar diálogo de confirmación", onClick = { showDialog = true }, fillWidth = false)
        VittaOutlinedButton(text = "Mostrar hoja de recompensa", onClick = { showReward = true }, fillWidth = false)
    }

    // Each overlay lives inside its own Box so its scrim/positioning is
    // scoped to this section only — the same pattern a real screen follows.
    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        VittaToast(
            visible = showToast,
            points = 10,
            title = "Hábito completado",
            description = "Racha de 7 días"
        )
    }

    val firstReward = mockRewards.first()
    VittaConfirmDialogHost(visible = showDialog, onDismiss = { showDialog = false })
    VittaRewardSheetHost(visible = showReward, onDismiss = { showReward = false }, reward = firstReward)
}

@Composable
private fun VittaConfirmDialogHost(visible: Boolean, onDismiss: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(if (visible) 360.dp else 0.dp)) {
        VittaConfirmDialog(
            visible = visible,
            area = "BIENESTAR",
            name = "Rutina de yoga en 5 minutos",
            description = "Cuatro secuencias cortas para las mañanas con prisa.",
            cost = "80 VitaPuntos",
            balance = "Saldo: 1.250",
            onCancel = onDismiss,
            onConfirm = onDismiss
        )
    }
}

@Composable
private fun VittaRewardSheetHost(
    visible: Boolean,
    onDismiss: () -> Unit,
    reward: com.vitta.app.data.mock.MockReward
) {
    Box(modifier = Modifier.fillMaxWidth().height(if (visible) 420.dp else 0.dp)) {
        VittaRewardSheet(
            visible = visible,
            area = reward.area,
            title = reward.title,
            description = "Contenido desbloqueado con tus VitaPuntos.",
            onDismiss = onDismiss,
            mascot = { VittaMascot(state = reward.mascot, size = MascotSize.Small) }
        ) {
            repeat(3) { index ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VittaColorRoles.surface, VittaShapes.large)
                        .padding(VittaSpacing.Md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                    Spacer(Modifier.width(VittaSpacing.Sm))
                    Text("Sesión ${index + 1}", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
                }
            }
        }
    }
}
