package com.vitta.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.repository.CreateRecordResult
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.data.repository.StreakResult
import com.vitta.app.ui.screens.habits.HabitIcon
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneOffset

data class HabitCardUiState(
    val habit: HabitDto,
    val icon: HabitIcon?,
    val doneToday: Boolean,
    val streakActual: Int,
    val metaTarget: Int?,
    val metaUnidad: String,
    val esAgua: Boolean,
    val pendingValue: Int,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null
)

data class ChecklistUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val cards: List<HabitCardUiState> = emptyList()
)

private fun parseMeta(metaTexto: String): Pair<Int?, String> {
    val match = Regex("""\d+""").find(metaTexto)
    val numero = match?.value?.toIntOrNull()
    val unidad = if (match != null) metaTexto.removeRange(match.range).trim() else metaTexto
    return numero to unidad
}

private fun iconForHabit(nombre: String): HabitIcon? {
    val n = nombre.lowercase()
    return when {
        n.contains("agua") -> HabitIcon.Water
        n.contains("dormir") || n.contains("sueño") || n.contains("descanso") -> HabitIcon.Sleep
        n.contains("camin") || n.contains("paso") || n.contains("ejercicio") ||
                n.contains("entrena") || n.contains("gym") || n.contains("deporte") -> HabitIcon.Walk
        n.contains("yoga") || n.contains("medita") -> HabitIcon.Yoga
        n.contains("leer") || n.contains("lectura") -> HabitIcon.Read
        n.contains("comer") || n.contains("fruta") || n.contains("aliment") -> HabitIcon.Eat
        else -> null
    }
}

private fun stepFor(unidad: String): Int = when {
    unidad.contains("paso") -> 1000
    unidad.contains("minuto") -> 5
    else -> 1
}

class ChecklistViewModel(
    private val repository: HabitRepository = HabitRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val habitsResult = repository.listHabits()) {
                is HabitsResult.Success -> {
                    val cards = coroutineScope {
                        habitsResult.habits.map { habit -> async { buildCard(habit) } }.awaitAll()
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false, cards = cards)
                }
                is HabitsResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = habitsResult.message)
                }
            }
        }
    }

    private suspend fun buildCard(habit: HabitDto): HabitCardUiState {
        val (metaTarget, metaUnidad) = parseMeta(habit.meta)
        val today = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()

        val doneToday = when (val records = repository.listRecords(habit.id)) {
            is RecordsResult.Success -> records.records.any { record ->
                record.cumplido && Instant.parse(record.fecha).atZone(ZoneOffset.UTC).toLocalDate() == today
            }
            is RecordsResult.Error -> false
        }

        val streakActual = when (val streak = repository.getStreak(habit.id)) {
            is StreakResult.Success -> streak.streak.racha_actual
            is StreakResult.Error -> 0
        }

        return HabitCardUiState(
            habit = habit,
            icon = iconForHabit(habit.nombre),
            doneToday = doneToday,
            streakActual = streakActual,
            metaTarget = metaTarget,
            metaUnidad = metaUnidad,
            esAgua = habit.nombre.contains("agua", ignoreCase = true),
            pendingValue = if (doneToday) metaTarget ?: 0 else 0
        )
    }

    private fun updateCard(habitId: Int, transform: (HabitCardUiState) -> HabitCardUiState) {
        _uiState.value = _uiState.value.copy(
            cards = _uiState.value.cards.map { if (it.habit.id == habitId) transform(it) else it }
        )
    }

    /** Toca un vaso específico: fija el conteo ahí. Si llega a la meta, guarda de una vez. */
    fun onGlassTap(habitId: Int, index: Int) {
        val card = _uiState.value.cards.find { it.habit.id == habitId } ?: return
        if (card.doneToday) return
        val nuevoValor = index + 1
        updateCard(habitId) { it.copy(pendingValue = nuevoValor) }
        if (card.metaTarget != null && nuevoValor >= card.metaTarget) {
            submit(habitId, nuevoValor)
        }
    }

    /** Botón "Todos": llena todo de una vez y guarda. */
    fun onMarkAll(habitId: Int) {
        val card = _uiState.value.cards.find { it.habit.id == habitId } ?: return
        val meta = card.metaTarget ?: 1
        updateCard(habitId) { it.copy(pendingValue = meta) }
        submit(habitId, meta)
    }

    /** Botón "+N": suma el paso; si llega a la meta, guarda. */
    fun onQuickAdd(habitId: Int) {
        val card = _uiState.value.cards.find { it.habit.id == habitId } ?: return
        if (card.doneToday) return
        val step = stepFor(card.metaUnidad)
        val nuevoValor = card.pendingValue + step
        updateCard(habitId) { it.copy(pendingValue = nuevoValor) }
        if (card.metaTarget != null && nuevoValor >= card.metaTarget) {
            submit(habitId, nuevoValor)
        }
    }

    /** Hábito booleano (sin meta numérica): un solo toque lo completa. */
    fun onToggleBoolean(habitId: Int) {
        val card = _uiState.value.cards.find { it.habit.id == habitId } ?: return
        if (card.doneToday) return
        submit(habitId, null)
    }

    private fun submit(habitId: Int, valor: Int?) {
        updateCard(habitId) { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val fechaIso = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
            when (val result = repository.createRecord(habitId, fechaIso, true, valor?.toDouble())) {
                is CreateRecordResult.Success -> {
                    val streakActual = when (val streak = repository.getStreak(habitId)) {
                        is StreakResult.Success -> streak.streak.racha_actual
                        is StreakResult.Error -> 0
                    }
                    updateCard(habitId) {
                        it.copy(isSubmitting = false, doneToday = true, streakActual = streakActual)
                    }
                }
                is CreateRecordResult.Error -> {
                    updateCard(habitId) { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }
}