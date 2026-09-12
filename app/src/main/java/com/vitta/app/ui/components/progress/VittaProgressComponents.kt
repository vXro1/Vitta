package com.vitta.app.ui.components.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSizes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * Every achievement badge illustration shipped in `assets/vitta/insignias/`
 * (streak milestones + area milestones from the mockup's `MED_RACHA` /
 * `MED_AREA` tables).
 */
enum class Insignia(internal val assetName: String) {
    Days7("7dias"),
    Days30("30dias"),
    Days50("50dias"),
    Days100("100dias"),
    FirstHabit("primerhabito"),
    Reader("lector"),
    EarlyRiser("madrugador"),
    Steps("pasos"),
    HealthyLife("vidasaludable"),
    Yoga("yoga")
}

/** A single achievement badge, dimmed when [unlocked] is false (matches the mockup's grayscale-locked state). */
@Composable
fun VittaInsigniaBadge(
    insignia: Insignia,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = VittaSizes.BadgeMedium
) {
    VittaAssetImage(
        assetPath = VittaIconAssets.insignia(insignia.assetName),
        contentDescription = null,
        modifier = modifier.then(if (!unlocked) Modifier.alpha(0.4f) else Modifier),
        size = size
    )
}

/** The orange flame "N días de racha" pill used on Home and the streak-lost screen. */
@Composable
fun VittaStreakBadge(
    days: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(VittaColorRoles.surfaceMuted, VittaShapes.medium)
            .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", style = VittaTextStyles.title)
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column {
            Text("$days", style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
            Text("días de racha", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
        }
    }
}

/** The gold "N VitaPuntos" pill. */
@Composable
fun VittaPointsBadge(
    points: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .background(VittaColorRoles.surfaceMuted, VittaShapes.medium)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = VittaSpacing.Lg, vertical = VittaSpacing.Md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(VittaColors.GoldAccent, CircleShape)
        )
        Spacer(Modifier.width(VittaSpacing.Sm))
        Column {
            Text(points.toString(), style = VittaTextStyles.title, color = VittaColorRoles.textPrimary)
            Text("VitaPuntos", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
        }
    }
}

/** The horizontal level-progress bar, e.g. "Impulsor · 750 para el siguiente nivel". */
@Composable
fun VittaLevelProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = VittaColorRoles.surfaceMuted,
    fillColor: Color = VittaColors.GoldAccent
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(trackColor, VittaShapes.pill)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(fillColor, VittaShapes.pill)
        )
    }
}

/** One bar of the weekly bar chart on the Progress screen. */
@Composable
fun VittaWeeklyBar(
    value: Float,
    label: String,
    modifier: Modifier = Modifier,
    isToday: Boolean = false,
    maxHeight: androidx.compose.ui.unit.Dp = 96.dp
) {
    val barColor = when {
        isToday -> VittaColorRoles.primary
        value >= 0.75f -> VittaColors.GreenMid
        else -> VittaColors.GreenTrack
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(maxHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(maxHeight * value.coerceIn(0f, 1f))
                    .background(barColor, VittaShapes.extraSmall)
            )
        }
        Spacer(Modifier.height(VittaSpacing.Xxs))
        Text(
            label,
            style = VittaTextStyles.label,
            color = if (isToday) VittaColorRoles.textPrimary else VittaColorRoles.textDisabled
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaProgressComponentsPreview() {
    VittaTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                VittaStreakBadge(days = 12)
                VittaPointsBadge(points = 1250)
            }
            VittaLevelProgressBar(progress = 0.4f)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.6f, 0.75f, 1f, 0.5f).forEachIndexed { i, v ->
                    VittaWeeklyBar(value = v, label = "L,M,X,J".split(",")[i], isToday = i == 2)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VittaInsigniaBadge(Insignia.Days7, unlocked = true)
                VittaInsigniaBadge(Insignia.Days30, unlocked = false)
            }
        }
    }
}
