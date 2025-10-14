package de.dbauer.expensetracker.ui.tags

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.viewmodel.TagsScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.tags_add_new
import recurringexpensetracker.app.generated.resources.tags_delete_undo
import recurringexpensetracker.app.generated.resources.tags_deleted
import recurringexpensetracker.app.generated.resources.tags_screen_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsScreen(
    setTopAppBar: (@Composable () -> Unit) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: TagsScreenViewModel = koinViewModel<TagsScreenViewModel>(),
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarDeleteMessage = stringResource(Res.string.tags_deleted)
    val snackbarDeleteAction = stringResource(Res.string.tags_delete_undo)

    LazyColumn(modifier = modifier) {
        items(viewModel.tags, key = { it.id }) { tag ->
            EditTagEntry(
                tag = tag,
                onClick = { viewModel.onEditTag(tag) },
                onDelete = {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    coroutineScope.launch {
                        delay(200)
                        viewModel.onDeleteTag(tag)
                        val result =
                            snackbarHostState.showSnackbar(
                                message = snackbarDeleteMessage,
                                actionLabel = snackbarDeleteAction,
                                duration = SnackbarDuration.Long,
                            )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.onUndoDeleteTag(tag)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
    setTopAppBar {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.tags_screen_title),
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = viewModel::onAddNewTag,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(Res.string.tags_add_new),
                    )
                }
            },
        )
    }
}

@Composable
private fun EditTagEntry(
    tag: Tag,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDelete = rememberUpdatedState(onDelete)
    val dismissState =
        rememberSwipeToDismissBoxState(
            positionalThreshold = { it * .25f },
        )

    LaunchedEffect(dismissState.targetValue) {
        when (dismissState.targetValue) {
            SwipeToDismissBoxValue.StartToEnd,
            SwipeToDismissBoxValue.EndToStart,
            -> {
                currentOnDelete.value()
                delay(200) // Small delay to allow swipe animation to complete

                // Reset the state back to Settled to prevent re-triggering on undo
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
            SwipeToDismissBoxValue.Settled -> {}
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clickable { onClick() },
        backgroundContent = { DismissBackground(dismissState = dismissState) },
    ) {
        val swipingBoxBackground =
            if (dismissState.dismissDirection ==
                SwipeToDismissBoxValue.Settled
            ) {
                MaterialTheme.colorScheme.background
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        Box(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(swipingBoxBackground),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp),
            ) {
                Canvas(modifier = Modifier.size(36.dp)) {
                    drawCircle(
                        color = Color(tag.color),
                        radius = minOf(size.width, size.height) / 2.0f,
                    )
                }
                Spacer(modifier = Modifier.size(16.dp))
                Text(
                    text = tag.title,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun DismissBackground(dismissState: SwipeToDismissBoxState) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment =
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                else -> Alignment.CenterStart
            },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(dismissState.progress)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.error),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Arrangement.End
                    SwipeToDismissBoxValue.StartToEnd -> Arrangement.Start
                    else -> Arrangement.Start
                },
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "delete",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.padding(12.dp, 8.dp),
            )
        }
    }
}
