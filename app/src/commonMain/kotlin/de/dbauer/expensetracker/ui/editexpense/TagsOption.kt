package de.dbauer.expensetracker.ui.editexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.ui.animatePlacement
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerThemePreview
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_tags
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TagsOption(
    tags: List<Pair<Tag, Boolean>>,
    onTagClick: (Tag) -> Unit,
    onEditTagsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tagsExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier =
            modifier
                .padding(vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.edit_expense_tags),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(
                onClick = onEditTagsClick,
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(Res.string.edit_expense_tags),
                )
            }
            IconButton(
                onClick = { tagsExpanded = !tagsExpanded },
            ) {
                Icon(
                    imageVector =
                        if (tagsExpanded) {
                            Icons.Default.KeyboardArrowUp
                        } else {
                            Icons.Default.KeyboardArrowDown
                        },
                    contentDescription = null,
                )
            }
        }
        TagsList(
            tags = tags,
            onTagClick = onTagClick,
            tagsExpanded = tagsExpanded,
        )
    }
}

@Composable
private fun TagsList(
    tags: List<Pair<Tag, Boolean>>,
    onTagClick: (Tag) -> Unit,
    tagsExpanded: Boolean,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier =
            modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tags.forEach { (tag, selected) ->
            key(tag.id) {
                if (tagsExpanded || selected) {
                    InputChip(
                        selected = selected,
                        onClick = { onTagClick(tag) },
                        label = { Text(text = tag.title) },
                        colors =
                            InputChipDefaults.inputChipColors().copy(
                                selectedContainerColor = Color(tag.color),
                            ),
                        trailingIcon = {
                            if (selected) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Localized description",
                                    Modifier.size(InputChipDefaults.AvatarSize),
                                )
                            }
                        },
                        modifier =
                            Modifier
                                .animatePlacement(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun TagsOptionPreview() {
    ExpenseTrackerThemePreview {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val tags =
                remember {
                    mutableStateListOf(
                        Tag("Tag longer1", 0x80990000, id = Uuid.random().hashCode()) to true,
                        Tag("Tag 2", 0x80994d00, id = Uuid.random().hashCode()) to true,
                        Tag("Tag extralong name 3", 0x80999900, id = Uuid.random().hashCode()) to false,
                        Tag("Tag 4", 0x80009900, id = Uuid.random().hashCode()) to false,
                        Tag("Tag 5", 0x8000994d, id = Uuid.random().hashCode()) to false,
                        Tag("Tag 6", 0x80009999, id = Uuid.random().hashCode()) to false,
                    )
                }

            TagsOption(
                tags = tags,
                onTagClick = { tag ->
                    val oldTag = tags.first { it.first == tag }
                    tags.remove(oldTag)
                    tags.add(tag to !oldTag.second)
                    tags.sortBy { !it.second }
                },
                onEditTagsClick = {},
            )
        }
    }
}
