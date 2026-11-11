package com.vitta.app.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import com.vitta.app.data.remote.dto.LogroDto
import com.vitta.app.ui.components.feedback.VittaRewardSheet
import com.vitta.app.ui.components.feedback.VittaStreakDialog
import com.vitta.app.ui.components.progress.VittaInsigniaBadge
import com.vitta.app.ui.screens.profile.insigniaPara
import com.vitta.app.ui.theme.Mk
import com.vitta.app.ui.theme.caprasimo
import com.vitta.app.ui.theme.figtree
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.components.auth.VittaAuthErrorBanner
import com.vitta.app.ui.components.brand.VittaLogo
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.components.mascot.MascotSize
import com.vitta.app.ui.components.mascot.MascotState
import com.vitta.app.ui.components.mascot.VittaMascot
import com.vitta.app.ui.screens.habits.VittaHabitIconBubble
import com.vitta.app.ui.screens.habits.VittaWaterTracker
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val DashboardMaxWidth = 640.dp

/**
 * Inicio (dashboard): encabezado con nivel, tarjeta principal con racha,
 * VitaPuntos y barra de nivel, lista "Hoy", y los bloques de abajo
 * y las celebraciones (nuevo día de racha, medalla desbloqueada).
 */
@Composable
fun ChecklistScreen(
    userName: String,
    viewModel: ChecklistViewModel,
    onAddHabit: () -> Unit,
    /** HAL-09: tocar un hábito abre su avance (la edición está dentro, en el lápiz). */
    onOpenHabit: (Int) -> Unit,
    onVerLogros: () -> Unit = {},
    onOpenNotifications: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(Modifier.fillMaxSize().background(VittaColorRoles.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VittaColorRoles.primary)
        }
        return
    }

    var undoTarget by remember { mutableStateOf<HabitCardUiState?>(null) }
    var noteTargetId by remember { mutableStateOf<Int?>(null) }
    var amountTargetId by remember { mutableStateOf<Int?>(null) }
    val points = state.puntos

    Box(Modifier.fillMaxSize().background(VittaColorRoles.background), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = DashboardMaxWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(VittaSpacing.Lg),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
        ) {
            item { HeaderBlock(userName = userName, level = points?.nivel, onOpenNotifications = onOpenNotifications) }

            state.errorMessage?.let { message ->
                item {
                    VittaAuthErrorBanner(message)
                    Spacer(Modifier.height(VittaSpacing.Sm))
                    VittaPrimaryButton(text = "Reintentar", onClick = viewModel::load)
                }
                if (state.cards.isEmpty()) return@LazyColumn
            }

            if (state.errorMessage == null && state.cards.isEmpty()) {
                item { EmptyHabits(onAddHabit = onAddHabit) }
                return@LazyColumn
            }

            val rachaPerdida = state.rachaPerdida
            if (rachaPerdida != null) {
                // Mockup: el aviso "Racha terminada" ocupa el lugar de la tarjeta principal.
                item { RachaTerminadaCard(rachaPerdida, onEmpezar = viewModel::dismissRachaPerdida) }
            } else {
                item {
                    HeroCard(
                        doneCount = state.doneToday,
                        totalCount = state.todayCards.size,
                        racha = state.bestCurrentStreak,
                        points = points?.puntos_totales,
                        levelName = points?.nombre_nivel,
                        pointsToNext = points?.puntos_para_siguiente,
                        levelProgress = points?.progresoNivel ?: 0f
                    )
                }
            }

            item {
                SectionTitle(
                    title = "Hoy",
                    trailing = {
                        Text(
                            "${state.doneToday} de ${state.todayCards.size}",
                            style = VittaTextStyles.body,
                            color = VittaColorRoles.textMuted
                        )
                    }
                )
            }

            if (state.todayCards.isEmpty() && state.cards.isNotEmpty()) {
                item {
                    Text(
                        "Hoy no tienes hábitos programados. ¡Disfruta el descanso!",
                        style = VittaTextStyles.body,
                        color = VittaColorRoles.textSecondary
                    )
                }
            }

            items(state.todayCards, key = { it.habit.id }) { card ->
                HabitDashboardCard(
                    card = card,
                    viewModel = viewModel,
                    onOpen = { onOpenHabit(card.habit.id) },
                    onUndoRequest = { undoTarget = card },
                    onNoteRequest = { noteTargetId = card.habit.id },
                    onAmountRequest = { amountTargetId = card.habit.id }
                )
            }

            item { AddHabitButton(onAddHabit) }

            if (state.otherCards.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Otros días",
                        subtitle = "No están programados para hoy, pero puedes registrarlos si quieres."
                    )
                }
                items(state.otherCards, key = { "other_${it.habit.id}" }) { card ->
                    HabitDashboardCard(
                        card = card,
                        viewModel = viewModel,
                        onOpen = { onOpenHabit(card.habit.id) },
                        onUndoRequest = { undoTarget = card },
                        onNoteRequest = { noteTargetId = card.habit.id },
                        onAmountRequest = { amountTargetId = card.habit.id }
                    )
                }
            }

            item { Spacer(Modifier.height(VittaSpacing.Xl)) }
        }

        // Celebraciones (último hijo del Box: el scrim solo cubre esta pantalla).
        CelebracionesOverlay(state = state, viewModel = viewModel, onVerLogros = onVerLogros)
    }

    undoTarget?.let { card ->
        UndoRecordDialog(
            habitName = card.habit.nombre,
            onConfirm = { viewModel.onUndo(card.habit.id); undoTarget = null },
            onDismiss = { undoTarget = null }
        )
    }

    noteTargetId?.let { id ->
        state.cards.find { it.habit.id == id }?.let { card ->
            HabitNoteDialog(
                card = card,
                onSave = { texto -> viewModel.saveNote(id, texto); noteTargetId = null },
                onDismiss = { noteTargetId = null }
            )
        }
    }

    amountTargetId?.let { id ->
        state.cards.find { it.habit.id == id }?.let { card ->
            HabitAmountDialog(
                card = card,
                onConfirm = { cantidad -> viewModel.onAddAmount(id, cantidad); amountTargetId = null },
                onDismiss = { amountTargetId = null }
            )
        }
    }
}

/**
 * Todas las celebraciones de Inicio y Hoy, de a una (HAL-12 y HAL-18):
 * racha (con los puntos del registro), VitaPuntos, medalla o resumen de medallas.
 * Puntos y medallas se cierran solos a los 3 s o al tocar la X / "¡Genial!".
 */
@Composable
internal fun CelebracionesOverlay(state: ChecklistUiState, viewModel: ChecklistViewModel, onVerLogros: () -> Unit) {
    // Primero la nota opcional del hábito recién completado; las celebraciones esperan a que se cierre.
    val enPausa = state.notaSugerida != null
    state.notaSugerida?.let { id ->
        state.cards.find { it.habit.id == id }?.let { card ->
            HabitNoteDialog(
                card = card,
                motivacion = true,
                onSave = { texto ->
                    if (texto.isNotBlank()) viewModel.saveNote(id, texto)
                    viewModel.dismissNotaSugerida()
                },
                onDismiss = viewModel::dismissNotaSugerida
            )
        }
    }
    RachaSumadaDialog(if (enPausa) null else state.rachaSumada, onDismiss = viewModel::dismissRachaSumada)
    PuntosGanadosDialog(if (enPausa) null else state.puntosGanados, onDismiss = viewModel::dismissPuntos)
    LogroDesbloqueadoSheet(
        logro = if (enPausa) null else state.logroCelebrado,
        onDismiss = viewModel::dismissCelebration,
        onVerLogros = {
            viewModel.dismissCelebration()
            onVerLogros()
        }
    )
    LogrosResumenSheet(
        logros = if (enPausa) null else state.logrosResumen,
        onDismiss = viewModel::dismissCelebration,
        onVerLogros = {
            viewModel.dismissCelebration()
            onVerLogros()
        }
    )
}

/** Tiempo que dura visible cada felicitación de puntos o medalla (HAL-18). */
private const val CELEBRACION_MS = 3000L

/** HAL-12: VitaPuntos en un modal con el estilo del de racha (Vitta, confeti, "¡Genial!"). */
@Composable
internal fun PuntosGanadosDialog(aviso: PuntosGanadosUi?, onDismiss: () -> Unit) {
    var last by remember { mutableStateOf<PuntosGanadosUi?>(null) }
    LaunchedEffect(aviso) {
        if (aviso != null) {
            last = aviso
            kotlinx.coroutines.delay(CELEBRACION_MS)
            onDismiss()
        }
    }
    val current = aviso ?: last ?: return
    com.vitta.app.ui.components.feedback.VittaPointsDialog(
        visible = aviso != null,
        habitName = current.habito,
        points = current.puntos,
        onDismiss = onDismiss,
        mascot = { VittaMascot(state = MascotState.GoodHabits, size = MascotSize.Medium) }
    )
}

/** HAL-18: más de dos medallas juntas → un solo aviso "Ganaste N logros" con "Ver mis logros". */
@Composable
internal fun LogrosResumenSheet(logros: List<LogroDto>?, onDismiss: () -> Unit, onVerLogros: () -> Unit) {
    var last by remember { mutableStateOf<List<LogroDto>?>(null) }
    LaunchedEffect(logros) {
        if (logros != null) {
            last = logros
            kotlinx.coroutines.delay(CELEBRACION_MS)
            onDismiss()
        }
    }
    val current = logros ?: last ?: return
    val bono = current.sumOf { it.puntos_bonus }
    VittaRewardSheet(
        visible = logros != null,
        area = "LOGROS DESBLOQUEADOS",
        title = "Ganaste ${current.size} logros",
        description = current.joinToString(" · ") { it.nombre },
        onDismiss = onDismiss,
        mascot = {
            VittaInsigniaBadge(insignia = insigniaPara(current.first().icono.orEmpty()), unlocked = true, size = 96.dp)
        }
    ) {
        if (bono > 0) {
            Row(
                Modifier
                    .background(Mk.AmberTile, VittaShapes.pill)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+$bono", style = caprasimo(15f, 1.2f), color = Mk.AmberInk)
                Spacer(Modifier.width(7.dp))
                Text("VitaPuntos", style = figtree(600, 13f), color = Mk.AmberDeep)
            }
        }
        Spacer(Modifier.height(VittaSpacing.Sm))
        VittaPrimaryButton(text = "Ver mis logros", onClick = onVerLogros, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * HAL-10: al tocar el contador ("Vas 0 de 6000 minutos") se escribe la
 * cantidad que se hizo. Debe ser positiva y no pasar el máximo por registro
 * de la unidad; los botones rápidos (+5, +1k…) siguen como atajo.
 */
@Composable
internal fun HabitAmountDialog(card: HabitCardUiState, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var texto by rememberSaveable(card.habit.id) { mutableStateOf("") }
    val meta = card.metaTarget ?: 0
    val falta = (meta - card.pendingValue).coerceAtLeast(0)
    val maximo = com.vitta.app.ui.common.HabitUnits.maxPorRegistro(card.metaUnidad) ?: meta
    val cantidad = texto.toIntOrNull()
    val error = when {
        texto.isEmpty() -> null
        cantidad == null || cantidad <= 0 -> "Escribe una cantidad mayor que 0"
        cantidad > maximo -> "El máximo por registro es $maximo ${card.metaUnidad}"
        else -> null
    }
    val valido = cantidad != null && cantidad > 0 && cantidad <= maximo
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VittaColorRoles.surface,
        title = {
            Text("¿Cuánto hiciste? · ${card.habit.nombre}", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
        },
        text = {
            Column {
                Text(
                    "Llevas ${card.pendingValue} de $meta ${card.metaUnidad}" + if (falta > 0) " · te faltan $falta." else ".",
                    style = VittaTextStyles.bodySmall,
                    color = VittaColorRoles.textSecondary
                )
                Spacer(Modifier.height(VittaSpacing.Md))
                OutlinedTextField(
                    value = texto,
                    onValueChange = { nuevo -> texto = nuevo.filter { it.isDigit() }.take(6) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null,
                    label = { Text("Cantidad en ${card.metaUnidad}", style = VittaTextStyles.bodySmall) },
                    placeholder = { Text("Ej: ${minOf(45, maximo.coerceAtLeast(1))}", style = VittaTextStyles.body, color = VittaColorRoles.textMuted) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    textStyle = VittaTextStyles.body.copy(color = VittaColorRoles.textPrimary),
                    shape = VittaShapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VittaColorRoles.primaryPressed,
                        unfocusedBorderColor = VittaColorRoles.border,
                        cursorColor = VittaColorRoles.primaryPressed
                    )
                )
                Text(
                    error ?: "Se suma a lo que ya registraste hoy.",
                    style = VittaTextStyles.caption,
                    color = if (error != null) VittaColors.Error else VittaColorRoles.textMuted,
                    modifier = Modifier.padding(top = VittaSpacing.Xxs)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { cantidad?.let(onConfirm) }, enabled = valido) {
                Text("Registrar", style = VittaTextStyles.button, color = if (valido) VittaColorRoles.primaryPressed else VittaColorRoles.textDisabled)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", style = VittaTextStyles.button, color = VittaColorRoles.textSecondary)
            }
        }
    )
}

/**
 * Nota del día de un hábito: qué leíste, cómo te sentiste… Se guarda en el
 * registro de hoy (`notas`) y se ve luego en la bitácora y en Progreso.
 *
 * Con [motivacion] = true se abre sola al completar el hábito: es opcional,
 * anima a describir lo que se hizo y el hábito cuenta igual si no la escribe.
 */
@Composable
internal fun HabitNoteDialog(
    card: HabitCardUiState,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
    motivacion: Boolean = false
) {
    var texto by rememberSaveable(card.habit.id) { mutableStateOf(card.nota.orEmpty()) }
    val teniaNota = !card.nota.isNullOrBlank()
    // HAL-11: mínimo 5 caracteres con letras o números; vacía solo sirve para borrar una nota existente.
    val borrar = texto.isBlank() && teniaNota
    val errorNota = if (texto.isBlank()) null else com.vitta.app.ui.common.VittaValidation.nota(texto)
    val puedeGuardar = borrar || (texto.isNotBlank() && errorNota == null)
    val r28 = RoundedCornerShape(28.dp)
    val pill = RoundedCornerShape(percent = 50)

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(r28)
                .background(Mk.Paper)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            // Cabecera con Vitta lector, como la bitácora.
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (motivacion) Mk.OliveTile else Mk.SandSoft)
                    .padding(start = 20.dp, end = 12.dp, top = 16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(Modifier.weight(1f).padding(bottom = 16.dp)) {
                    Text(
                        if (motivacion) "¡HÁBITO COMPLETADO!" else "NOTA DEL DÍA",
                        style = figtree(700, 11f, 1f, 0.08f),
                        color = if (motivacion) Mk.OliveDeep else Mk.SandText
                    )
                    Text(
                        if (motivacion) "¿Cómo te fue con ${card.habit.nombre}?" else card.habit.nombre,
                        style = caprasimo(20f, 1.15f, -0.015f),
                        color = Mk.Ink,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                VittaMascot(state = MascotState.Reader, size = MascotSize.Small, modifier = Modifier.offset(y = 4.dp))
            }
            Column(Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                if (motivacion) {
                    Text(
                        "Describe en pocas palabras lo que hiciste: así compruebas que de verdad lo cumpliste y lo verás en tu bitácora. " +
                            "Es opcional, tu hábito ya cuenta.",
                        style = figtree(500, 13.5f, 1.45f),
                        color = Mk.Body
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(notePrompt(card.habit.nombre), style = figtree(600, 13f, 1.4f), color = Mk.OliveDeep)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = texto,
                    onValueChange = { if (it.length <= NOTA_MAX) texto = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                    placeholder = { Text("Escribe aquí…", style = figtree(500, 14f), color = Mk.Muted) },
                    textStyle = figtree(500, 14.5f, 1.45f).copy(color = Mk.Ink),
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Mk.Olive,
                        unfocusedBorderColor = Mk.Dashed,
                        cursorColor = Mk.OliveDeep,
                        focusedContainerColor = Mk.Screen,
                        unfocusedContainerColor = Mk.Screen
                    )
                )
                Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    Text(errorNota ?: "", style = figtree(600, 12f), color = Mk.Danger, modifier = Modifier.weight(1f))
                    // Contador en rojo mientras no se cumple el mínimo.
                    Text(
                        "${texto.trim().length}/$NOTA_MAX",
                        style = figtree(600, 12f),
                        color = if (errorNota != null) Mk.Danger else Mk.Muted,
                        textAlign = TextAlign.End
                    )
                }
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(pill)
                        .background(
                            when {
                                borrar -> Mk.Danger
                                puedeGuardar -> Mk.OliveDeep
                                else -> Mk.Track
                            }
                        )
                        .clickable(enabled = puedeGuardar, role = androidx.compose.ui.semantics.Role.Button) { onSave(texto) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (borrar) "Borrar nota" else "Guardar nota",
                        style = caprasimo(16f, 1.2f),
                        color = if (puedeGuardar) Mk.NightText else Mk.Muted
                    )
                }
                Box(
                    Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clip(pill)
                        .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (motivacion) "Ahora no" else "Cancelar", style = figtree(700, 14.5f), color = Mk.SandText)
                }
            }
        }
    }
}


/** Pregunta guía según el hábito, para que escribir la nota sea fácil. */
private fun notePrompt(nombre: String): String {
    val n = nombre.lowercase()
    return when {
        "leer" in n || "lectura" in n -> "¿Qué leíste hoy? Un libro, un capítulo o una idea que te quedó."
        "yoga" in n || "ejercicio" in n || "caminar" in n || "correr" in n -> "¿Qué rutina hiciste y cómo se sintió tu cuerpo?"
        "meditar" in n -> "¿Cómo te sentiste antes y después de meditar?"
        "dormir" in n -> "¿Cómo dormiste? ¿Te despertaste descansado?"
        "agua" in n -> "¿Te costó llegar a la meta? ¿Qué te ayudó?"
        else -> "¿Cómo te fue hoy con este hábito? Anota lo que quieras recordar."
    }
}

@Composable
internal fun AddHabitButton(onAddHabit: () -> Unit) {
    TextButton(
        onClick = onAddHabit,
        modifier = Modifier.heightIn(min = 48.dp),
        colors = ButtonDefaults.textButtonColors(contentColor = VittaColorRoles.primaryPressed)
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(VittaSpacing.Xxs))
        Text("Nuevo hábito", style = VittaTextStyles.button)
    }
}

/** Confirmación antes de desmarcar: borrar el registro también quita sus VitaPuntos. */
@Composable
internal fun UndoRecordDialog(habitName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = VittaColorRoles.surface,
        title = { Text("¿Desmarcar \"$habitName\"?", style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary) },
        text = {
            Text(
                "Se borrará el registro de hoy y los 10 VitaPuntos que te dio. Tu racha se recalcula.",
                style = VittaTextStyles.body,
                color = VittaColorRoles.textSecondary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Desmarcar", style = VittaTextStyles.button, color = VittaColors.Error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", style = VittaTextStyles.button, color = VittaColorRoles.textSecondary)
            }
        }
    )
}

/**
 * Modal "¡Felicitaciones!" al sumar un nuevo día de racha ([VittaStreakDialog]),
 * con la mascota de Vitta y la racha real del usuario. Va como hijo del Box
 * de la pantalla para que el scrim solo cubra esa pantalla.
 */
@Composable
internal fun RachaSumadaDialog(aviso: RachaSumadaUi?, onDismiss: () -> Unit) {
    // Conserva el último valor mientras dura la animación de salida.
    var last by remember { mutableStateOf<RachaSumadaUi?>(null) }
    LaunchedEffect(aviso) { if (aviso != null) last = aviso }
    val current = aviso ?: last ?: return
    VittaStreakDialog(
        visible = aviso != null,
        days = current.dias,
        bonusPoints = current.puntos,
        onDismiss = onDismiss,
        mascot = { VittaMascot(state = MascotState.Strong, size = MascotSize.Medium) }
    )
}

/**
 * Celebración de una medalla nueva ("Logro desbloqueado" del mockup), con el
 * [VittaRewardSheet] existente, la insignia SVG del logro y su bono real.
 */
@Composable
internal fun LogroDesbloqueadoSheet(logro: LogroDto?, onDismiss: () -> Unit, onVerLogros: () -> Unit) {
    // Conserva el último logro mientras dura la animación de salida.
    var last by remember { mutableStateOf<LogroDto?>(null) }
    LaunchedEffect(logro) {
        if (logro != null) {
            last = logro
            // HAL-18: se cierra sola a los 3 s (o antes con la X) y pasa a la siguiente.
            kotlinx.coroutines.delay(CELEBRACION_MS)
            onDismiss()
        }
    }
    val current = logro ?: last ?: return

    VittaRewardSheet(
        visible = logro != null,
        area = "LOGRO DESBLOQUEADO",
        title = current.nombre,
        description = current.descripcion.ifBlank { current.condicion },
        onDismiss = onDismiss,
        mascot = {
            VittaInsigniaBadge(
                insignia = insigniaPara(current.icono.orEmpty()),
                unlocked = true,
                size = 96.dp
            )
        }
    ) {
        if (current.puntos_bonus > 0) {
            Row(
                Modifier
                    .background(Mk.AmberTile, VittaShapes.pill)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("+${current.puntos_bonus}", style = caprasimo(15f, 1.2f), color = Mk.AmberInk)
                Spacer(Modifier.width(7.dp))
                Text("VitaPuntos", style = figtree(600, 13f), color = Mk.AmberDeep)
            }
        }
        Spacer(Modifier.height(VittaSpacing.Sm))
        VittaPrimaryButton(text = "Ver mis logros", onClick = onVerLogros, modifier = Modifier.fillMaxWidth())
    }
}

/**
 * "Racha terminada" del mockup (`perdida`): fondo #EFE6D6, radio 30, Vitta de
 * la tarde, récord histórico y "Empezar una racha nueva hoy". Datos reales:
 * la racha cortada, el día en que cerró y el récord salen del backend.
 */
@Composable
private fun RachaTerminadaCard(racha: RachaPerdidaUi, onEmpezar: () -> Unit) {
    val ayer = LocalDate.now(java.time.ZoneId.systemDefault()).minusDays(1)
    val cuando = if (racha.cerroEl == ayer) "ayer"
    else "el " + racha.cerroEl.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es", "ES")))
    val dias = if (racha.dias == 1) "1 día" else "${racha.dias} días"
    val gris = remember {
        androidx.compose.ui.graphics.ColorFilter.colorMatrix(
            androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0.25f) }
        )
    }

    Column(
        Modifier
            .padding(top = 4.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(Mk.Sand)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f).padding(bottom = 4.dp)) {
                Text("RACHA TERMINADA", style = figtree(600, 11f, 1f, 0.09f), color = Mk.SandText)
                Text(
                    "Tu racha de $dias cerró $cuando",
                    style = caprasimo(22f, 1.15f, -0.015f),
                    color = Mk.Ink,
                    modifier = Modifier.padding(top = 9.dp).semantics { heading() }
                )
                Text(
                    "Tus logros y tus VitaPuntos siguen intactos. Hoy puedes empezar una nueva.",
                    style = figtree(500, 13.5f, 1.45f),
                    color = Mk.Body,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            // <img vitta width:98px;margin:0 -2px -12px 0> (misma ilustración que "Dormir · esta noche").
            VittaMascot(
                state = MascotState.Evening,
                size = MascotSize.Medium,
                modifier = Modifier.offset(x = 2.dp, y = 12.dp)
            )
        }

        Row(
            Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
                .background(Mk.Screen, RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            coil.compose.AsyncImage(
                model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                    .data("file:///android_asset/" + VittaIconAssets.streakFlame)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                alpha = 0.45f,
                colorFilter = gris
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Tu récord histórico", style = figtree(600, 13f), color = Mk.Body)
                Text(
                    "${racha.record} ${if (racha.record == 1) "día seguido" else "días seguidos"}" +
                        (racha.recordMes?.let { ", en $it" } ?: ""),
                    style = figtree(500, 12.5f),
                    color = Mk.Muted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Box(
            Modifier
                .padding(top = 12.dp)
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clip(VittaShapes.pill)
                .background(Mk.OliveDeep)
                .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onEmpezar),
            contentAlignment = Alignment.Center
        ) {
            Text("Empezar una racha nueva hoy", style = caprasimo(16f, 1.2f), color = Mk.NightText)
        }
    }
}

@Composable
private fun HeaderBlock(userName: String, level: Int?, onOpenNotifications: () -> Unit) {
    val today = remember {
        val locale = Locale("es", "ES")
        val date = LocalDate.now()
        val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
        val formatted = date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", locale))
        "${dayName.replaceFirstChar { it.uppercase() }} $formatted"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = VittaSpacing.Xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaLogo(size = 28.dp)
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column(Modifier.weight(1f)) {
            Text(today, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
            Text(
                if (userName.isBlank()) "Hola" else "Hola, $userName",
                style = VittaTextStyles.heading,
                color = VittaColorRoles.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
        }
        if (level != null) LevelPill(level = level)
        Spacer(Modifier.width(VittaSpacing.Xs))
        // HAL-20: acceso a recordatorios y al historial de notificaciones.
        androidx.compose.material3.IconButton(onClick = onOpenNotifications) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = "Notificaciones y recordatorios",
                tint = VittaColorRoles.textPrimary
            )
        }
    }
}

@Composable
private fun LevelPill(level: Int) {
    Row(
        modifier = Modifier
            .background(VittaColorRoles.primaryStrong, VittaShapes.pill)
            .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).background(VittaColors.GoldAccent, CircleShape))
        Spacer(Modifier.width(VittaSpacing.Xs))
        Text("Nivel $level", style = VittaTextStyles.caption, color = VittaColorRoles.onPrimary)
    }
}

@Composable
private fun HeroCard(
    doneCount: Int,
    totalCount: Int,
    racha: Int,
    points: Int?,
    levelName: String?,
    pointsToNext: Int?,
    levelProgress: Float
) {
    val sinHabitosHoy = totalCount == 0
    val empezando = doneCount == 0
    val completo = totalCount > 0 && doneCount >= totalCount

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(VittaShapes.extraLarge)
            .background(VittaColorRoles.surface)
    ) {
        // Burbuja decorativa; el clip de arriba la recorta dentro de la tarjeta.
        Box(
            Modifier
                .size(140.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceVariant)
        )

        Column(modifier = Modifier.padding(VittaSpacing.Xl)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            sinHabitosHoy -> "DÍA LIBRE"
                            completo -> "¡DÍA COMPLETO!"
                            empezando -> "TU DÍA EMPIEZA AQUÍ"
                            else -> "VAS BIEN"
                        },
                        style = VittaTextStyles.label,
                        color = VittaColorRoles.primaryPressed
                    )
                    Spacer(Modifier.height(VittaSpacing.Xs))
                    Text(
                        when {
                            sinHabitosHoy -> "Nada programado hoy"
                            completo -> "Vitta está orgulloso de ti"
                            empezando -> "Vitta te está esperando"
                            else -> "$doneCount de $totalCount hábitos listos"
                        },
                        style = VittaTextStyles.displayLarge,
                        color = VittaColorRoles.textPrimary
                    )
                    Spacer(Modifier.height(VittaSpacing.Sm))
                    Text(
                        when {
                            sinHabitosHoy -> "Puedes registrar hábitos de otros días si quieres."
                            completo -> "Registraste todo lo de hoy. ¡Buen trabajo!"
                            empezando -> "$totalCount hábitos listos para registrar. Empieza por el más fácil."
                            else -> "Vitta ya se siente mejor. Queda poco para cerrar el día."
                        },
                        style = VittaTextStyles.body,
                        color = VittaColorRoles.textSecondary
                    )
                }
                Spacer(Modifier.width(VittaSpacing.Sm))
                VittaMascot(
                    state = if (empezando) MascotState.EmptyState else MascotState.GoodHabits,
                    size = MascotSize.Medium
                )
            }

            Spacer(Modifier.height(VittaSpacing.Lg))

            Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
                // La llama de racha es la ilustración propia de Vitta (no de Icons8).
                HeroStat(
                    icon = { StreakFlame(32.dp) },
                    value = "$racha",
                    label = if (racha == 1) "día de racha" else "días de racha",
                    modifier = Modifier.weight(1f)
                )
                HeroStat(
                    icon = { StatIcon(R.drawable.ic8_trophy) },
                    value = points?.let(::formatPoints) ?: "—",
                    label = "VitaPuntos",
                    modifier = Modifier.weight(1f)
                )
            }

            if (levelName != null) {
                Spacer(Modifier.height(VittaSpacing.Md))
                LevelProgress(levelName = levelName, pointsToNext = pointsToNext, progress = levelProgress)
            }
        }
    }
}

@Composable
private fun HeroStat(icon: @Composable () -> Unit, value: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(VittaColorRoles.surfaceVariant, VittaShapes.medium)
            .padding(VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column {
            Text(value, style = VittaTextStyles.heading, color = VittaColorRoles.textPrimary)
            Text(label, style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
        }
    }
}

@Composable
private fun StatIcon(icon: Int) {
    Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(32.dp))
}

/** Llama de racha creada para Vitta (`assets/vitta/icons/icon_streak_flame.svg`). Decorativa. */
@Composable
private fun StreakFlame(size: androidx.compose.ui.unit.Dp) {
    VittaAssetImage(assetPath = VittaIconAssets.streakFlame, contentDescription = null, size = size)
}

@Composable
private fun LevelProgress(levelName: String, pointsToNext: Int?, progress: Float) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Nivel: $levelName", style = VittaTextStyles.body, color = VittaColorRoles.textSecondary)
            Text(
                if (pointsToNext == null) "Nivel máximo" else "Faltan ${formatPoints(pointsToNext)} pts",
                style = VittaTextStyles.bodySmall,
                color = VittaColorRoles.textMuted
            )
        }
        Spacer(Modifier.height(VittaSpacing.Xs))
        // Amarillo de marca: los puntos y el nivel son recompensas.
        ProgressBar(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            fill = VittaColors.GoldAccent,
            track = VittaColors.SurfaceVariant
        )
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    fill: androidx.compose.ui.graphics.Color = VittaColors.GreenPrimary,
    track: androidx.compose.ui.graphics.Color = VittaColors.GreenTrack
) {
    Box(
        modifier
            .height(8.dp)
            .clip(CircleShape)
            .background(track)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(fill, CircleShape)
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String? = null,
    trailing: @Composable () -> Unit = {}
) {
    Column(Modifier.padding(top = VittaSpacing.Sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = VittaTextStyles.heading,
                color = VittaColorRoles.textPrimary,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() }
            )
            trailing()
        }
        if (subtitle != null) {
            Text(subtitle, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        }
    }
}

@Composable
private fun EmptyHabits(onAddHabit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.extraLarge)
            .border(1.dp, VittaColorRoles.border, VittaShapes.extraLarge)
            .padding(VittaSpacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VittaMascot(state = MascotState.EmptyState, size = MascotSize.Medium)
        Spacer(Modifier.height(VittaSpacing.Lg))
        Text(
            "Aún no tienes hábitos",
            style = VittaTextStyles.heading,
            color = VittaColorRoles.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(VittaSpacing.Xs))
        Text(
            "Elige uno sencillo para empezar. Con uno basta.",
            style = VittaTextStyles.body,
            color = VittaColorRoles.textSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(VittaSpacing.Lg))
        VittaPrimaryButton(text = "Crear mi primer hábito", icon = Icons.Filled.Add, onClick = onAddHabit)
    }
}

@Composable
internal fun HabitDashboardCard(
    card: HabitCardUiState,
    viewModel: ChecklistViewModel,
    onOpen: () -> Unit,
    onUndoRequest: () -> Unit,
    onNoteRequest: () -> Unit,
    onAmountRequest: () -> Unit
) {
    val puedeEscribirCantidad = !card.doneToday && card.metaTarget != null && !card.isSubmitting
    val subtitle = when {
        card.doneToday -> "Registrado hoy"
        card.esAgua && card.metaTarget != null -> "Vas ${card.pendingValue} de ${card.metaTarget} vasos"
        card.metaTarget != null -> "Vas ${card.pendingValue} de ${card.metaTarget} ${card.metaUnidad}"
        else -> card.habit.meta
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (card.doneToday) VittaColorRoles.surfaceTint else VittaColorRoles.surface,
                VittaShapes.large
            )
            .border(
                1.dp,
                if (card.doneToday) VittaColors.GreenPale else VittaColorRoles.border,
                VittaShapes.large
            )
            .padding(VittaSpacing.Lg)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VittaHabitIconBubble(
                icon = card.icon,
                bubbleSize = 52.dp,
                containerColor = if (card.doneToday) VittaColorRoles.surface else VittaColorRoles.surfaceVariant
            )
            Spacer(Modifier.width(VittaSpacing.Md))
            // HAL-09: tocar el nombre abre el avance del hábito (la edición está dentro).
            Column(Modifier.weight(1f)) {
                Text(
                    card.habit.nombre,
                    style = VittaTextStyles.title,
                    color = VittaColorRoles.textPrimary,
                    modifier = Modifier
                        .clip(VittaShapes.small)
                        .clickable(onClickLabel = "Ver avance", onClick = onOpen)
                )
                Spacer(Modifier.height(VittaSpacing.Xxs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // HAL-10: tocar el contador abre el campo para escribir la cantidad.
                    Row(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(VittaShapes.small)
                            .then(
                                if (puedeEscribirCantidad) Modifier.clickable(onClickLabel = "Escribir cantidad", onClick = onAmountRequest)
                                else Modifier.clickable(onClickLabel = "Ver avance", onClick = onOpen)
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            subtitle,
                            style = VittaTextStyles.bodySmall,
                            color = if (puedeEscribirCantidad) VittaColorRoles.primaryPressed else VittaColorRoles.textSecondary,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (puedeEscribirCantidad) {
                            Spacer(Modifier.width(VittaSpacing.Xxs))
                            Icon(
                                androidx.compose.material.icons.Icons.Outlined.EditNote,
                                contentDescription = null,
                                tint = VittaColorRoles.primaryPressed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    if (card.streakActual > 0) {
                        Spacer(Modifier.width(VittaSpacing.Sm))
                        StreakChip(days = card.streakActual)
                    }
                }
            }
            Spacer(Modifier.width(VittaSpacing.Sm))
            TrailingAction(card = card, viewModel = viewModel, onUndoRequest = onUndoRequest)
        }

        if (!card.doneToday && card.metaTarget != null) {
            Spacer(Modifier.height(VittaSpacing.Md))
            if (card.esAgua) {
                VittaWaterTracker(
                    filled = card.pendingValue,
                    total = card.metaTarget,
                    onGlassClick = { index -> viewModel.onGlassTap(card.habit.id, index) }
                )
            } else {
                ProgressBar(
                    progress = card.pendingValue.toFloat() / card.metaTarget,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // La nota aparece cuando ya hiciste algo hoy (o ya tienes una), para no recargar la tarjeta.
        if (card.doneToday || card.savedValue > 0 || card.nota != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            HabitNoteRow(nota = card.nota, saving = card.isSavingNote, onClick = onNoteRequest)
        }

        if (card.errorMessage != null) {
            Spacer(Modifier.height(VittaSpacing.Sm))
            Text(card.errorMessage, style = VittaTextStyles.bodySmall, color = VittaColors.Error)
        }
    }
}

/**
 * "Agregar nota del día" o la nota de hoy ya guardada: tarjeta con el estilo
 * de la bitácora (franja oliva, Vitta lector) que se amplía con "Ver más" y
 * se edita con el lápiz.
 */
@Composable
internal fun HabitNoteRow(nota: String?, saving: Boolean, onClick: () -> Unit) {
    if (nota == null || saving) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(VittaShapes.small)
                .clickable(enabled = !saving, onClickLabel = "Agregar nota", onClick = onClick)
                .heightIn(min = 40.dp)
                .padding(horizontal = VittaSpacing.Sm, vertical = VittaSpacing.Xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.EditNote, contentDescription = null, tint = VittaColorRoles.primaryPressed, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(VittaSpacing.Xs))
            Text(
                if (saving) "Guardando nota…" else "Agregar nota del día · cuenta cómo te fue",
                style = VittaTextStyles.bodySmall,
                color = if (saving) VittaColorRoles.textMuted else VittaColorRoles.primaryPressed
            )
        }
        return
    }
    var ampliada by rememberSaveable(nota) { mutableStateOf(false) }
    val r18 = RoundedCornerShape(18.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(r18)
            .background(Mk.Paper)
            .border(1.dp, Mk.OliveLight, r18)
            .height(IntrinsicSize.Min)
    ) {
        Box(Modifier.width(5.dp).fillMaxHeight().background(Mk.Olive))
        Column(
            Modifier
                .weight(1f)
                .clickable(onClickLabel = if (ampliada) "Ver menos" else "Ver la nota completa") { ampliada = !ampliada }
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Outlined.StickyNote2, contentDescription = null, tint = Mk.OliveDeep, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("TU NOTA DE HOY", style = figtree(700, 10.5f, 1f, 0.08f), color = Mk.OliveDeep)
            }
            Text(
                "“$nota”",
                style = figtree(500, 13.5f, 1.45f).copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = Mk.Ink,
                maxLines = if (ampliada) Int.MAX_VALUE else 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp).animateContentSize()
            )
            if (nota.length > 70 || nota.contains('\n')) {
                Text(
                    if (ampliada) "Ver menos ▴" else "Ver más ▾",
                    style = figtree(700, 12f),
                    color = Mk.OliveDeep,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        androidx.compose.material3.IconButton(onClick = onClick) {
            Icon(Icons.Outlined.Edit, contentDescription = "Editar nota", tint = Mk.OliveDeep, modifier = Modifier.size(18.dp))
        }
    }
}


@Composable
private fun TrailingAction(card: HabitCardUiState, viewModel: ChecklistViewModel, onUndoRequest: () -> Unit) {
    when {
        card.isSubmitting -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = VittaColorRoles.primary
            )
        }

        card.doneToday -> {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(VittaColorRoles.primaryStrong)
                    .clickable(onClickLabel = "Desmarcar", onClick = onUndoRequest),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = "Completado. Toca para desmarcar",
                    tint = VittaColorRoles.onPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        card.esAgua && card.metaTarget != null -> {
            OutlinedPill(text = "Todos", onClick = { viewModel.onMarkAll(card.habit.id) })
        }

        card.metaTarget != null -> {
            val step = when {
                card.metaUnidad.contains("paso") -> 1000
                card.metaUnidad.contains("minuto") -> 5
                else -> 1
            }
            OutlinedPill(
                text = if (step == 1000) "+1k" else "+$step",
                onClick = { viewModel.onQuickAdd(card.habit.id) }
            )
        }

        else -> {
            OutlinedPill(text = "Listo", onClick = { viewModel.onComplete(card.habit.id) })
        }
    }
}

@Composable
private fun StreakChip(days: Int) {
    Row(
        modifier = Modifier
            .background(VittaColorRoles.surfaceVariant, VittaShapes.pill)
            .padding(horizontal = VittaSpacing.Sm, vertical = VittaSpacing.Xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StreakFlame(14.dp)
        Spacer(Modifier.width(VittaSpacing.Xxs))
        Text("$days", style = VittaTextStyles.caption, color = VittaColorRoles.textPrimary)
    }
}

@Composable
private fun OutlinedPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(VittaShapes.pill)
            .border(1.dp, VittaColorRoles.border, VittaShapes.pill)
            .clickable(onClick = onClick)
            .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = VittaTextStyles.button, color = VittaColorRoles.textPrimary)
    }
}

/** 1240 -> "1.240" (separador de miles con punto, como en el prototipo). */
internal fun formatPoints(points: Int): String {
    val symbols = DecimalFormatSymbols(Locale("es", "ES")).apply { groupingSeparator = '.' }
    return DecimalFormat("#,###", symbols).format(points)
}

@Preview(showBackground = true)
@Composable
private fun HeroCardPreview() {
    HeroCard(
        doneCount = 0,
        totalCount = 4,
        racha = 12,
        points = 1240,
        levelName = "Impulsor",
        pointsToNext = 760,
        levelProgress = 0.3f
    )
}

@Preview(showBackground = true)
@Composable
private fun PillsPreview() {
    Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)) {
        StreakChip(12)
        OutlinedPill("Todos") {}
        OutlinedPill("Listo") {}
        LevelPill(5)
    }
}