package com.vitta.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitta.app.data.local.LogroCelebrationStore
import com.vitta.app.data.local.StreakNoticeStore
import com.vitta.app.data.remote.dto.DashboardDto
import com.vitta.app.data.remote.dto.UserPointsDto
import com.vitta.app.data.repository.DashboardResult
import com.vitta.app.data.repository.RemindersResult
import com.vitta.app.data.repository.UserRepository
import com.vitta.app.data.remote.dto.HabitDto
import com.vitta.app.data.remote.dto.LogroDto
import com.vitta.app.data.remote.dto.RecordDto
import com.vitta.app.data.remote.dto.RecordWriteDto
import com.vitta.app.data.repository.CreateRecordResult
import com.vitta.app.data.repository.HabitRepository
import com.vitta.app.data.repository.HabitResult
import com.vitta.app.data.repository.HabitsResult
import com.vitta.app.data.repository.LogroRepository
import com.vitta.app.data.repository.LogrosResult
import com.vitta.app.data.repository.RecordsResult
import com.vitta.app.data.repository.SaveProgressResult
import com.vitta.app.data.repository.StreakResult
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconStore
import com.vitta.app.ui.components.icons.HabitIcons
import com.vitta.app.ui.screens.habits.HabitTextFormat
import com.vitta.app.ui.screens.habits.WeekDays
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HabitCardUiState(
    val habit: HabitDto,
    val icon: HabitIcon,
    val doneToday: Boolean,
    val streakActual: Int,
    val streakMax: Int = 0,
    /** false si la frecuencia es de días específicos y hoy no es uno de ellos. */
    val scheduledToday: Boolean = true,
    val metaTarget: Int?,
    val metaUnidad: String,
    val esAgua: Boolean,
    /** Avance que ve el usuario (se actualiza al instante con cada toque). */
    val pendingValue: Int,
    /** Último avance confirmado por el backend (`PATCH /progreso`). */
    val savedValue: Int = 0,
    val isSubmitting: Boolean = false,
    /** Hay un avance parcial guardándose; no bloquea nuevos toques. */
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    /** Hora del recordatorio (`HH:MM`, `/api/recordatorios`); ubica el hábito en la pestaña Hoy. */
    val reminderHora: String? = null,
    /** Días (locales) con registro cumplido; sirven para saber cuándo se cortó una racha. */
    val completedDays: Set<LocalDate> = emptySet(),
    /** Registro de hoy en el backend (completo o parcial); se usa para desmarcar. */
    val todayRecordId: Int? = null,
    /** Nota del día (`notas` del registro de hoy). */
    val nota: String? = null,
    /** La nota se está guardando. */
    val isSavingNote: Boolean = false
)

/**
 * Aviso "Racha terminada" del mockup, con datos reales: la racha de un hábito
 * se corta cuando termina (medianoche local) un día programado sin cumplirlo.
 */
data class RachaPerdidaUi(
    /** Claves "hábito|día" de todas las rachas cortadas recientes (se cierran juntas). */
    val keys: List<String>,
    val habitIds: Set<Int>,
    /** Días reales que llevaba la racha que se cortó (calculados del historial). */
    val dias: Int,
    /** Día programado que quedó sin cumplir (cuando "cerró" la racha). */
    val cerroEl: LocalDate,
    val record: Int,
    /** Mes (y año si no es el actual) en que terminó la racha récord; null si no se puede ubicar. */
    val recordMes: String?
)

/** Modal "¡Felicitaciones!" al sumar un nuevo día a la racha del usuario. */
data class RachaSumadaUi(
    /** Racha real del usuario tras el registro (la misma "N días de racha" de Inicio). */
    val dias: Int
)

/** Límite del backend para `notas` (validation.ts → NOTAS_MAX). */
const val NOTA_MAX = 1000

/** Solo se avisan rachas cortadas en la última semana; las más viejas ya no son noticia. */
private const val DIAS_AVISO_RACHA_PERDIDA = 7L

data class ChecklistUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val cards: List<HabitCardUiState> = emptyList(),
    /** Medalla recién desbloqueada que se está celebrando (una a la vez). */
    val logroCelebrado: LogroDto? = null,
    /** VitaPuntos y nivel reales (`GET /api/dashboard`). */
    val puntos: UserPointsDto? = null,
    val rachaPerdida: RachaPerdidaUi? = null,
    val rachaSumada: RachaSumadaUi? = null,
    /** Mensaje breve para el snackbar ("¡Leer listo! +10 VitaPuntos", "Nota guardada"). */
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

/** Día local: el backend asigna cada registro al día de la zona que envía la app (X-Timezone). */
private fun today(): LocalDate = LocalDate.now(ZoneId.systemDefault())

/** Día del registro: el campo `dia` del backend o, si no viene, el día local de `fecha`. */
private fun RecordDto.localDay(): LocalDate? =
    dia?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        ?: runCatching { Instant.parse(fecha).atZone(ZoneId.systemDefault()).toLocalDate() }.getOrNull()

class ChecklistViewModel(
    private val repository: HabitRepository = HabitRepository(),
    private val logroRepository: LogroRepository = LogroRepository(),
    private val celebrationStore: LogroCelebrationStore = LogroCelebrationStore,
    private val userRepository: UserRepository = UserRepository(),
    private val streakNotices: StreakNoticeStore = StreakNoticeStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChecklistUiState())
    val uiState: StateFlow<ChecklistUiState> = _uiState.asStateFlow()

    /** Un candado por hábito: los avances se envían en orden y nunca en paralelo. */
    private val saveLocks = mutableMapOf<Int, Mutex>()

    /** Hábitos para los que el backend rechazó `PATCH /progreso` (sin `meta_valor`). */
    private val partialUnsupported = mutableSetOf<Int>()

    /** Serializa la detección de medallas para no celebrar dos veces la misma. */
    private val logrosMutex = Mutex()
    private val celebrationQueue = ArrayDeque<LogroDto>()
    private val sessionStart: Instant = Instant.now()

    init {
        load()
        viewModelScope.launch { rememberAlreadyObtained() }
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val remindersCall = async { userRepository.listReminders() }
            val dashboardCall = async { userRepository.getDashboard() }
            when (val habitsResult = repository.listHabits()) {
                is HabitsResult.Success -> {
                    val horas = (remindersCall.await() as? RemindersResult.Success)?.reminders
                        ?.filter { it.activo }?.associate { it.habito_id to it.hora }.orEmpty()
                    val cards = coroutineScope {
                        habitsResult.habits.map { habit -> async { buildCard(habit, horas[habit.id]) } }.awaitAll()
                    }
                    val dashboard = (dashboardCall.await() as? DashboardResult.Success)?.dashboard
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        cards = cards,
                        puntos = dashboard?.puntos ?: _uiState.value.puntos,
                        rachaPerdida = rachaPerdida(dashboard, cards, streakNotices.dismissed())
                    )
                }
                is HabitsResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = habitsResult.message)
                }
            }
        }
    }

    /** VitaPuntos/nivel tras registrar (los puntos cambian con cada cumplimiento y cada medalla). */
    private suspend fun refreshPuntos() {
        val dashboard = (userRepository.getDashboard() as? DashboardResult.Success)?.dashboard ?: return
        _uiState.value = _uiState.value.copy(puntos = dashboard.puntos ?: _uiState.value.puntos)
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    private fun showMessage(text: String) {
        _uiState.value = _uiState.value.copy(message = text)
    }

    /** "Empezar una racha nueva hoy": cierra el aviso y no vuelve a mostrarlo. */
    fun dismissRachaPerdida() {
        val aviso = _uiState.value.rachaPerdida ?: return
        _uiState.value = _uiState.value.copy(rachaPerdida = null)
        viewModelScope.launch { aviso.keys.forEach { streakNotices.dismiss(it) } }
    }

    private suspend fun buildCard(habit: HabitDto, reminderHora: String?): HabitCardUiState {
        val (metaTarget, metaUnidad) = HabitTextFormat.parseMeta(habit.meta)
        val today = today()
        HabitIconStore.sync(habit.id, habit.icono)

        // Registro de hoy (uno por hábito y día): trae el avance guardado, parcial o completo.
        val records = (repository.listRecords(habit.id) as? RecordsResult.Success)?.records.orEmpty()
        val todayRecord = records.lastOrNull { it.localDay() == today }
        val completedDays = records.filter { it.cumplido }.mapNotNull { it.localDay() }.toSet()
        val doneToday = todayRecord?.cumplido == true
        val saved = todayRecord?.valor?.toInt() ?: if (doneToday) metaTarget ?: 0 else 0

        val streak = when (val streak = repository.getStreak(habit.id)) {
            is StreakResult.Success -> streak.streak
            is StreakResult.Error -> null
        }
        val todayCode = WeekDays[today.dayOfWeek.value - 1].first

        return HabitCardUiState(
            habit = habit,
            // Icono guardado en el backend; si el hábito es antiguo y no lo tiene, el del teléfono o el del nombre.
            icon = HabitIcons.byId(habit.icono) ?: HabitIconStore.iconFor(habit.id, habit.nombre),
            doneToday = doneToday,
            streakActual = streak?.racha_actual ?: 0,
            streakMax = streak?.racha_maxima ?: 0,
            scheduledToday = HabitTextFormat.isScheduledOn(habit.frecuencia, todayCode),
            metaTarget = metaTarget,
            metaUnidad = metaUnidad,
            esAgua = habit.nombre.contains("agua", ignoreCase = true),
            pendingValue = if (doneToday) maxOf(saved, metaTarget ?: 0) else saved,
            savedValue = saved,
            reminderHora = reminderHora,
            completedDays = completedDays,
            todayRecordId = todayRecord?.id,
            nota = todayRecord?.notas?.takeIf { it.isNotBlank() }
        )
    }

    /**
     * Detecta una racha perdida con el historial real (`GET /records`) y la
     * misma regla del backend (`computeStreaks`): la racha se corta cuando
     * termina un día programado (medianoche en la zona del usuario) sin
     * registro cumplido; los días no programados no cortan. Hoy todavía no
     * cuenta: sigue abierto hasta medianoche. El récord sale de
     * `GET /api/dashboard` (`rachas.maxima`).
     */
    private fun rachaPerdida(dashboard: DashboardDto?, cards: List<HabitCardUiState>, cerrados: Set<String>): RachaPerdidaUi? {
        val hoy = today()
        val perdidas = cards.mapNotNull { card ->
            val ultimo = card.completedDays.maxOrNull() ?: return@mapNotNull null
            // Primer día programado después del último cumplido que ya terminó sin cumplirse.
            val cerro = generateSequence(ultimo.plusDays(1)) { it.plusDays(1) }
                .takeWhile { it.isBefore(hoy) }
                .firstOrNull { scheduledOn(card.habit.frecuencia, it) }
                ?: return@mapNotNull null
            val dias = runEndingAt(card.completedDays, card.habit.frecuencia, ultimo)
            val reciente = !cerro.isBefore(hoy.minusDays(DIAS_AVISO_RACHA_PERDIDA))
            if (dias <= 0 || !reciente || "${card.habit.id}|$cerro" in cerrados) null else Triple(card, cerro, dias)
        }
        // La que se cortó más recientemente; si hay empate, la más larga.
        val (card, cerro, dias) = perdidas
            .sortedWith(compareByDescending<Triple<HabitCardUiState, LocalDate, Int>> { it.second }.thenByDescending { it.third })
            .firstOrNull() ?: return null
        val porId = cards.associateBy { it.habit.id }
        val maxima = dashboard?.rachas?.maxima
        val record = maxOf(maxima?.dias ?: 0, card.streakMax, dias)
        val recordMes = run {
            val fuente = maxima?.let { porId[it.habito_id] } ?: card
            val (largo, fin) = longestRun(fuente.completedDays, fuente.habit.frecuencia)
            if (largo == record && fin != null) mesDe(fin, hoy) else null
        }
        return RachaPerdidaUi(
            keys = perdidas.map { "${it.first.habit.id}|${it.second}" },
            habitIds = perdidas.map { it.first.habit.id }.toSet(),
            dias = dias,
            cerroEl = cerro,
            record = record,
            recordMes = recordMes
        )
    }

    /** Largo de la racha que termina en [fin] (los días no programados no la cortan). */
    private fun runEndingAt(days: Set<LocalDate>, frecuencia: String, fin: LocalDate): Int {
        var n = 0
        var d = fin
        val desde = days.minOrNull() ?: return 0
        while (!d.isBefore(desde)) {
            if (d in days) n += 1
            else if (scheduledOn(frecuencia, d)) break
            d = d.minusDays(1)
        }
        return n
    }

    private fun scheduledOn(frecuencia: String, day: LocalDate): Boolean =
        HabitTextFormat.isScheduledOn(frecuencia, WeekDays[day.dayOfWeek.value - 1].first)

    /** Racha más larga (misma regla del backend: los días no programados no cortan) y su último día. */
    private fun longestRun(days: Set<LocalDate>, frecuencia: String): Pair<Int, LocalDate?> {
        val desde = days.minOrNull() ?: return 0 to null
        val hasta = days.max()
        var actual = 0
        var mejor = 0
        var fin: LocalDate? = null
        var d = desde
        while (!d.isAfter(hasta)) {
            if (d in days) {
                actual += 1
                if (actual >= mejor) { mejor = actual; fin = d }
            } else if (scheduledOn(frecuencia, d)) {
                actual = 0
            }
            d = d.plusDays(1)
        }
        return mejor to fin
    }

    private fun mesDe(day: LocalDate, hoy: LocalDate): String {
        val mes = day.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))
        return if (day.year == hoy.year) mes else "$mes de ${day.year}"
    }

    private fun card(habitId: Int): HabitCardUiState? = _uiState.value.cards.find { it.habit.id == habitId }

    private fun updateCard(habitId: Int, transform: (HabitCardUiState) -> HabitCardUiState) {
        _uiState.value = _uiState.value.copy(
            cards = _uiState.value.cards.map { if (it.habit.id == habitId) transform(it) else it }
        )
    }

    /** Toca un vaso específico: fija el conteo ahí y guarda el avance. */
    fun onGlassTap(habitId: Int, index: Int) = setProgress(habitId, index + 1)

    /** Botón "Todos": llena todo de una vez y guarda. */
    fun onMarkAll(habitId: Int) {
        val meta = card(habitId)?.metaTarget ?: return
        setProgress(habitId, meta)
    }

    /** Botón "+N": suma el paso y guarda el avance. */
    fun onQuickAdd(habitId: Int) {
        val card = card(habitId) ?: return
        setProgress(habitId, card.pendingValue + stepFor(card.metaUnidad))
    }

    /** Círculo de la pestaña Hoy: cierra el hábito de una vez (meta completa o "Listo"). */
    fun onComplete(habitId: Int) {
        val card = card(habitId) ?: return
        if (card.metaTarget != null) onMarkAll(habitId) else onToggleBoolean(habitId)
    }

    /** Hábito booleano (sin meta numérica): un solo toque lo completa. */
    fun onToggleBoolean(habitId: Int) {
        val card = card(habitId) ?: return
        if (card.doneToday || card.isSubmitting) return
        submit(habitId, null)
    }

    // ---------------------------------------------------------------------
    // Avance parcial (PATCH /api/habits/:id/progreso)
    // ---------------------------------------------------------------------

    /** Actualiza el avance en pantalla al instante y lo envía al backend. */
    private fun setProgress(habitId: Int, value: Int) {
        val card = card(habitId) ?: return
        if (card.doneToday || card.isSubmitting) return
        val meta = card.metaTarget ?: return
        updateCard(habitId) { it.copy(pendingValue = value.coerceIn(0, meta), errorMessage = null) }
        persistProgress(habitId)
    }

    /**
     * Envía el ÚLTIMO avance del hábito. Cada toque lanza una llamada, pero el
     * candado por hábito las ejecuta de una en una y cada una lee el valor
     * más reciente al entrar: los toques rápidos se agrupan, las respuestas no
     * llegan desordenadas y nunca se pierde el último cambio. Como el backend
     * recibe el total del día (no un incremento), reenviar es seguro.
     */
    private fun persistProgress(habitId: Int) {
        viewModelScope.launch {
            val mutex = saveLocks.getOrPut(habitId) { Mutex() }
            mutex.withLock {
                val card = card(habitId) ?: return@withLock
                if (card.doneToday) return@withLock
                val meta = card.metaTarget ?: return@withLock
                val target = card.pendingValue

                if (habitId in partialUnsupported) {
                    // Sin avance parcial en el backend: se registra solo al completar la meta.
                    if (target >= meta) submitRecord(habitId, target)
                    return@withLock
                }
                if (target == card.savedValue) return@withLock

                updateCard(habitId) { it.copy(isSaving = true) }
                ensureMetaValor(habitId)
                when (val result = repository.saveProgress(habitId, target.toDouble())) {
                    is SaveProgressResult.Success -> {
                        val record = result.record
                        // Completado SOLO si el backend lo confirma (valor >= meta_valor).
                        updateCard(habitId) {
                            it.copy(
                                isSaving = false,
                                todayRecordId = record.id,
                                savedValue = record.valor?.toInt() ?: target,
                                doneToday = record.cumplido,
                                pendingValue = if (record.cumplido) maxOf(it.pendingValue, meta) else it.pendingValue,
                                errorMessage = null
                            )
                        }
                        onRecordWritten(habitId, record)
                    }
                    SaveProgressResult.Unsupported -> {
                        partialUnsupported += habitId
                        updateCard(habitId) { it.copy(isSaving = false) }
                        if (target >= meta) submitRecord(habitId, target)
                    }
                    is SaveProgressResult.Error -> {
                        // El valor sigue en pantalla; el próximo toque lo reintenta.
                        updateCard(habitId) { it.copy(isSaving = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    /**
     * `PATCH /progreso` compara contra `meta_valor`. Los hábitos creados desde
     * la app solo guardan la meta como texto ("8 vasos"), así que antes de
     * enviar el avance se alinea `meta_valor` con esa meta (PUT parcial).
     */
    private suspend fun ensureMetaValor(habitId: Int) {
        val card = card(habitId) ?: return
        val meta = card.metaTarget ?: return
        if (meta <= 0 || card.habit.meta_valor == meta.toDouble()) return
        val unidad = card.metaUnidad.trim().take(30).ifBlank { null }
        val result = repository.updateMetaValor(habitId, meta.toDouble(), unidad)
        if (result is HabitResult.Success) {
            updateCard(habitId) { it.copy(habit = result.habit) }
        }
    }

    // ---------------------------------------------------------------------
    // Registro completo (POST /api/habits/:id/records)
    // ---------------------------------------------------------------------

    private fun submit(habitId: Int, valor: Int?) {
        viewModelScope.launch { submitRecord(habitId, valor) }
    }

    private suspend fun submitRecord(habitId: Int, valor: Int?) {
        // Evita envíos duplicados (dobles toques o toques en cola mientras la UI está ocupada).
        val card = card(habitId) ?: return
        if (card.isSubmitting || card.doneToday) return
        updateCard(habitId) { it.copy(isSubmitting = true, errorMessage = null) }
        val fechaIso = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS).toString()
        when (val result = repository.createRecord(habitId, fechaIso, true, valor?.toDouble())) {
            is CreateRecordResult.Success -> {
                updateCard(habitId) {
                    it.copy(
                        isSubmitting = false,
                        doneToday = result.record.cumplido,
                        todayRecordId = result.record.id,
                        errorMessage = null
                    )
                }
                onRecordWritten(habitId, result.record)
            }
            CreateRecordResult.AlreadyRegistered -> {
                // 409 = el registro de hoy ya existe en el servidor: el hábito está hecho,
                // así que se refleja igual que un guardado correcto (sin mensaje de error).
                val recordId = findTodayRecordId(habitId)
                updateCard(habitId) {
                    it.copy(isSubmitting = false, doneToday = true, todayRecordId = recordId, errorMessage = null)
                }
                refreshStreak(habitId)
            }
            is CreateRecordResult.Error -> {
                updateCard(habitId) { it.copy(isSubmitting = false, errorMessage = result.message) }
            }
        }
    }

    private suspend fun findTodayRecordId(habitId: Int): Int? =
        (repository.listRecords(habitId) as? RecordsResult.Success)?.records?.lastOrNull { it.localDay() == today() }?.id

    // ---------------------------------------------------------------------
    // Desmarcar (DELETE /api/habits/:id/records/:registroId)
    // ---------------------------------------------------------------------

    /** Borra el registro de hoy: el backend retira sus VitaPuntos y recalcula la racha. */
    fun onUndo(habitId: Int) {
        val card = card(habitId) ?: return
        if (card.isSubmitting) return
        updateCard(habitId) { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            saveLocks.getOrPut(habitId) { Mutex() }.withLock {
                val recordId = card(habitId)?.todayRecordId ?: findTodayRecordId(habitId)
                val error = recordId?.let { repository.deleteRecord(habitId, it) }
                if (error != null) {
                    updateCard(habitId) { it.copy(isSubmitting = false, errorMessage = error) }
                    return@withLock
                }
                updateCard(habitId) {
                    it.copy(
                        isSubmitting = false,
                        doneToday = false,
                        todayRecordId = null,
                        pendingValue = 0,
                        savedValue = 0,
                        nota = null,
                        completedDays = it.completedDays - today()
                    )
                }
                refreshStreak(habitId)
                refreshPuntos()
                showMessage("Registro deshecho")
            }
        }
    }

    // ---------------------------------------------------------------------
    // Nota del día (PATCH /api/habits/:id/progreso con `notas`)
    // ---------------------------------------------------------------------

    /**
     * Guarda (o borra, si [texto] queda vacío) la nota de hoy sin cambiar el
     * avance: se reenvía el mismo valor del día. `PATCH /progreso` necesita
     * `meta_valor`; los hábitos de sí/no quedan con meta 1 (1 = cumplido),
     * así la nota funciona en todos los hábitos y la pantalla no cambia.
     */
    fun saveNote(habitId: Int, texto: String) {
        val card = card(habitId) ?: return
        val nota = texto.trim().take(NOTA_MAX)
        if (nota == card.nota.orEmpty()) return
        updateCard(habitId) { it.copy(isSavingNote = true, errorMessage = null) }
        viewModelScope.launch {
            saveLocks.getOrPut(habitId) { Mutex() }.withLock {
                val actual = card(habitId) ?: return@withLock
                val metaValor = actual.metaTarget?.also { ensureMetaValor(habitId) }?.toDouble()
                    ?: actual.habit.meta_valor
                    ?: (repository.updateMetaValor(habitId, 1.0, null) as? HabitResult.Success)
                        ?.habit?.also { h -> updateCard(habitId) { it.copy(habit = h) } }?.meta_valor
                if (metaValor == null) {
                    updateCard(habitId) { it.copy(isSavingNote = false, errorMessage = "No se pudo guardar la nota.") }
                    return@withLock
                }
                val valor = if (actual.doneToday) maxOf(actual.savedValue.toDouble(), metaValor) else actual.savedValue.toDouble()
                when (val result = repository.saveProgress(habitId, valor, notas = nota)) {
                    is SaveProgressResult.Success -> {
                        updateCard(habitId) {
                            it.copy(
                                isSavingNote = false,
                                todayRecordId = result.record.id,
                                nota = result.record.notas?.takeIf { n -> n.isNotBlank() }
                            )
                        }
                        showMessage(if (nota.isEmpty()) "Nota borrada" else "Nota guardada")
                    }
                    SaveProgressResult.Unsupported ->
                        updateCard(habitId) { it.copy(isSavingNote = false, errorMessage = "No se pudo guardar la nota.") }
                    is SaveProgressResult.Error ->
                        updateCard(habitId) { it.copy(isSavingNote = false, errorMessage = result.message) }
                }
            }
        }
    }

    /** Efectos de un registro guardado: racha y, si quedó cumplido, medallas nuevas. */
    private suspend fun onRecordWritten(habitId: Int, record: RecordWriteDto) {
        // Racha del usuario (la misma que muestra Inicio: "N días de racha") antes del registro.
        val rachaUsuarioAntes = _uiState.value.bestCurrentStreak
        val racha = record.racha
        if (racha != null) {
            updateCard(habitId) {
                it.copy(streakActual = racha.racha_actual, streakMax = maxOf(it.streakMax, racha.racha_maxima))
            }
        } else {
            refreshStreak(habitId)
        }
        if (record.cumplido) {
            // Quien llama solo escribe si el hábito no estaba hecho: aquí acaba de completarse.
            card(habitId)?.let { showMessage("¡${it.habit.nombre} listo! +10 VitaPuntos") }
            // Volvió a cumplir el hábito: empieza una racha nueva y el aviso de racha perdida ya no aplica.
            _uiState.value.rachaPerdida?.let { aviso ->
                if (habitId in aviso.habitIds) _uiState.value = _uiState.value.copy(rachaPerdida = null)
            }
        }
        refreshPuntos()
        // Solo cuando el backend confirma que la racha del usuario subió (no por cada hábito).
        val rachaUsuarioAhora = _uiState.value.bestCurrentStreak
        if (record.cumplido && rachaUsuarioAhora > rachaUsuarioAntes) {
            avisarNuevoDiaDeRacha(record.dia ?: today().toString(), rachaUsuarioAhora)
        }
        if (record.cumplido || !record.logros_desbloqueados.isNullOrEmpty()) {
            detectNewLogros(record.logros_desbloqueados.orEmpty())
        }
    }

    // ---------------------------------------------------------------------
    // Modal "¡Felicitaciones! … nuevo día de racha"
    // ---------------------------------------------------------------------

    /** Evita mostrar el modal dos veces si dos registros terminan casi a la vez. */
    private val rachaDiaMutex = Mutex()

    /**
     * Muestra el modal una sola vez por día: la clave "racha_dia|fecha" queda
     * guardada por usuario en [StreakNoticeStore], así no se repite aunque se
     * completen más hábitos ese día o se reabra la app.
     */
    private suspend fun avisarNuevoDiaDeRacha(dia: String, dias: Int) = rachaDiaMutex.withLock {
        val key = "racha_dia|$dia"
        if (key in streakNotices.dismissed()) return@withLock
        streakNotices.dismiss(key)
        _uiState.value = _uiState.value.copy(rachaSumada = RachaSumadaUi(dias))
    }

    /** Botón "¡Genial!" (o tocar fuera): cierra el modal y, si hay, muestra la medalla pendiente. */
    fun dismissRachaSumada() {
        _uiState.value = _uiState.value.copy(rachaSumada = null)
        if (_uiState.value.logroCelebrado == null) showNextCelebration()
    }

    private suspend fun refreshStreak(habitId: Int) {
        val streak = repository.getStreak(habitId)
        if (streak is StreakResult.Success) {
            updateCard(habitId) {
                it.copy(
                    streakActual = streak.streak.racha_actual,
                    streakMax = maxOf(it.streakMax, streak.streak.racha_maxima)
                )
            }
        }
    }

    // ---------------------------------------------------------------------
    // Medallas desbloqueadas
    // ---------------------------------------------------------------------

    /**
     * Las medallas que el usuario ya tenía antes de abrir la app no se
     * celebran: se guardan como conocidas en [LogroCelebrationStore].
     */
    private suspend fun rememberAlreadyObtained() {
        val logros = (logroRepository.listLogros() as? LogrosResult.Success)?.logros ?: return
        logrosMutex.withLock {
            celebrationStore.markCelebrated(
                logros.filter { it.obtenido && !obtainedDuringSession(it) }.map { it.id }
            )
        }
    }

    private fun obtainedDuringSession(logro: LogroDto): Boolean {
        val at = logro.obtenido_en?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return false
        return !at.isBefore(sessionStart)
    }

    /**
     * Junta las medallas que el backend reporta en `logros_desbloqueados` con
     * las que `GET /api/logros` (vía [LogroRepository]) marca como obtenidas
     * durante esta sesión, descarta las ya celebradas y encola el resto.
     */
    private suspend fun detectNewLogros(fromResponse: List<LogroDto>) {
        val catalogo = (logroRepository.listLogros() as? LogrosResult.Success)?.logros.orEmpty()
        logrosMutex.withLock {
            val yaCelebrados = celebrationStore.celebrated()
            val enCola = celebrationQueue.map { it.id }.toSet() + listOfNotNull(_uiState.value.logroCelebrado?.id)
            val candidatos = LinkedHashMap<Int, LogroDto>()
            fromResponse.forEach { candidatos[it.id] = it }
            catalogo.filter { it.obtenido && obtainedDuringSession(it) }.forEach { candidatos.putIfAbsent(it.id, it) }
            val nuevos = candidatos.values.filter { it.id !in yaCelebrados && it.id !in enCola }
            // Las que ya estaban obtenidas desde antes se marcan sin celebrarlas.
            val antiguas = catalogo.filter { it.obtenido && it.id !in candidatos }.map { it.id }
            celebrationStore.markCelebrated(nuevos.map { it.id } + antiguas)
            celebrationQueue.addAll(nuevos)
            // Si está visible el aviso de racha, la medalla se muestra al terminar ese aviso.
            if (_uiState.value.logroCelebrado == null && _uiState.value.rachaSumada == null) showNextCelebration()
        }
    }

    private fun showNextCelebration() {
        _uiState.value = _uiState.value.copy(logroCelebrado = celebrationQueue.removeFirstOrNull())
    }

    /** Cierra la celebración actual y muestra la siguiente medalla, si hay. */
    fun dismissCelebration() {
        showNextCelebration()
    }
}
