package com.vitta.app.ui.components.mascot

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.theme.VittaSizes
import com.vitta.app.ui.theme.VittaTheme

/**
 * Every mascot illustration that exists in the source project — the 6
 * reward illustrations shipped as loose `assets/png/vitta-*.png` files,
 * plus 5 more poses recovered from `Vitta App.html`'s embedded bundle
 * (onboarding hero, empty-state, small waves/peeks — see
 * README_RECURSOS.md for exactly how each was identified and extracted).
 *
 * Add a new case here (and its drawable) rather than inventing a pose that
 * doesn't exist in the source project.
 */
enum class MascotState(internal val drawableRes: Int) {
    Strong(R.drawable.vitta_fuerte),
    GoodHabits(R.drawable.vitta_buenoshabitos),
    Reader(R.drawable.vitta_lector),
    NoGym(R.drawable.vitta_singym),
    Evening(R.drawable.vitta_tarde),
    Yoga(R.drawable.vitta_yoga),
    OnboardingWelcome(R.drawable.mascot_onboarding_welcome),
    OnboardingHabits(R.drawable.mascot_onboarding_habits),
    Peek(R.drawable.mascot_peek),
    EmptyState(R.drawable.mascot_empty_state),
    Wave(R.drawable.mascot_wave)
}

/** Preset sizes matching how the mascot appears at different moments in the mockup. */
enum class MascotSize(internal val dp: Dp) {
    Small(VittaSizes.MascotSmall),
    Medium(VittaSizes.MascotMedium),
    Large(VittaSizes.MascotLarge),
    ExtraLarge(VittaSizes.MascotXl)
}

/**
 * Renders the Vitta mascot in a given [state] and [size]. This is the single
 * entry point screens should use for the mascot — never reference
 * `R.drawable.vitta_*` directly from a screen.
 */
@Composable
fun VittaMascot(
    state: MascotState,
    modifier: Modifier = Modifier,
    size: MascotSize = MascotSize.Medium
) {
    Image(
        painter = painterResource(state.drawableRes),
        contentDescription = null,
        modifier = modifier.size(size.dp),
        contentScale = ContentScale.Fit
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F4EE)
@Composable
private fun VittaMascotPreview() {
    VittaTheme {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            MascotState.entries.forEach { state ->
                VittaMascot(state = state, size = MascotSize.Small)
            }
        }
    }
}
