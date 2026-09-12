package com.vitta.app.ui.screens.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.brand.VittaBrandLockup
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * Internal catalog of every piece of the Vitta design system — colors,
 * type, buttons, cards, habit/progress/mascot components, forms and
 * feedback overlays — all in one scrollable screen.
 *
 * This is **not** a real app screen. It exists so the whole visual system
 * can be checked at a glance while building it, and as a living reference
 * once real screens (Home, Onboarding, etc.) are built on top of these same
 * components.
 */
private enum class DesignSystemSection(val title: String) {
    Brand("Marca"),
    Colors("Colores"),
    Typography("Tipografía"),
    Buttons("Botones"),
    Cards("Cards"),
    Habits("Hábitos"),
    Progress("Progreso y logros"),
    Store("Vitta Store"),
    Mascot("Mascota"),
    Forms("Formularios"),
    Navigation("Navegación"),
    Feedback("Modales y notificaciones")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesignSystemScreen(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = VittaColorRoles.background,
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        VittaBrandLockup(logoSize = 26.dp, wordmarkHeight = 20.dp)
                        androidx.compose.foundation.layout.Spacer(Modifier.width(VittaSpacing.Sm))
                        Text("· Design System", style = VittaTextStyles.title)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = VittaColorRoles.background,
                    titleContentColor = VittaColorRoles.textPrimary
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(VittaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Xxl)
        ) {
            items(DesignSystemSection.entries) { section ->
                DesignSystemSectionContainer(title = section.title) {
                    when (section) {
                        DesignSystemSection.Brand -> BrandSection()
                        DesignSystemSection.Colors -> ColorsSection()
                        DesignSystemSection.Typography -> TypographySection()
                        DesignSystemSection.Buttons -> ButtonsSection()
                        DesignSystemSection.Cards -> CardsSection()
                        DesignSystemSection.Habits -> HabitsSection()
                        DesignSystemSection.Progress -> ProgressSection()
                        DesignSystemSection.Store -> StoreSection()
                        DesignSystemSection.Mascot -> MascotSection()
                        DesignSystemSection.Forms -> FormsSection()
                        DesignSystemSection.Navigation -> NavigationSection()
                        DesignSystemSection.Feedback -> FeedbackSection()
                    }
                }
            }
        }
    }
}

@Composable
private fun DesignSystemSectionContainer(
    title: String,
    content: @Composable () -> Unit
) {
    Box {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
        ) {
            Text(title, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DesignSystemScreenPreview() {
    VittaTheme {
        DesignSystemScreen()
    }
}
