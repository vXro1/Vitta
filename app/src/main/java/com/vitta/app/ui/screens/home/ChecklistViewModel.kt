package com.vitta.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.remote.dto.UserPointsDto
import com.vitta.app.data.repository.CreateRecordResult
import com.vitta.app.data.repository.GamificationRepository
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.data.repository.StreakResult
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconStore
import com.vitta.app.ui.screens.habits.HabitTextFormat
import com.vitta.app.ui.screens.habits.WeekDays
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
import java.time.ZoneOffset

data class HabitCardUiState(
    val habit: HabitDto,
    val icon: HabitIcon,
    val doneToday: Boolean,
    /** Registro de hoy en el backend; se usa para desmarcar. */
    val todayRecordId: Int? = null,
    val streakActual: Int,
    val streakMax: Int = 0,
    /** false si la frecuencia es de días específicos y hoy no es uno de ellos. */
    val scheduledToday: Boolean = true,
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
    val cards: List<HabitCardUiState> = emptyList(),
    /** VitaPuntos y nivel reales; null mientras cargan o si el backend no respondió. */
    val points: UserPointsDto? = null,
    /** Mensaje breve para el snackbar (p. ej. "+10 VitaPuntos"). */
    val message: String? = null
) {
    val todayCards: List<HabitCardUiState> get() = cards.filter { it.scheduledToday }
    val otherCards: List<HabitCardUiState> get() = cards.filterNot { it.scheduledToday }
    val doneToday: Int get() = todayCards.count { it.doneToday }
    val bestCurrentStreak: Int get() = cards.maxOfOrNull { it.streakActual } ?: 0
    val bestStreakEver: Int get() = cards.maxOfOrNull { maxOf(it.streakMax, it.streakActual) } ?: 0
}

private fun stepFor(unidad: String): Int = when {
    unidad.contains("paso") -> 1000
    unidad.contains("minuto") -> 5
    else -> 1
}

/** El backend guarda un registro por hábito y día calendario UTC. */
private fun todayUtc(): LocalDate = Instant.now().atZone(ZoneOffset.UTC).toLocalDate()

private fun RecordDto.isToday(): Boolean =
    runCatching { Instant.parse(fecha).atZone(ZoneOffset.UTC).toLocalDate() == todayUtc() }.getOrDefault(false)

class ChecklistViewModel(
    private val repository: HabitRepository = HabitRepository(),
    private val gamification: GamificationRepository = GamificationRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.cards.isEmpty(), errorMessage = null) }
            launch { refreshPoints() }
            when (val habitsResult = repository.listHabits()) {
                is HabitsResult.Success -> {
                    val cards = coroutineScope {
                        habitsResult.habits.map { habit -> async { buildCard(habit) } }.awaitAll()
                    }
                    _uiState.update { it.copy(isLoading = false, cards = cards) }
                }
                is HabitsResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = habitsResult.message) }
                }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private suspend fun refreshPoints() {
        gamification.dashboard().onSuccess { dash -> _uiState.update { it.copy(points = dash.puntos) } }
    }

    private suspend fun buildCard(habit: HabitDto): HabitCardUiState {
        val (metaTarget, metaUnidad) = HabitTextFormat.parseMeta(habit.meta)

        val todayRecord = when (val records = repository.listRecords(habit.id)) {
            is RecordsResult.Success -> records.records.firstOrNull { it.cumplido && it.isToday() }
            is RecordsResult.Error -> null
        }

        val streak = when (val streak = repository.getStreak(habit.id)) {
            is StreakResult.Success -> streak.streak
            is StreakResult.Error -> null
        }
        val todayCode = WeekDays[LocalDate.now().dayOfWeek.value - 1].first
        val doneToday = todayRecord != null

        return HabitCardUiState(
            habit = habit,
            icon = HabitIconStore.iconFor(habit.id, habit.nombre),
            doneToday = doneToday,
            todayRecordId = todayRecord?.id,
            streakActual = streak?.racha_actual ?: 0,
            streakMax = streak?.racha_maxima ?: 0,
            scheduledToday = HabitTextFormat.isScheduledOn(habit.frecuencia, todayCode),
            metaTarget = metaTarget,
            metaUnidad = metaUnidad,
            esAgua = habit.nombre.contains("agua", ignoreCase = true),
            pendingValue = if (doneToday) metaTarget ?: 0 else 0
        )
    }

    private fun updateCard(habitId: Int, transform: (HabitCardUiState) -> HabitCardUiState) {
        _uiState.update { state ->
            state.copy(cards = state.cards.map { if (it.habit.id == habitId) transform(it) else it })
        }
    }

    private fun card(habitId: Int) = _uiState.value.cards.find { it.habit.id == habitId }

    /** Toca un vaso específico: fija el conteo ahí. Si llega a la meta, guarda de una vez. */
    fun onGlassTap(habitId: Int, index: Int) {
        val card = card(habitId) ?: return
        if (card.doneToday || card.isSubmitting) return
        val nuevoValor = index + 1
        updateCard(habitId) { it.copy(pendingValue = nuevoValor) }
        if (card.metaTarget != null && nuevoValor >= card.metaTarget) {
            submit(habitId, nuevoValor)
        }
    }

    /** Botón "Todos": llena todo de una vez y guarda. */
    fun onMarkAll(habitId: Int) {
        val card = card(habitId) ?: return
        if (card.doneToday || card.isSubmitting) return
        val meta = card.metaTarget ?: 1
        updateCard(habitId) { it.copy(pendingValue = meta) }
        submit(habitId, meta)
    }

    /** Botón "+N": suma el paso; si llega a la meta, guarda. */
    fun onQuickAdd(habitId: Int) {
        val card = card(habitId) ?: return
        if (card.doneToday || card.isSubmitting) return
        val nuevoValor = card.pendingValue + stepFor(card.metaUnidad)
        updateCard(habitId) { it.copy(pendingValue = nuevoValor) }
        if (card.metaTarget != null && nuevoValor >= card.metaTarget) {
            submit(habitId, nuevoValor)
        }
    }

    /** Completa el hábito de una vez, tenga o no meta numérica. */
    fun onComplete(habitId: Int) {
        val card = card(habitId) ?: return
        if (card.doneToday || card.isSubmitting) return
        updateCard(habitId) { it.copy(pendingValue = it.metaTarget ?: 0) }
        submit(habitId, card.metaTarget)
    }

    /** Desmarca el registro de hoy: el backend borra el registro y sus puntos y recalcula la racha. */
    fun onUndo(habitId: Int) {
        val card = card(habitId) ?: return
        if (!card.doneToday || card.isSubmitting) return
        updateCard(habitId) { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val recordId = card.todayRecordId ?: findTodayRecordId(habitId)
            val error = if (recordId == null) null else repository.deleteRecord(habitId, recordId)
            if (error != null) {
                updateCard(habitId) { it.copy(isSubmitting = false, errorMessage = error) }
                return@launch
            }
            val streak = (repository.getStreak(habitId) as? StreakResult.Success)?.streak
            updateCard(habitId) {
                it.copy(
                    isSubmitting = false,
                    doneToday = false,
                    todayRecordId = null,
                    pendingValue = 0,
                    streakActual = streak?.racha_actual ?: it.streakActual,
                    streakMax = streak?.racha_maxima ?: it.streakMax
                )
            }
            _uiState.update { it.copy(message = "Registro deshecho") }
            refreshPoints()
        }
    }

    private suspend fun findTodayRecordId(habitId: Int): Int? =
        (repository.listRecords(habitId) as? RecordsResult.Success)?.records?.firstOrNull { it.isToday() }?.id

    private fun submit(habitId: Int, valor: Int?) {
        // Evita envíos duplicados (dobles toques o toques en cola mientras la UI está ocupada).
        val card = card(habitId) ?: return
        if (card.isSubmitting || card.doneToday) return
        updateCard(habitId) { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            val fechaIso = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
            when (val result = repository.createRecord(habitId, fechaIso, true, valor?.toDouble())) {
                is CreateRecordResult.Success, CreateRecordResult.AlreadyRegistered -> {
                    // 409 = el registro de hoy ya existe en el servidor: el hábito está hecho.
                    val recordId = (result as? CreateRecordResult.Success)?.record?.id ?: findTodayRecordId(habitId)
                    updateCard(habitId) {
                        it.copy(isSubmitting = false, doneToday = true, todayRecordId = recordId, errorMessage = null)
                    }
                    if (result is CreateRecordResult.Success) {
                        _uiState.update { it.copy(message = "¡${card.habit.nombre} listo! +10 VitaPuntos") }
                    }
                    val streak = repository.getStreak(habitId)
                    if (streak is StreakResult.Success) {
                        updateCard(habitId) {
                            it.copy(
                                streakActual = streak.streak.racha_actual,
                                streakMax = maxOf(it.streakMax, streak.streak.racha_maxima)
                            )
                        }
                    }
                    refreshPoints()
                }
                is CreateRecordResult.Error -> {
                    updateCard(habitId) { it.copy(isSubmitting = false, errorMessage = result.message) }
                }
            }
        }
    }
}
