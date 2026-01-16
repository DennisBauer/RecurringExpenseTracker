package de.dbauer.expensetracker.ui.editexpense

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.ui.animatePlacement
import de.dbauer.expensetracker.ui.elements.tagChipColorDefaults
import de.dbauer.expensetracker.ui.tags.AddTagDialog
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.viewmodel.TagsScreenViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_tags
import recurringexpensetracker.app.generated.resources.tags_show_more
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun TagsOption(
    tags: List<Pair<Tag, Boolean>>,
    onTagClick: (Tag) -> Unit,
    onEditTagsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TagsScreenViewModel = koinViewModel<TagsScreenViewModel>(),
) {
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
                onClick = viewModel::onAddNewTag,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                )
            }
        }
        TagsList(
            tags = tags,
            onTagClick = onTagClick,
        )
    }
    if (viewModel.showAddOrEditTagDialog) {
        AddTagDialog(
            isNewTag = viewModel.isNewTag,
            tagTitle = viewModel.tagTitle,
            tagTitleError = viewModel.tagTitleError,
            onTagTitleChange = viewModel::onTagTitleChange,
            tagColor = viewModel.tagColor,
            tagColorError = viewModel.tagColorError,
            onTagColorChange = viewModel::onTagColorChange,
            onConfirmAddNewTag = viewModel::onConfirmAddNewTag,
            onDismissAddNewTagDialog = viewModel::onDismissAddNewTagDialog,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsList(
    tags: List<Pair<Tag, Boolean>>,
    onTagClick: (Tag) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var collapsedSize by remember { mutableStateOf(IntSize.Zero) }
    var fullSize by remember { mutableStateOf(IntSize.Zero) }
    val hasOverflow by remember(collapsedSize, fullSize) {
        derivedStateOf {
            fullSize.height > 0 && collapsedSize.height > 0 && fullSize.height > collapsedSize.height
        }
    }

    Column(modifier = modifier) {
        Box {
            FlowRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .onSizeChanged { collapsedSize = it },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            ) {
                tags.forEach { (tag, selected) ->
                    key(tag.id) {
                        FilterChip(
                            selected = selected,
                            onClick = { onTagClick(tag) },
                            label = { Text(text = tag.title) },
                            colors = tagChipColorDefaults(Color(tag.color)),
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
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

            FlowRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .layout { measurable, constraints ->
                            val placeable = measurable.measure(constraints)
                            fullSize = IntSize(placeable.width, placeable.height)
                            layout(0, 0) {}
                        },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                tags.forEach { (tag, selected) ->
                    FilterChip(
                        selected = selected,
                        onClick = { },
                        label = { Text(text = tag.title) },
                        leadingIcon = {
                            if (selected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    Modifier.size(InputChipDefaults.AvatarSize),
                                )
                            }
                        },
                    )
                }
            }
        }

        if (hasOverflow) {
            TextButton(
                onClick = { isExpanded = true },
                border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(Res.string.tags_show_more))
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
