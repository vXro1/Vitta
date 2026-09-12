package com.vitta.app.ui.components.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaShapes
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import com.vitta.app.ui.theme.VittaTheme

/**
 * The monthly activity heatmap from the Progress screen ("Últimas cinco
 * semanas"): a 7-columns-wide grid of small colored cells, one per day,
 * mirroring the mockup's `MAPA` array + `aspect-ratio:1` cell grid.
 *
 * @param intensities one entry per day, each `0..3` (0 = no activity,
 *   3 = full activity), read left-to-right then top-to-bottom in weeks of 7.
 * @param dayLabels the row of single-letter day labels under the grid.
 */
@Composable
fun VittaCalendarHeatmap(
    intensities: List<Int>,
    modifier: Modifier = Modifier,
    title: String = "Últimas cinco semanas",
    dayLabels: List<String> = listOf("L", "M", "X", "J", "V", "S", "D")
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VittaColorRoles.surface, VittaShapes.large)
            .padding(VittaSpacing.Lg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = VittaTextStyles.subtitle, color = VittaColorRoles.textSecondary)
            Text("Menos · Más", style = VittaTextStyles.caption, color = VittaColorRoles.textMuted)
        }
        Spacer(Modifier.height(VittaSpacing.Md))
        HeatmapGrid(intensities = intensities, columns = dayLabels.size)
        Spacer(Modifier.height(VittaSpacing.Sm))
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        label,
                        style = VittaTextStyles.label,
                        color = VittaColorRoles.textDisabled,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeatmapGrid(intensities: List<Int>, columns: Int) {
    val rows = intensities.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                week.forEach { level ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(colorForLevel(level), VittaShapes.extraSmall)
                    )
                }
                // Pads the last, possibly-incomplete week so cells keep their size.
                repeat(columns - week.size) {
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                }
            }
        }
    }
}

private fun colorForLevel(level: Int): Color = when (level.coerceIn(0, 3)) {
    0 -> VittaColors.SurfaceVariant
    1 -> VittaColors.GreenTrack
    2 -> VittaColors.GreenSoft
    else -> VittaColors.GreenPrimary
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaCalendarHeatmapPreview() {
    val mock = listOf(
        2, 3, 1, 3, 3, 2, 0, 3, 3, 2, 3, 1, 3, 2,
        3, 2, 3, 3, 3, 1, 2, 3, 3, 3, 2, 3, 3, 3,
        3, 2, 3, 1, 3, 3, 0
    )
    VittaTheme {
        VittaCalendarHeatmap(intensities = mock, modifier = Modifier.padding(16.dp))
    }
}
