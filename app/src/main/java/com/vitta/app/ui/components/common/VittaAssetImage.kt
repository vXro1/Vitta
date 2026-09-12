package com.vitta.app.ui.components.common

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

/**
 * Renders one of the rich, full-color SVGs shipped under
 * `app/src/main/assets/vitta/...` (habit icons, achievement badges).
 *
 * These SVGs are AI-illustrated pictures with hundreds of gradient-filled
 * paths, not simple single-tint glyphs — they are loaded through Coil's SVG
 * decoder instead of being hand-converted to VectorDrawable, which would
 * either balloon into an unmaintainable XML or silently drop detail the
 * conversion can't express. See README_RECURSOS.md for the full rationale.
 *
 * @param assetPath path under `assets/`, e.g. `"vitta/icons/yoga.svg"`.
 */
@Composable
fun VittaAssetImage(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp? = null
) {
    AsyncImage(
        model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
            .data("file:///android_asset/$assetPath")
            .build(),
        contentDescription = contentDescription,
        modifier = if (size != null) modifier.size(size) else modifier,
        contentScale = ContentScale.Fit
    )
}

/** Path helpers so screens never hand-type `"vitta/icons/..."` strings. */
object VittaIconAssets {
    fun icon(name: String) = "vitta/icons/$name.svg"
    fun insignia(name: String) = "vitta/insignias/insignia-$name.svg"

    /** The Vitta app logo/symbol mark. */
    const val logo = "vitta/logo.svg"

    /** The "Vitta" wordmark (logotype) used as the app's title treatment. */
    const val wordmark = "vitta/wordmark.svg"

    /** The animated-in-HTML streak flame illustration, used statically here. */
    const val streakFlame = "vitta/icons/icon_streak_flame.svg"
}
