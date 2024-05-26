package ui.editexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import data.RecurringExpenseData
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_button_add
import recurringexpensetracker.app.generated.resources.edit_expense_button_delete
import recurringexpensetracker.app.generated.resources.edit_expense_button_save
import ui.theme.ExpenseTrackerTheme
import viewmodel.EditRecurringExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecurringExpense(
    onUpdateExpense: (RecurringExpenseData) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    currentData: RecurringExpenseData? = null,
    onDeleteExpense: ((RecurringExpenseData) -> Unit)? = null,
) {
    val sheetState: SheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        )
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        EditRecurringExpenseInternal(
            onUpdateExpense = onUpdateExpense,
            confirmButtonString =
                if (currentData == null) {
                    stringResource(Res.string.edit_expense_button_add)
                } else {
                    stringResource(
                        Res.string.edit_expense_button_save,
                    )
                },
            currentData = currentData,
            onDeleteExpense = onDeleteExpense,
        )
    }
}

@Composable
private fun EditRecurringExpenseInternal(
    onUpdateExpense: (RecurringExpenseData) -> Unit,
    confirmButtonString: String,
    modifier: Modifier = Modifier,
    currentData: RecurringExpenseData? = null,
    viewModel: EditRecurringExpenseViewModel =
        viewModel<EditRecurringExpenseViewModel> {
            EditRecurringExpenseViewModel(currentData)
        },
    onDeleteExpense: ((RecurringExpenseData) -> Unit)? = null,
) {
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
            onNameChanged = { viewModel.nameState = it },
            nameInputError = viewModel.nameInputError.value,
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        DescriptionOption(
            description = viewModel.descriptionState,
            onDescriptionChanged = { viewModel.descriptionState = it },
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        PriceOption(
            price = viewModel.priceState,
            onPriceChanged = { viewModel.priceState = it },
            priceInputError = viewModel.priceInputError.value,
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        RecurrenceOption(
            everyXRecurrence = viewModel.everyXRecurrenceState,
            onEveryXRecurrenceChanged = { viewModel.everyXRecurrenceState = it },
            everyXRecurrenceInputError = viewModel.everyXRecurrenceInputError.value,
            selectedRecurrence = viewModel.selectedRecurrence,
            onSelectRecurrence = { viewModel.selectedRecurrence = it },
            onNext = { localFocusManager.clearFocus() },
        )
        FirstPaymentOption(
            date = viewModel.firstPaymentDate,
            onDateSelected = { viewModel.firstPaymentDate = it },
        )
        ColorOption(
            expenseColor = viewModel.expenseColor,
            onExpenseColorSelected = { viewModel.expenseColor = it },
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(align = Alignment.CenterHorizontally)
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 24.dp),
        ) {
            if (currentData != null) {
                OutlinedButton(
                    onClick = {
                        onDeleteExpense?.invoke(currentData)
                    },
                    colors =
                        ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                    modifier =
                        Modifier
                            .weight(1f)
                            .wrapContentWidth(),
                ) {
                    Text(
                        text = stringResource(Res.string.edit_expense_button_delete),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
            Button(
                onClick = {
                    viewModel.tryCreateUpdatedRecurringExpenseData()?.let { expenseData ->
                        onUpdateExpense(expenseData)
                    }
                },
                modifier =
                    Modifier
                        .weight(1f)
                        .wrapContentWidth(),
            ) {
                Text(
                    text = confirmButtonString,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun EditRecurringExpensePreview() {
    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EditRecurringExpenseInternal(
                onUpdateExpense = {},
                confirmButtonString = "Add Expense",
            )
        }
    }
}
