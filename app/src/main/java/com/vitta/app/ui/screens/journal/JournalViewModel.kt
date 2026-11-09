package com.vitta.app.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconStore
import com.vitta.app.ui.components.icons.HabitIcons
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** Una nota del día guardada en un registro (`registros_diarios.reflexion` → `notas`). */
data class JournalEntry(
    val recordId: Int,
    val habitId: Int,
    val habitName: String,
    val icon: HabitIcon,
    val dia: LocalDate,
    val nota: String,
    val cumplido: Boolean,
    val valor: Double?,
    val meta: String
)

/** Filtro por fecha de la bitácora general (HAL-15). */
enum class JournalPeriodo(val label: String) {
    Todo("Todo"),
    Semana("Últimos 7 días"),
    Mes("Este mes"),
    MesPasado("Mes pasado")
}

data class JournalUiState(
    val cargando: Boolean = true,
    val error: String? = null,
    /** null = bitácora general; con id = bitácora de un hábito (HAL-14). */
    val habitIdFijo: Int? = null,
    val habits: List<HabitDto> = emptyList(),
    val entries: List<JournalEntry> = emptyList(),
    val filtroHabito: Int? = null,
    val filtroPeriodo: JournalPeriodo = JournalPeriodo.Todo,
    /** Búsqueda por letras o números en el texto de la nota o el nombre del hábito. */
    val busqueda: String = ""
) {
    /** Cuántos filtros (hábito y fecha) están activos, para el botón "Filtros". */
    val filtrosActivos: Int get() = (if (filtroHabito != null && habitIdFijo == null) 1 else 0) + (if (filtroPeriodo != JournalPeriodo.Todo) 1 else 0)

    val habitFijo: HabitDto? get() = habits.firstOrNull { it.id == habitIdFijo }

    /** Notas visibles, de la más reciente a la más antigua. */
    val visibles: List<JournalEntry>
        get() {
            val hoy = LocalDate.now(ZoneId.systemDefault())
            val habito = habitIdFijo ?: filtroHabito
            val q = normalizar(busqueda.trim())
            return entries
                .filter { q.isEmpty() || normalizar(it.nota).contains(q) || normalizar(it.habitName).contains(q) }
                .filter { habito == null || it.habitId == habito }
                .filter {
                    when (filtroPeriodo) {
                        JournalPeriodo.Todo -> true
                        JournalPeriodo.Semana -> !it.dia.isBefore(hoy.minusDays(6))
                        JournalPeriodo.Mes -> it.dia.year == hoy.year && it.dia.month == hoy.month
                        JournalPeriodo.MesPasado -> hoy.minusMonths(1).let { m -> it.dia.year == m.year && it.dia.month == m.month }
                    }
                }
                .sortedWith(compareByDescending<JournalEntry> { it.dia }.thenByDescending { it.recordId })
        }

    /** Hábitos que tienen al menos una nota (para los chips del filtro). */
    val habitsConNotas: List<HabitDto> get() = habits.filter { h -> entries.any { it.habitId == h.id } }
}

/**
 * Bitácoras con datos reales: `GET /api/habits?incluir_archivados=true` y
 * `GET /api/habits/:id/records` (cada registro trae su `notas`).
 */
class JournalViewModel(
    private val habitId: Int?,
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState(habitIdFijo = habitId))
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            val habits = when (val r = repository.listAllHabits()) {
                is HabitsResult.Success -> r.habits
                is HabitsResult.Error -> {
                    _uiState.update { it.copy(cargando = false, error = r.message) }
                    return@launch
                }
            }
            val objetivo = if (habitId == null) habits else habits.filter { it.id == habitId }
            val resultados = coroutineScope {
                objetivo.map { h -> async { h to repository.listRecords(h.id) } }.awaitAll()
            }
            if (resultados.any { it.second is RecordsResult.Error }) {
                _uiState.update { it.copy(cargando = false, habits = habits, error = "No se pudieron cargar todas las notas. Revisa tu conexión.") }
                return@launch
            }
            val entries = resultados.flatMap { (h, r) ->
                val icon = HabitIcons.byId(h.icono) ?: HabitIconStore.iconFor(h.id, h.nombre)
                (r as RecordsResult.Success).records.mapNotNull { it.toEntry(h, icon) }
            }
            _uiState.update { it.copy(cargando = false, habits = habits, entries = entries) }
        }
    }

    fun filtrarHabito(id: Int?) = _uiState.update { it.copy(filtroHabito = id) }
    fun filtrarPeriodo(p: JournalPeriodo) = _uiState.update { it.copy(filtroPeriodo = p) }
    fun buscar(texto: String) = _uiState.update { it.copy(busqueda = texto.take(60)) }
    fun limpiarFiltros() = _uiState.update { it.copy(filtroHabito = null, filtroPeriodo = JournalPeriodo.Todo, busqueda = "") }
}

private fun RecordDto.toEntry(h: HabitDto, icon: HabitIcon): JournalEntry? {
    val texto = notas?.takeIf { it.isNotBlank() } ?: return null
    val dia = dia?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: runCatching { Instant.parse(fecha).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()
        ?: return null
    return JournalEntry(id, h.id, h.nombre, icon, dia, texto, cumplido, valor, h.meta)
}

class JournalViewModelFactory(private val habitId: Int?) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return JournalViewModel(habitId) as T
    }
}

/** Minúsculas y sin tildes, para que "dia" encuentre "día". */
internal fun normalizar(texto: String): String =
    java.text.Normalizer.normalize(texto.lowercase(), java.text.Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "")
