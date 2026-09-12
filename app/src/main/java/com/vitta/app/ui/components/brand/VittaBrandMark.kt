package com.vitta.app.ui.components.brand

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.components.common.VittaIconAssets
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTheme

/** The Vitta logo/symbol mark (`assets/vitta/logo.svg`). */
@Composable
fun VittaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    VittaAssetImage(
        assetPath = VittaIconAssets.logo,
        contentDescription = "Vitta",
        modifier = modifier,
        size = size
    )
}

/** The "Vitta" wordmark/logotype (`assets/vitta/wordmark.svg`) — the app's title treatment. */
@Composable
fun VittaWordmark(
    modifier: Modifier = Modifier,
    height: Dp = 28.dp
) {
    VittaAssetImage(
        assetPath = VittaIconAssets.wordmark,
        contentDescription = "Vitta",
        modifier = modifier,
        size = height
    )
}

/** Logo + wordmark side by side, the standard app-title lockup. */
@Composable
fun VittaBrandLockup(
    modifier: Modifier = Modifier,
    logoSize: Dp = 32.dp,
    wordmarkHeight: Dp = 24.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        VittaLogo(size = logoSize)
        Spacer(Modifier.width(VittaSpacing.Sm))
        VittaWordmark(height = wordmarkHeight)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaBrandMarkPreview() {
    VittaTheme {
        Column {
            VittaBrandLockup()
            Spacer(Modifier.width(VittaSpacing.Md))
            Text("(logo + wordmark)", color = VittaColorRoles.textMuted)
        }
    }
}
