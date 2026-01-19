package de.dbauer.expensetracker.shared.ui.tags

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import de.dbauer.expensetracker.shared.conditional
import de.dbauer.expensetracker.shared.ui.customizations.tagColorFamilies
import de.dbauer.expensetracker.shared.ui.elements.HorizontalLazyRowWithGradient
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.cancel
import recurringexpensetracker.shared.generated.resources.save
import recurringexpensetracker.shared.generated.resources.tags_add_new
import recurringexpensetracker.shared.generated.resources.tags_color
import recurringexpensetracker.shared.generated.resources.tags_color_custom
import recurringexpensetracker.shared.generated.resources.tags_color_simple
import recurringexpensetracker.shared.generated.resources.tags_edit
import recurringexpensetracker.shared.generated.resources.tags_title
import recurringexpensetracker.shared.generated.resources.tags_title_empty

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
    var showSimpleColorPicker by rememberSaveable { mutableStateOf(true) }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.tags_color),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    TextButton(
                        onClick = { showSimpleColorPicker = !showSimpleColorPicker },
                        border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
                    ) {
                        Text(
                            text =
                                if (showSimpleColorPicker) {
                                    stringResource(Res.string.tags_color_custom)
                                } else {
                                    stringResource(Res.string.tags_color_simple)
                                },
                        )
                    }
                }
                val errorBorderColor = MaterialTheme.colorScheme.error
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .conditional(tagColorError) {
                                Modifier.border(
                                    width = 1.dp,
                                    color = errorBorderColor,
                                    shape = RoundedCornerShape(8.dp),
                                )
                            },
                ) {
                    if (showSimpleColorPicker) {
                        SimpleColorPicker(
                            tagColor = tagColor,
                            onTagColorChange = onTagColorChange,
                        )
                    } else {
                        CustomColorPicker(
                            tagColor = tagColor,
                            onTagColorChange = onTagColorChange,
                        )
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

@Composable
private fun ColumnScope.SimpleColorPicker(
    tagColor: Long,
    onTagColorChange: (Long) -> Unit,
) {
    var selectedBaseColor by remember {
        mutableStateOf(tagColorFamilies.firstOrNull { it.palette.contains(tagColor) } ?: tagColorFamilies.first())
    }
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

@Composable
private fun ColumnScope.CustomColorPicker(
    tagColor: Long,
    onTagColorChange: (Long) -> Unit,
) {
    val controller = rememberColorPickerController()

    Box(
        modifier =
            Modifier
                .requiredHeight(48.dp)
                .fillMaxWidth()
                .background(
                    color = Color(tagColor),
                    shape = RoundedCornerShape(8.dp),
                ),
    )
    Spacer(modifier = Modifier.size(8.dp))
    HsvColorPicker(
        modifier =
            Modifier
                .size(250.dp)
                .align(Alignment.CenterHorizontally),
        controller = controller,
        initialColor = Color(tagColor),
        onColorChanged = {
            if (it.fromUser) {
                onTagColorChange(it.hexCode.toLong(16))
            }
        },
    )
    Spacer(modifier = Modifier.size(8.dp))
    BrightnessSlider(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(35.dp),
        controller = controller,
    )
}
