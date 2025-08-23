package de.dbauer.expensetracker.ui.tags

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.conditional
import de.dbauer.expensetracker.ui.customizations.ExpenseColor
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.cancel
import recurringexpensetracker.app.generated.resources.save
import recurringexpensetracker.app.generated.resources.tags_add_new
import recurringexpensetracker.app.generated.resources.tags_edit
import recurringexpensetracker.app.generated.resources.tags_title
import recurringexpensetracker.app.generated.resources.tags_title_empty

@Composable
fun AddTagDialog(
    isNewTag: Boolean,
    tagTitle: String,
    tagTitleError: Boolean,
    onTagTitleChange: (String) -> Unit,
    tagColor: Long,
    tagColorError: Boolean,
    onTagColorChange: (Long) -> Unit,
    onConfirmAddNewTag: () -> Unit,
    onDismissAddNewTagDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyGridState()
    AlertDialog(
        onDismissRequest = onDismissAddNewTagDialog,
        text = {
            Column {
                Text(
                    text =
                        if (isNewTag) {
                            stringResource(Res.string.tags_add_new)
                        } else {
                            stringResource(Res.string.tags_edit)
                        },
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = stringResource(Res.string.tags_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
                val supportingText: (@Composable () -> Unit)? =
                    if (tagTitleError) {
                        {
                            Text(
                                text = stringResource(Res.string.tags_title_empty),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else {
                        null
                    }
                val trailingIcon: (@Composable () -> Unit)? =
                    if (tagTitleError) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Error,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    } else {
                        null
                    }
                TextField(
                    value = tagTitle,
                    onValueChange = onTagTitleChange,
                    singleLine = true,
                    isError = tagTitleError,
                    maxLines = 1,
                    supportingText = supportingText,
                    trailingIcon = trailingIcon,
                )
                Spacer(modifier = Modifier.size(16.dp))
                val errorBorderColor = MaterialTheme.colorScheme.error
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(48.dp),
                    state = gridState,
                    modifier =
                        Modifier.conditional(tagColorError) {
                            Modifier.border(
                                width = 1.dp,
                                color = errorBorderColor,
                                shape = RoundedCornerShape(8.dp),
                            )
                        },
                ) {
                    items(ExpenseColor.entries) { color ->
                        val outlineColor = MaterialTheme.colorScheme.onSurface
                        val colorLong = color.getColor().toArgb().toLong()

                        Canvas(
                            modifier =
                                Modifier
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(50))
                                    .conditional(tagColor == colorLong) {
                                        border(
                                            width = 2.dp,
                                            color = outlineColor,
                                            shape = RoundedCornerShape(50),
                                        )
                                    }.background(color.getColor())
                                    .requiredSize(48.dp)
                                    .clickable {
                                        onTagColorChange(colorLong)
                                    },
                        ) {
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmAddNewTag,
            ) {
                Text(text = stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissAddNewTagDialog,
            ) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
        modifier = modifier,
    )
}
