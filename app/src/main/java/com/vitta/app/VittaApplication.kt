package com.vitta.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder

/**
 * Registers Coil's SVG decoder app-wide so [com.vitta.app.ui.components.common.VittaAssetImage]
 * can render the habit-icon and badge SVGs under `assets/vitta/`.
 */
class VittaApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()
}
