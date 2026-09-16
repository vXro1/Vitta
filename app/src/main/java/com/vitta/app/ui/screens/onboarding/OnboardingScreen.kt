package com.vitta.app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vitta.app.R
import com.vitta.app.ui.components.buttons.VittaPrimaryButton
import com.vitta.app.ui.components.buttons.VittaTextButton
import com.vitta.app.ui.components.common.VittaAssetImage
import com.vitta.app.ui.theme.VittaColorRoles
import com.vitta.app.ui.theme.VittaColors
import com.vitta.app.ui.theme.VittaSpacing
import com.vitta.app.ui.theme.VittaTextStyles
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VittaColorRoles.background)
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(VittaColors.SurfaceVariant)
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VittaSpacing.Lg, end = VittaSpacing.Lg),
                contentAlignment = Alignment.TopEnd
            ) {
                if (pagerState.currentPage < 2) {
                    VittaTextButton(text = "Omitir", onClick = onFinish)
                }
            }
            VittaTextButton(text = "Omitir", onClick = {
                android.util.Log.d("VittaDebug", "Omitir tocado")
                onFinish()
            })

            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                when (page) {
                    0 -> OnboardingPageContent(
                        illustration = {
                            Image(
                                painter = painterResource(R.drawable.mascot_onboarding_welcome),
                                contentDescription = null,
                                modifier = Modifier.size(220.dp),
                                contentScale = ContentScale.Fit
                            )
                        },
                        title = "Construye hábitos\ncon Vitta",
                        description = "Elige lo que quieres mejorar y Vitta te acompaña cada día."
                    )
                    1 -> OnboardingPageContent(
                        illustration = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(VittaSpacing.Xl),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VittaAssetImage(
                                    assetPath = "vitta/insignias/insignia-7dias.svg",
                                    contentDescription = null,
                                    size = 70.dp
                                )
                                VittaAssetImage(
                                    assetPath = "vitta/icons/icon_streak_flame.svg",
                                    contentDescription = null,
                                    size = 90.dp
                                )
                                VittaAssetImage(
                                    assetPath = "vitta/insignias/insignia-30dias.svg",
                                    contentDescription = null,
                                    size = 70.dp
                                )
                            }
                        },
                        title = "Tu constancia\nse vuelve visible",
                        description = "Rachas, VitaPuntos y logros que reconocen lo que ya estás haciendo."
                    )
                    2 -> OnboardingPageContent(
                        illustration = {
                            Image(
                                painter = painterResource(R.drawable.mascot_onboarding_habits),
                                contentDescription = null,
                                modifier = Modifier.size(220.dp),
                                contentScale = ContentScale.Fit
                            )
                        },
                        title = "A tu ritmo,\nsin castigos",
                        description = "Si pierdes un día, mañana empiezas una racha nueva. Vitta acompaña, no juzga."
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = VittaSpacing.Lg),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(width = if (pagerState.currentPage == index) 22.dp else 8.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) VittaColorRoles.primary
                                else VittaColors.GreenTrack
                            )
                    )
                }
            }

            Box(modifier = Modifier.padding(horizontal = VittaSpacing.Xxl, vertical = VittaSpacing.Lg)) {
                VittaPrimaryButton(
                    text = if (pagerState.currentPage == 2) "Empezar" else "Siguiente",
                    onClick = {
                        if (pagerState.currentPage == 2) {
                            onFinish()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    illustration: @Composable () -> Unit,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = VittaSpacing.Xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        illustration()
        Spacer(Modifier.height(VittaSpacing.Xxxl))
        Text(
            text = title,
            style = VittaTextStyles.displayLarge,
            color = VittaColorRoles.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(VittaSpacing.Sm))
        Text(
            text = description,
            style = VittaTextStyles.body,
            color = VittaColorRoles.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}