package de.dbauer.expensetracker.ui.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.conditional
import de.dbauer.expensetracker.ui.customizations.tagColorFamilies
import de.dbauer.expensetracker.ui.elements.HorizontalLazyRowWithGradient
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.cancel
import recurringexpensetracker.app.generated.resources.save
import recurringexpensetracker.app.generated.resources.tags_add_new
import recurringexpensetracker.app.generated.resources.tags_color
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
    val scrollState = rememberScrollState()
    var selectedBaseColor by remember { mutableStateOf(tagColorFamilies.first()) }
    AlertDialog(
        onDismissRequest = onDismissAddNewTagDialog,
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
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
                Text(
                    text = stringResource(Res.string.tags_color),
                    style = MaterialTheme.typography.bodyLarge,
                )
                val errorBorderColor = MaterialTheme.colorScheme.error
                Column(
                    modifier =
                        Modifier.conditional(tagColorError) {
                            Modifier.border(
                                width = 1.dp,
                                color = errorBorderColor,
                                shape = RoundedCornerShape(8.dp),
                            )
                        },
                ) {
                    val outlineColor = MaterialTheme.colorScheme.onSurface
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 16.dp),
                    ) {
                        tagColorFamilies.forEach { tagColorFamily ->
                            Box(
                                modifier =
                                    Modifier
                                        .requiredSize(48.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (selectedBaseColor == tagColorFamily) {
                                                Modifier
                                                    .border(
                                                        width = 2.dp,
                                                        color = outlineColor,
                                                        shape = CircleShape,
                                                    ).padding(4.dp)
                                            } else {
                                                Modifier
                                            },
                                        ).background(
                                            color = Color(tagColorFamily.base),
                                            shape = CircleShape,
                                        ).clickable {
                                            if (selectedBaseColor != tagColorFamily) {
                                                onTagColorChange(0L)
                                            }
                                            selectedBaseColor = tagColorFamily
                                        },
                            )
                        }
                    }
                    HorizontalDivider()
                    HorizontalLazyRowWithGradient(
                        gradientColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 16.dp),
                    ) {
                        items(selectedBaseColor.palette) { color ->
                            Box(
                                modifier =
                                    Modifier
                                        .requiredSize(48.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (tagColor == color) {
                                                Modifier
                                                    .border(
                                                        width = 2.dp,
                                                        color = outlineColor,
                                                        shape = CircleShape,
                                                    ).padding(4.dp)
                                            } else {
                                                Modifier
                                            },
                                        ).background(
                                            color = Color(color),
                                            shape = CircleShape,
                                        ).clickable { onTagColorChange(color) },
                            )
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
