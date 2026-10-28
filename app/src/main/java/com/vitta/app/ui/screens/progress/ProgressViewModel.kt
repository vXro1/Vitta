package com.vitta.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.progress.ProgressCalculator
import com.vitta.app.data.progress.ProgressHistory
import com.vitta.app.data.progress.ProgressPeriod
import com.vitta.app.data.progress.ProgressSource
import com.vitta.app.data.progress.ProgressSourceResult
import com.vitta.app.data.progress.ProgressSources
import com.vitta.app.data.progress.ProgressSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

data class ProgressUiState(
    val period: ProgressPeriod = ProgressPeriod.Week,
    val isLoading: Boolean = true,
    val error: String? = null,
    val summary: ProgressSummary? = null
)

/**
 * Carga el historial una vez desde [source] y lo resume para el periodo
 * elegido. Cambiar Semana/Mes no vuelve a pedir datos: solo recalcula.
 */
class ProgressViewModel(
    private val source: ProgressSource = ProgressSources.default(),
    // Día local: el backend asigna cada registro al día de la zona que envía la app (X-Timezone).
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.systemDefault()) }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private var history: ProgressHistory? = null

    /** Se llama al abrir la pestaña, para reflejar los hábitos marcados en Inicio. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = source.loadHistory()) {
                is ProgressSourceResult.Success -> {
                    history = result.history
                    recompute()
                }
                is ProgressSourceResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun selectPeriod(period: ProgressPeriod) {
        if (period == _uiState.value.period) return
        _uiState.update { it.copy(period = period) }
        viewModelScope.launch { recompute() }
    }

    private suspend fun recompute() {
        val current = history ?: return
        val period = _uiState.value.period
        val summary = withContext(Dispatchers.Default) { ProgressCalculator.summarize(current, period, today()) }
        _uiState.update { it.copy(isLoading = false, error = null, summary = summary) }
    }
}
