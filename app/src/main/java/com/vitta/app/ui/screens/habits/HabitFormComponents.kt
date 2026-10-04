package com.vitta.app.ui.screens.habits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.buttons.VittaBackButton
import com.vitta.app.ui.components.icons.HabitIcon
import com.vitta.app.ui.components.icons.HabitIconCategory
import com.vitta.app.ui.components.icons.HabitIcons
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Bloques reutilizables del formulario de hábitos (crear, configurar
 * predefinidos y editar), para que los tres se vean y se comporten igual.
 */

// ---------------------------------------------------------------------------
// Estructura de pantalla
// ---------------------------------------------------------------------------

/** Barra superior: botón de regreso + título (+ subtítulo opcional). */
@Composable
fun HabitScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VittaBackButton(onClick = onBack)
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = VittaTextStyles.heading,
                color = VittaColorRoles.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
            if (subtitle != null) {
                Text(subtitle, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
            }
        }
    }
}

/**
 * Una sección numerada del formulario ("1 · Nombre"). El número guía el
 * flujo nombre → icono → meta → frecuencia → recordatorio.
 */
@Composable
fun HabitFormSection(
    title: String,
    modifier: Modifier = Modifier,
    step: Int? = null,
    supportingText: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (step != null) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(VittaColorRoles.surfaceTint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$step", style = VittaTextStyles.caption, color = VittaColorRoles.primaryPressed)
                }
                Spacer(Modifier.width(VittaSpacing.Sm))
            }
            Text(
                title,
                style = VittaTextStyles.subtitle.copy(fontSize = VittaTextStyles.title.fontSize),
                color = VittaColorRoles.textPrimary,
                modifier = Modifier.semantics { heading() }
            )
        }
        if (supportingText != null) {
            Spacer(Modifier.height(VittaSpacing.Xxs))
            Text(supportingText, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textMuted)
        }
        Spacer(Modifier.height(VittaSpacing.Md))
        content()
    }
}

/** Nota informativa discreta (icono "i" + texto). */
@Composable
fun HabitInfoNote(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surfaceMuted, VittaShapes.small)
            .padding(VittaSpacing.Md),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info,
            contentDescription = null,
            tint = VittaColorRoles.textSecondary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(VittaSpacing.Sm))
        Text(text, style = VittaTextStyles.bodySmall, color = VittaColorRoles.textSecondary)
    }
}

// ---------------------------------------------------------------------------
// Iconos
// ---------------------------------------------------------------------------

/**
 * Casilla cuadrada de icono. Tamaño dado por quien la usa (normalmente
 * `weight(1f)` + `aspectRatio(1f)`), el icono siempre centrado y al mismo
 * porcentaje del cuadro. Seleccionada: borde verde grueso + fondo tintado + check.
 */
@Composable
fun HabitIconTile(
    icon: HabitIcon,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(VittaShapes.medium)
            .background(if (selected) VittaColorRoles.surfaceTint else VittaColorRoles.surface)
            .border(
                BorderStroke(if (selected) 2.5.dp else 1.dp, if (selected) VittaColorRoles.primary else VittaColorRoles.border),
                VittaShapes.medium
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = icon.label },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(icon.drawable),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(0.58f),
            contentScale = ContentScale.Fit
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(18.dp)
                    .background(VittaColorRoles.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = VittaColorRoles.onPrimary, modifier = Modifier.size(12.dp))
            }
        }
    }
}

/**
 * Cuadrícula de iconos frecuentes (4 por fila) + botón "+" que abre el
 * catálogo completo. Las filas usan pesos iguales, así las casillas tienen
 * el mismo tamaño y separación en cualquier ancho de pantalla.
 */
@Composable
fun HabitIconPicker(
    selected: HabitIcon,
    onSelect: (HabitIcon) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4
) {
    var showCatalog by rememberSaveable { mutableStateOf(false) }

    // El icono elegido siempre está visible: si viene del catálogo ocupa el
    // primer lugar; si ya es uno frecuente, la cuadrícula no se reordena.
    val featured = remember(selected) {
        val base = HabitIcons.featured.take(columns * 3 - 1)
        if (base.any { it.id == selected.id }) base else listOf(selected) + base.dropLast(1)
    }
    val cells: List<HabitIcon?> = featured + null // null = botón "+"

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
        cells.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Md)) {
                row.forEach { icon ->
                    if (icon != null) {
                        HabitIconTile(
                            icon = icon,
                            selected = icon.id == selected.id,
                            onClick = { onSelect(icon) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        MoreIconsTile(onClick = { showCatalog = true }, modifier = Modifier.weight(1f))
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
        Text(
            "Seleccionado: ${selected.label}",
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textSecondary
        )
    }

    if (showCatalog) {
        HabitIconCatalogSheet(
            selected = selected,
            onSelect = {
                onSelect(it)
                showCatalog = false
            },
            onDismiss = { showCatalog = false }
        )
    }
}

@Composable
private fun MoreIconsTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(VittaShapes.medium)
            .background(VittaColorRoles.surface)
            .border(BorderStroke(1.5.dp, VittaColorRoles.primary), VittaShapes.medium)
            .clickable(role = Role.Button, onClickLabel = "Ver todos los iconos", onClick = onClick)
            .semantics { contentDescription = "Ver más iconos" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Add, contentDescription = null, tint = VittaColorRoles.primaryPressed, modifier = Modifier.size(28.dp))
        Text("Más", style = VittaTextStyles.caption, color = VittaColorRoles.primaryPressed)
    }
}

/**
 * Catálogo completo en una hoja inferior: búsqueda, filtro por categoría y
 * cuadrícula perezosa (solo dibuja lo visible) agrupada por categoría.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitIconCatalogSheet(
    selected: HabitIcon,
    onSelect: (HabitIcon) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<String?>(null) }

    val results = remember(query, category) {
        HabitIcons.search(query).filter { category == null || it.category.name == category }
    }
    val groups = remember(results) { HabitIcons.grouped(results) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = VittaColorRoles.background,
        contentColor = VittaColorRoles.textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = VittaSpacing.Lg),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Elige un icono",
                    style = VittaTextStyles.heading,
                    color = VittaColorRoles.textPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { heading() }
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Cerrar catálogo", tint = VittaColorRoles.textPrimary)
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Sm),
                singleLine = true,
                placeholder = { Text("Buscar icono… (yoga, agua, leer)", style = VittaTextStyles.body) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda") } }
                } else null,
                shape = VittaShapes.pill,
                textStyle = VittaTextStyles.body,
                colors = vittaTextFieldColors()
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = VittaSpacing.Lg),
                horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm)
            ) {
                item {
                    VittaFilterChip("Todas", selected = category == null) { category = null }
                }
                items(HabitIconCategory.entries) { c ->
                    VittaFilterChip(c.label, selected = category == c.name) {
                        category = if (category == c.name) null else c.name
                    }
                }
            }

            if (results.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(VittaSpacing.Xxl),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No encontramos iconos para “$query”.",
                        style = VittaTextStyles.subtitle,
                        color = VittaColorRoles.textPrimary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Prueba con otra palabra, por ejemplo “correr” o “salud”.",
                        style = VittaTextStyles.bodySmall,
                        color = VittaColorRoles.textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 76.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(VittaSpacing.Lg),
                    horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Md),
                    verticalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
                ) {
                    groups.forEach { (cat, icons) ->
                        item(key = "header_${cat.name}", span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                cat.label,
                                style = VittaTextStyles.subtitle,
                                color = VittaColorRoles.textSecondary,
                                modifier = Modifier
                                    .padding(top = VittaSpacing.Sm)
                                    .semantics { heading() }
                            )
                        }
                        items(icons, key = { it.id }) { icon ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                HabitIconTile(
                                    icon = icon,
                                    selected = icon.id == selected.id,
                                    onClick = { onSelect(icon) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(Modifier.height(VittaSpacing.Xxs))
                                Text(
                                    icon.label,
                                    style = VittaTextStyles.caption,
                                    color = VittaColorRoles.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VittaFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = VittaTextStyles.bodySmall) },
        modifier = Modifier.heightIn(min = 40.dp),
        shape = VittaShapes.pill,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = VittaColorRoles.surface,
            labelColor = VittaColorRoles.textSecondary,
            selectedContainerColor = VittaColorRoles.primary,
            selectedLabelColor = VittaColorRoles.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = VittaColorRoles.border,
            selectedBorderColor = VittaColorRoles.primary
        )
    )
}

@Composable
fun vittaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = VittaColorRoles.textPrimary,
    unfocusedTextColor = VittaColorRoles.textPrimary,
    errorTextColor = VittaColorRoles.textPrimary,
    focusedContainerColor = VittaColorRoles.surface,
    unfocusedContainerColor = VittaColorRoles.surface,
    errorContainerColor = VittaColorRoles.surface,
    focusedBorderColor = VittaColorRoles.primary,
    unfocusedBorderColor = VittaColorRoles.border,
    errorBorderColor = VittaColors.Error,
    cursorColor = VittaColorRoles.primaryPressed,
    focusedPlaceholderColor = VittaColorRoles.textMuted,
    unfocusedPlaceholderColor = VittaColorRoles.textMuted,
    focusedLeadingIconColor = VittaColorRoles.primaryPressed,
    unfocusedLeadingIconColor = VittaColorRoles.textSecondary,
    focusedTrailingIconColor = VittaColorRoles.textSecondary,
    unfocusedTrailingIconColor = VittaColorRoles.textSecondary
)

// ---------------------------------------------------------------------------
// Meta: cantidad + unidad
// ---------------------------------------------------------------------------

/**
 * Meta separada en dos conceptos: **Cantidad** (número, con − / + o
 * escribiéndolo) y **Unidad** (texto libre con sugerencias). En el backend
 * se guardan juntas en `habitos.meta` como "30 minutos".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HabitGoalInput(
    cantidad: Int,
    onCantidadChange: (Int) -> Unit,
    unidad: String,
    onUnidadChange: (String) -> Unit,
    unitSuggestions: List<String>,
    modifier: Modifier = Modifier,
    step: Int = 1,
    cantidadError: String? = null,
    unidadError: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .border(1.dp, VittaColorRoles.border, VittaShapes.large)
            .padding(VittaSpacing.Lg)
    ) {
        Text("Cantidad", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
        Spacer(Modifier.height(VittaSpacing.Sm))

        // Texto local para permitir borrar el número mientras se escribe.
        var text by remember { mutableStateOf(cantidad.toString()) }
        LaunchedEffect(cantidad) {
            if ((text.toIntOrNull() ?: 0) != cantidad) text = cantidad.toString()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Md)
        ) {
            StepperButton(
                icon = Icons.Filled.Remove,
                description = "Disminuir cantidad",
                enabled = cantidad > 0,
                filled = false,
                onClick = { onCantidadChange((cantidad - step).coerceAtLeast(0)) }
            )
            OutlinedTextField(
                value = text,
                onValueChange = { new ->
                    val digits = new.filter { it.isDigit() }.take(6)
                    text = digits
                    onCantidadChange(digits.toIntOrNull() ?: 0)
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Cantidad" },
                singleLine = true,
                isError = cantidadError != null,
                textStyle = VittaTextStyles.displayLarge.copy(textAlign = TextAlign.Center, color = VittaColorRoles.textPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = VittaShapes.medium,
                colors = vittaTextFieldColors()
            )
            StepperButton(
                icon = Icons.Filled.Add,
                description = "Aumentar cantidad",
                enabled = true,
                filled = true,
                onClick = { onCantidadChange(cantidad + step) }
            )
        }
        if (cantidadError != null) FieldError(cantidadError)

        Spacer(Modifier.height(VittaSpacing.Lg))
        Text("Unidad", style = VittaTextStyles.subtitle, color = VittaColorRoles.textPrimary)
        Spacer(Modifier.height(VittaSpacing.Sm))
        OutlinedTextField(
            value = unidad,
            onValueChange = { onUnidadChange(it.take(30)) },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Unidad" },
            singleLine = true,
            isError = unidadError != null,
            placeholder = { Text("Ej: minutos, vasos, páginas", style = VittaTextStyles.body) },
            textStyle = VittaTextStyles.body.copy(fontSize = VittaTextStyles.subtitle.fontSize),
            shape = VittaShapes.medium,
            colors = vittaTextFieldColors()
        )
        if (unidadError != null) FieldError(unidadError)
        Spacer(Modifier.height(VittaSpacing.Sm))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(VittaSpacing.Xs)
        ) {
            unitSuggestions.forEach { u ->
                VittaFilterChip(u, selected = unidad.trim().equals(u, ignoreCase = true)) { onUnidadChange(u) }
            }
        }

        Spacer(Modifier.height(VittaSpacing.Md))
        Text(
            if (unidad.isBlank()) "Tu meta: $cantidad" else "Tu meta: $cantidad ${unidad.trim()}",
            style = VittaTextStyles.subtitle,
            color = VittaColorRoles.primaryPressed
        )
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    filled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(48.dp)
            .then(if (filled) Modifier else Modifier.border(1.dp, VittaColorRoles.border, CircleShape)),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (filled) VittaColorRoles.primary else VittaColorRoles.surface,
            contentColor = if (filled) VittaColorRoles.onPrimary else VittaColorRoles.textPrimary,
            disabledContainerColor = VittaColorRoles.surfaceMuted,
            disabledContentColor = VittaColorRoles.textDisabled
        )
    ) {
        Icon(icon, contentDescription = description)
    }
}

@Composable
fun FieldError(text: String) {
    Text(
        text,
        style = VittaTextStyles.bodySmall,
        color = VittaColors.Error,
        modifier = Modifier.padding(top = VittaSpacing.Xs)
    )
}

// ---------------------------------------------------------------------------
// Frecuencia
// ---------------------------------------------------------------------------

/** Días en el orden y la abreviatura que ya usa el backend en `frecuencia` ("L,M,X"). */
val WeekDays = listOf(
    "L" to "Lunes", "M" to "Martes", "X" to "Miércoles", "J" to "Jueves",
    "V" to "Viernes", "S" to "Sábado", "D" to "Domingo"
)

@Composable
fun HabitFrequencySelector(
    diasEspecificos: Boolean,
    onModeChange: (Boolean) -> Unit,
    selectedDays: Set<String>,
    onToggleDay: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(VittaColorRoles.surfaceMuted, VittaShapes.pill)
                .padding(4.dp)
        ) {
            listOf("Todos los días" to false, "Días específicos" to true).forEach { (label, value) ->
                val selected = diasEspecificos == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(VittaShapes.pill)
                        .background(if (selected) VittaColorRoles.surface else VittaColorRoles.surfaceMuted)
                        .then(if (selected) Modifier.border(1.dp, VittaColorRoles.primary, VittaShapes.pill) else Modifier)
                        .selectable(selected = selected, role = Role.Tab, onClick = { onModeChange(value) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        style = VittaTextStyles.button,
                        color = if (selected) VittaColorRoles.primaryPressed else VittaColorRoles.textSecondary
                    )
                }
            }
        }

        if (diasEspecificos) {
            Spacer(Modifier.height(VittaSpacing.Md))
            // 7 celdas de igual ancho; cada círculo es cuadrado y como máximo
            // de 48dp, centrado en su celda: nunca se superponen ni se descuadran.
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Xs)) {
                WeekDays.forEach { (code, name) ->
                    val selected = code in selectedDays
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .widthIn(max = 48.dp)
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (selected) VittaColorRoles.primary else VittaColorRoles.surface)
                                .border(1.dp, if (selected) VittaColorRoles.primary else VittaColorRoles.border, CircleShape)
                                .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggleDay(code) })
                                .semantics {
                                    contentDescription = name
                                    stateDescription = if (selected) "seleccionado" else "no seleccionado"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                code,
                                style = VittaTextStyles.subtitle,
                                color = if (selected) VittaColorRoles.onPrimary else VittaColorRoles.textSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            frequencySummary(diasEspecificos, selectedDays),
            style = VittaTextStyles.bodySmall,
            color = VittaColorRoles.textSecondary
        )
        if (error != null) FieldError(error)
    }
}

fun frequencySummary(diasEspecificos: Boolean, selectedDays: Set<String>): String {
    if (!diasEspecificos) return "Se repite todos los días."
    val names = WeekDays.filter { it.first in selectedDays }.map { it.second }
    return when (names.size) {
        0 -> "Elige al menos un día."
        1 -> "Se repite cada ${names[0].lowercase()}."
        else -> "Se repite: " + names.dropLast(1).joinToString(", ") + " y " + names.last() + "."
    }
}

/** Ordena los días como en la semana (L..D) para guardarlos en `frecuencia`. */
fun orderedDays(days: Set<String>): List<String> = WeekDays.map { it.first }.filter { it in days }

// ---------------------------------------------------------------------------
// Recordatorio: hora
// ---------------------------------------------------------------------------

private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale("es", "CO"))

fun formatReminderTime(time: LocalTime): String = time.format(timeFormatter)

/**
 * Campo de hora: se ve claramente como editable (icono, hora grande,
 * "Cambiar" + lápiz) y al tocarlo abre el selector de hora de Material 3.
 */
@Composable
fun HabitReminderTimeField(
    time: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val formatted = formatReminderTime(time)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(VittaShapes.large)
            .background(VittaColorRoles.surface)
            .border(1.dp, VittaColorRoles.border, VittaShapes.large)
            .clickable(role = Role.Button, onClickLabel = "Cambiar hora", onClick = { showPicker = true })
            .padding(VittaSpacing.Lg)
            .semantics(mergeDescendants = true) {
                contentDescription = "Hora del recordatorio: $formatted Toca para cambiarla."
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(VittaColors.SkySoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Alarm, contentDescription = null, tint = VittaColorRoles.reminder)
        }
        Spacer(Modifier.width(VittaSpacing.Md))
        Column(Modifier.weight(1f)) {
            Text("Hora del recordatorio", style = VittaTextStyles.bodySmall, color = VittaColorRoles.textSecondary)
            Text(formatted, style = VittaTextStyles.displayLarge, color = VittaColorRoles.textPrimary)
        }
        Row(
            modifier = Modifier
                .background(VittaColors.SkySoft, VittaShapes.pill)
                .padding(horizontal = VittaSpacing.Md, vertical = VittaSpacing.Sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = null, tint = VittaColorRoles.reminder, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(VittaSpacing.Xxs))
            Text("Cambiar", style = VittaTextStyles.button, color = VittaColorRoles.reminder)
        }
    }

    if (showPicker) {
        VittaTimePickerDialog(
            initial = time,
            onConfirm = {
                onTimeChange(it)
                showPicker = false
            },
            onDismiss = { showPicker = false }
        )
    }
}

/**
 * Diálogo con el TimePicker de Material 3 (reloj) y opción de escribir la
 * hora con el teclado. Formato de 12 horas con a. m. / p. m.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VittaTimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = false
    )
    var keyboardMode by rememberSaveable { mutableStateOf(false) }
    val colors = TimePickerDefaults.colors(
        clockDialColor = VittaColorRoles.surfaceMuted,
        clockDialSelectedContentColor = VittaColorRoles.onPrimary,
        clockDialUnselectedContentColor = VittaColorRoles.textPrimary,
        selectorColor = VittaColorRoles.primary,
        containerColor = VittaColorRoles.surface,
        periodSelectorBorderColor = VittaColorRoles.border,
        periodSelectorSelectedContainerColor = VittaColorRoles.surfaceTint,
        periodSelectorUnselectedContainerColor = VittaColorRoles.surface,
        periodSelectorSelectedContentColor = VittaColorRoles.primaryPressed,
        periodSelectorUnselectedContentColor = VittaColorRoles.textSecondary,
        timeSelectorSelectedContainerColor = VittaColorRoles.surfaceTint,
        timeSelectorUnselectedContainerColor = VittaColorRoles.surfaceMuted,
        timeSelectorSelectedContentColor = VittaColorRoles.primaryPressed,
        timeSelectorUnselectedContentColor = VittaColorRoles.textPrimary
    )

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = VittaShapes.extraLarge,
            color = VittaColorRoles.surface,
            contentColor = VittaColorRoles.textPrimary,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(VittaSpacing.Xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Selecciona la hora",
                    style = VittaTextStyles.title,
                    color = VittaColorRoles.textPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { heading() }
                )
                Text(
                    if (keyboardMode) "Escribe la hora y elige a. m. o p. m."
                    else "Mueve la aguja o toca un número. Luego ajusta los minutos.",
                    style = VittaTextStyles.bodySmall,
                    color = VittaColorRoles.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(VittaSpacing.Lg))

                if (keyboardMode) TimeInput(state = state, colors = colors)
                else TimePicker(state = state, colors = colors)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { keyboardMode = !keyboardMode }) {
                        Icon(
                            if (keyboardMode) Icons.Outlined.Schedule else Icons.Outlined.Keyboard,
                            contentDescription = if (keyboardMode) "Usar el reloj" else "Escribir la hora con el teclado",
                            tint = VittaColorRoles.textSecondary
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = VittaColorRoles.textSecondary)
                    ) { Text("Cancelar", style = VittaTextStyles.button) }
                    TextButton(
                        onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) },
                        colors = ButtonDefaults.textButtonColors(contentColor = VittaColorRoles.primaryPressed)
                    ) { Text("Aceptar", style = VittaTextStyles.button) }
                }
            }
        }
    }
}
