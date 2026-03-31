package de.dbauer.expensetracker.shared.ui.whatsnew

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.dialog_ok
import recurringexpensetracker.shared.generated.resources.whats_new_continue

@Composable
internal fun WhatsNewUI(
    whatsNewSlides: List<WhatsNewSlide>,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { whatsNewSlides.size })
    val coroutineScope = rememberCoroutineScope()

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

                val isLastPage = pagerState.currentPage == whatsNewSlides.size - 1

                val dismissButtonAlpha by animateFloatAsState(
                    targetValue = if (isLastPage) 1f else 0f,
                    label = "dismissButtonAlpha",
                )

                val continueButtonAlpha by animateFloatAsState(
                    targetValue = if (isLastPage) 0f else 1f,
                    label = "continueButtonAlpha",
                )

                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        enabled = !isLastPage,
                        modifier = Modifier.alpha(continueButtonAlpha),
                    ) {
                        Text(text = stringResource(Res.string.whats_new_continue))
                    }

                    Button(
                        onClick = onDismissRequest,
                        enabled = isLastPage,
                        modifier = Modifier.alpha(dismissButtonAlpha),
                    ) {
                        Text(text = stringResource(Res.string.dialog_ok))
                    }
                }
            }
        }
    }
}
