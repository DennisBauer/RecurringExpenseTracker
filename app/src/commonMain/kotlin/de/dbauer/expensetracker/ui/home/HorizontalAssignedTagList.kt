package de.dbauer.expensetracker.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.ui.elements.HorizontalLazyRowWithGradient

@Composable
fun HorizontalAssignedTagList(
    tags: List<Tag>,
    onTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp),
    gradientColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
) {
    HorizontalLazyRowWithGradient(
        gradientColor = gradientColor,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier,
    ) {
        items(tags, key = { it.id }) { tag ->
            FilterChip(
                selected = false,
                onClick = { onTagClick(tag) },
                label = { Text(text = tag.title) },
                colors =
                    FilterChipDefaults.filterChipColors().copy(
                        containerColor = Color(tag.color),
                    ),
            )
        }
    }
}
