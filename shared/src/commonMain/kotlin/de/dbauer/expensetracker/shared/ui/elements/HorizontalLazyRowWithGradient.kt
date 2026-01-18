package de.dbauer.expensetracker.shared.ui.elements

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalLazyRowWithGradient(
    gradientColor: Color,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    content: LazyListScope.() -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val gradientWidth = 24.dp

    val startGradientAlpha by animateFloatAsState(
        targetValue = if (lazyListState.canScrollBackward) 1f else 0f,
        label = "StartGradientAlpha",
    )

    val endGradientAlpha by animateFloatAsState(
        targetValue = if (lazyListState.canScrollForward) 1f else 0f,
        label = "EndGradientAlpha",
    )

    LazyRow(
        state = lazyListState,
        horizontalArrangement = horizontalArrangement,
        modifier =
            modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()

                    val viewPortWidth = size.width

                    if (startGradientAlpha > 0f) {
                        drawRect(
                            brush =
                                Brush.horizontalGradient(
                                    colors =
                                        listOf(
                                            gradientColor.copy(alpha = startGradientAlpha),
                                            Color.Transparent,
                                        ),
                                    startX = 0f,
                                    endX = gradientWidth.toPx(),
                                ),
                            size = Size(width = gradientWidth.toPx(), height = size.height),
                        )
                    }

                    if (endGradientAlpha > 0f) {
                        drawRect(
                            brush =
                                Brush.horizontalGradient(
                                    colors =
                                        listOf(
                                            Color.Transparent,
                                            gradientColor.copy(alpha = endGradientAlpha),
                                        ),
                                    startX = viewPortWidth - gradientWidth.toPx(),
                                    endX = viewPortWidth,
                                ),
                            topLeft = Offset(x = viewPortWidth - gradientWidth.toPx(), y = 0f),
                        )
                    }
                },
    ) {
        content()
    }
}
