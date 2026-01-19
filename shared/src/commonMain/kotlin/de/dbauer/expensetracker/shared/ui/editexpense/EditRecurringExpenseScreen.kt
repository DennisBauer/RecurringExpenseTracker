package de.dbauer.expensetracker.shared.ui.editexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.shared.viewmodel.EditRecurringExpenseViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.cancel
import recurringexpensetracker.shared.generated.resources.delete
import recurringexpensetracker.shared.generated.resources.discard
import recurringexpensetracker.shared.generated.resources.edit_expense_button_add
import recurringexpensetracker.shared.generated.resources.edit_expense_delete_dialog_text
import recurringexpensetracker.shared.generated.resources.edit_expense_title
import recurringexpensetracker.shared.generated.resources.edit_expense_unsaved_changes_dialog_text
import recurringexpensetracker.shared.generated.resources.edit_expense_unsaved_changes_dialog_title
import recurringexpensetracker.shared.generated.resources.save

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EditRecurringExpenseScreen(
    expenseId: Int?,
    canUseNotifications: Boolean,
    onDismiss: () -> Unit,
    onEditTagsClick: () -> Unit,
    setTopAppBar: (@Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditRecurringExpenseViewModel = koinViewModel { parametersOf(expenseId) },
) {
    // This disables the predictive back gesture on this screen to prevent accidental data loss
    // TODO: Something to revisit later whether it can be improved
    BackHandler {
        viewModel.onBackPressed(onDismiss)
    }

    val scrollState = rememberScrollState()
    val localFocusManager = LocalFocusManager.current
    Column(
        modifier =
            modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
    ) {
        NameOption(
            name = viewModel.nameState,
            onNameChange = { viewModel.nameState = it },
            nameInputError = viewModel.nameInputError.value,
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        DescriptionOption(
            description = viewModel.descriptionState,
            onDescriptionChange = { viewModel.descriptionState = it },
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        PriceOption(
            price = viewModel.priceState,
            onPriceChange = { viewModel.priceState = it },
            priceInputError = viewModel.priceInputError.value,
            selectedCurrencyOption = viewModel.selectedCurrencyOption,
            availableCurrencyOptions = viewModel.availableCurrencyOptions,
            onSelectCurrencyOption = { viewModel.selectedCurrencyOption = it },
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
            currencyInputError = viewModel.currencyError,
        )
        RecurrenceOption(
            everyXRecurrence = viewModel.everyXRecurrenceState,
            onEveryXRecurrenceChange = { viewModel.everyXRecurrenceState = it },
            everyXRecurrenceInputError = viewModel.everyXRecurrenceInputError.value,
            selectedRecurrence = viewModel.selectedRecurrence,
            onSelectRecurrence = { viewModel.selectedRecurrence = it },
            onNext = { localFocusManager.clearFocus() },
        )
        FirstPaymentOption(
            date = viewModel.firstPaymentDate,
            onSelectDate = { viewModel.firstPaymentDate = it },
        )
        TagsOption(
            tags = viewModel.tags,
            onTagClick = viewModel::onTagClick,
            onEditTagsClick = onEditTagsClick,
        )
        if (canUseNotifications) {
            MultipleRemindersOption(
                expenseNotificationEnabledGlobally =
                    viewModel.expenseNotificationEnabledGlobally
                        .collectAsState()
                        .value,
                notifyForExpense = viewModel.notifyForExpense,
                onNotifyForExpenseChange = viewModel::onNotifyForExpenseChange,
                reminders = viewModel.reminders,
                onAddReminder = viewModel::addReminder,
                onUpdateReminder = viewModel::updateReminder,
                onRemoveReminder = viewModel::removeReminder,
                isReminderDuplicate = viewModel::isReminderDuplicate,
                isNewReminderDuplicate = viewModel::isNewReminderDuplicate,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .navigationBarsPadding()
                    .imePadding(),
        ) {
            Button(
                onClick = {
                    viewModel.updateExpense { successful ->
                        if (successful) {
                            onDismiss()
                        }
                    }
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .wrapContentWidth(),
            ) {
                Text(
                    text =
                        stringResource(
                            if (viewModel.isNewExpense) {
                                Res.string.edit_expense_button_add
                            } else {
                                Res.string.save
                            },
                        ),
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
    if (viewModel.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteDialog,
            text = {
                Text(text = stringResource(Res.string.edit_expense_delete_dialog_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense()
                        onDismiss()
                    },
                ) {
                    Text(text = stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::onDismissDeleteDialog,
                ) {
                    Text(text = stringResource(Res.string.cancel))
                }
            },
        )
    }
    if (viewModel.showDismissUnsavedChangesDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissUnsavedChangesDialog,
            title = {
                Text(text = stringResource(Res.string.edit_expense_unsaved_changes_dialog_title))
            },
            text = {
                Text(text = stringResource(Res.string.edit_expense_unsaved_changes_dialog_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDiscardChanges(onDismiss)
                    },
                ) {
                    Text(text = stringResource(Res.string.discard))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::onDismissUnsavedChangesDialog,
                ) {
                    Text(text = stringResource(Res.string.cancel))
                }
            },
        )
    }
    setTopAppBar {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.edit_expense_title),
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.onBackPressed(onDismiss) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                    )
                }
            },
            actions = {
                if (viewModel.showDeleteButton) {
                    IconButton(
                        onClick = viewModel::onDeleteClick,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(Res.string.delete),
                        )
                    }
                }
            },
        )
    }
}

@Preview
@Composable
private fun EditRecurringExpensePreview() {
    ExpenseTrackerThemePreview {
        Surface(modifier = Modifier.fillMaxSize()) {
            EditRecurringExpenseScreen(
                expenseId = 0,
                canUseNotifications = true,
                onDismiss = {},
                onEditTagsClick = {},
                setTopAppBar = {},
            )
        }
    }
}
