package com.vitta.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii used throughout the mockup, from the small 10px chip on a
 * calendar cell up to the 34px sheet/dialog corner. `Pill` is the special
 * "999px" full-stadium radius used by every button and chip.
 */
object VittaShapes {
    val RadiusXs = 10.dp
    val RadiusSmall = 14.dp
    val RadiusMedium = 18.dp
    val RadiusLarge = 20.dp
    val RadiusXl = 22.dp
    val RadiusXxl = 24.dp
    val RadiusHuge = 30.dp
    val RadiusSheet = 34.dp
    val Pill = 999.dp

    val extraSmall = RoundedCornerShape(RadiusXs)
    val small = RoundedCornerShape(RadiusSmall)
    val medium = RoundedCornerShape(RadiusMedium)
    val large = RoundedCornerShape(RadiusLarge)
    val extraLarge = RoundedCornerShape(RadiusXxl)
    val sheetTop = RoundedCornerShape(topStart = RadiusSheet, topEnd = RadiusSheet)
    val pill = RoundedCornerShape(percent = 50)
}

/** Material 3 [Shapes] mapped onto [VittaShapes] so stock M3 components match Vitta's rounding. */
val VittaMaterialShapes = Shapes(
    extraSmall = VittaShapes.extraSmall,
    small = VittaShapes.small,
    medium = VittaShapes.medium,
    large = VittaShapes.large,
    extraLarge = VittaShapes.extraLarge
)
