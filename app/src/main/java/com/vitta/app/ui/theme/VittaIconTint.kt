package com.vitta.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

/**
 * Los íconos de hábitos (Icons8) vienen a todo color. Este filtro los pasa a
 * un duotono de la paleta: las zonas oscuras quedan en verde oscuro y las
 * claras en oliva pálido, así conservan el detalle pero combinan con la app.
 */
object VittaIconTint {
    val habit: ColorFilter = duotone(dark = VittaColors.TextPrimary, light = VittaColors.GreenSoft)

    private fun duotone(dark: Color, light: Color): ColorFilter {
        // Luminancia (0–255) → mezcla lineal entre dark y light, canal por canal.
        fun row(d: Float, l: Float): FloatArray {
            val span = l - d
            return floatArrayOf(0.299f * span, 0.587f * span, 0.114f * span, 0f, d * 255f)
        }
        val matrix = row(dark.red, light.red) +
            row(dark.green, light.green) +
            row(dark.blue, light.blue) +
            floatArrayOf(0f, 0f, 0f, 1f, 0f)
        return ColorFilter.colorMatrix(ColorMatrix(matrix))
    }
}
