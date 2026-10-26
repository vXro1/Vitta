package com.vitta.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo

/** Pestaña Logros: catálogo real de `GET /api/logros`, con los obtenidos resaltados. */
@Composable
fun AchievementsScreen(viewModel: ProfileViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarLogros() }

    Box(modifier.fillMaxSize().background(Mk.Screen), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier
                .widthIn(max = 640.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
        ) {
            Text(
                "Tus logros",
                style = caprasimo(28f, 1.1f, -0.02f),
                color = Mk.Ink,
                modifier = Modifier.padding(bottom = 12.dp).semantics { heading() }
            )
            LogrosSection(state, onRetry = viewModel::cargarLogros)
        }
    }
}
