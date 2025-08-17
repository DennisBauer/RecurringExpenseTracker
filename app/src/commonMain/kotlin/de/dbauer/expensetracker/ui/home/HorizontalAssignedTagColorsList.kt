package de.dbauer.expensetracker.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.ui.elements.HorizontalLazyRowWithGradient

@Composable
fun HorizontalAssignedTagColorsList(
    tags: List<Tag>,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(4.dp),
    gradientColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    HorizontalLazyRowWithGradient(
        gradientColor = gradientColor,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier,
    ) {
        items(tags) {
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(
                    color = Color(it.color),
                    radius = minOf(size.width, size.height) / 2.0f,
                )
            }
        }
    }
}
