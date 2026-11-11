package com.vitta.app.ui.screens.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.progress.DayTotals
import com.vitta.app.data.progress.HeatCell
import com.vitta.app.data.progress.ProgressCalculator
import com.vitta.app.data.progress.ProgressHistory
import com.vitta.app.data.progress.ProgressPeriod
import com.vitta.app.data.progress.ProgressSource
import com.vitta.app.data.progress.ProgressSourceResult
import com.vitta.app.data.progress.ProgressSources
import com.vitta.app.data.progress.ProgressSummary
import com.vitta.app.data.repository.DailyProgressResult
import com.vitta.app.data.repository.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class ProgressUiState(
    val period: ProgressPeriod = ProgressPeriod.Week,
    val isLoading: Boolean = true,
    val error: String? = null,
    val summary: ProgressSummary? = null,
    /** HAL-17: mes que muestra el calendario (por defecto el actual). */
    val calendarMonth: YearMonth = YearMonth.now(ZoneId.systemDefault()),
    val calendarCells: List<HeatCell?> = emptyList(),
    val calendarLoading: Boolean = false,
    val calendarError: String? = null,
    /** Límites de navegación: desde el primer mes con hábitos hasta el mes actual. */
    val minMonth: YearMonth = YearMonth.now(ZoneId.systemDefault()),
    val maxMonth: YearMonth = YearMonth.now(ZoneId.systemDefault())
)

/**
 * Carga el historial una vez desde [source] y lo resume para el periodo
 * elegido. Cambiar Semana/Mes no vuelve a pedir datos: solo recalcula.
 * El calendario (HAL-17) pide los totales del mes elegido a
 * `GET /api/progress?desde&hasta` y los combina con el historial por hábito.
 */
class ProgressViewModel(
    private val source: ProgressSource = ProgressSources.default(),
    private val repository: ProgressRepository = ProgressRepository(),
    // Día local: el backend asigna cada registro al día de la zona que envía la app (X-Timezone).
    private val today: () -> LocalDate = { LocalDate.now(ZoneId.systemDefault()) }
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private var history: ProgressHistory? = null

    /** Meses ya pedidos al backend en esta carga (no se piden dos veces). */
    private val mesesCargados = mutableSetOf<YearMonth>()

    /** Se llama al abrir la pestaña, para reflejar los hábitos marcados en Inicio. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = source.loadHistory()) {
                is ProgressSourceResult.Success -> {
                    history = result.history
                    mesesCargados.clear()
                    val hoy = today()
                    val actual = YearMonth.from(hoy)
                    // El rango inicial de la fuente ya cubre el mes actual y el anterior.
                    mesesCargados += actual
                    mesesCargados += actual.minusMonths(1)
                    _uiState.update {
                        it.copy(
                            minMonth = minOf(ProgressCalculator.firstMonth(result.history, hoy), actual),
                            maxMonth = actual,
                            calendarMonth = it.calendarMonth.coerceIn(minOf(ProgressCalculator.firstMonth(result.history, hoy), actual), actual)
                        )
                    }
                    recompute()
                    loadMonth(_uiState.value.calendarMonth)
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

    /** Flechas y selector de mes/año del calendario. */
    fun selectMonth(month: YearMonth) {
        val s = _uiState.value
        val m = month.coerceIn(s.minMonth, s.maxMonth)
        if (m == s.calendarMonth && s.calendarCells.isNotEmpty()) return
        _uiState.update { it.copy(calendarMonth = m) }
        viewModelScope.launch { loadMonth(m) }
    }

    private suspend fun loadMonth(month: YearMonth) {
        val current = history ?: return
        val hoy = today()
        if (month !in mesesCargados && !month.atDay(1).isAfter(hoy)) {
            _uiState.update { it.copy(calendarLoading = true, calendarError = null) }
            val hasta = minOf(month.atEndOfMonth(), hoy)
            when (val r = repository.dailyProgress(month.atDay(1), hasta)) {
                is DailyProgressResult.Success -> {
                    val nuevos = r.days.mapNotNull { d ->
                        runCatching { LocalDate.parse(d.fecha) }.getOrNull()?.let { it to DayTotals(d.completados, d.total) }
                    }.toMap()
                    history = current.copy(dailyTotals = (current.dailyTotals.orEmpty() + nuevos))
                    mesesCargados += month
                }
                // Sin el endpoint se calcula con el historial por hábito, que ya está completo.
                DailyProgressResult.Unavailable -> mesesCargados += month
                is DailyProgressResult.Error -> _uiState.update { it.copy(calendarError = r.message) }
            }
        }
        val h = history ?: return
        val cells = withContext(Dispatchers.Default) { ProgressCalculator.monthCells(h, month, hoy) }
        // Solo se pinta si el usuario no cambió de mes mientras cargaba.
        if (_uiState.value.calendarMonth == month) {
            _uiState.update { it.copy(calendarCells = cells, calendarLoading = false) }
        }
    }

    private suspend fun recompute() {
        val current = history ?: return
        val period = _uiState.value.period
        val summary = withContext(Dispatchers.Default) { ProgressCalculator.summarize(current, period, today()) }
        _uiState.update { it.copy(isLoading = false, error = null, summary = summary) }
    }
}

private fun YearMonth.coerceIn(min: YearMonth, max: YearMonth): YearMonth = when {
    this < min -> min
    this > max -> max
    else -> this
}
