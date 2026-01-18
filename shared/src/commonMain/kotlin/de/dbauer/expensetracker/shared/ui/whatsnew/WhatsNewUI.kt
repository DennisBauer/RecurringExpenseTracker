package de.dbauer.expensetracker.shared.ui.whatsnew

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.dialog_ok

@Composable
internal fun WhatsNewUI(
    whatsNewSlides: List<WhatsNewSlide>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { whatsNewSlides.size })

    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier =
                    Modifier
                        .padding(24.dp)
                        .weight(3f),
            ) { page ->
                PagerSlide(slide = whatsNewSlides[page])
            }

            Column(
                modifier =
                    Modifier
                        .padding(bottom = 48.dp)
                        .fillMaxWidth()
                        .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                PageIndicator(
                    pagerState = pagerState,
                    pageCount = whatsNewSlides.size,
                )

                Spacer(modifier = Modifier.height(32.dp))

                val isButtonVisible = pagerState.currentPage == whatsNewSlides.size - 1

                val dismissButtonAlpha by animateFloatAsState(
                    targetValue = if (isButtonVisible) 1f else 0f,
                    label = "dismissButtonAlpha",
                )

                Button(
                    onClick = onDismissRequest,
                    enabled = isButtonVisible,
                    modifier = Modifier.alpha(dismissButtonAlpha),
                ) {
                    Text(text = stringResource(Res.string.dialog_ok))
                }
            }
        }
    }
}
