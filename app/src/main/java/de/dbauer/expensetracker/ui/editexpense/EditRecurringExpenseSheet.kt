package de.dbauer.expensetracker.ui.editexpense

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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.toFloatLocaleAware
import de.dbauer.expensetracker.toLocalString
import de.dbauer.expensetracker.ui.customizations.ExpenseColor
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme

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
                    stringResource(R.string.edit_expense_button_add)
                } else {
                    stringResource(
                        R.string.edit_expense_button_save,
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
    onDeleteExpense: ((RecurringExpenseData) -> Unit)? = null,
) {
    var nameState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentData?.name ?: ""))
    }
    val nameInputError =
        rememberSaveable {
            mutableStateOf(false)
        }
    var descriptionState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentData?.description ?: ""))
    }
    var priceState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentData?.price?.toLocalString() ?: ""))
    }
    val priceInputError = rememberSaveable { mutableStateOf(false) }
    var everyXRecurrenceState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentData?.everyXRecurrence?.toString() ?: ""))
    }
    val everyXRecurrenceInputError = rememberSaveable { mutableStateOf(false) }
    var selectedRecurrence by rememberSaveable {
        mutableStateOf(currentData?.recurrence ?: Recurrence.Monthly)
    }
    var firstPaymentDate by rememberSaveable {
        mutableLongStateOf(currentData?.firstPayment ?: 0L)
    }
    var expenseColor by rememberSaveable {
        mutableStateOf(currentData?.color ?: ExpenseColor.Dynamic)
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
            name = nameState,
            onNameChanged = { nameState = it },
            nameInputError = nameInputError.value,
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        DescriptionOption(
            description = descriptionState,
            onDescriptionChanged = { descriptionState = it },
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        PriceOption(
            price = priceState,
            onPriceChanged = { priceState = it },
            priceInputError = priceInputError.value,
            onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
        )
        RecurrenceOption(
            everyXRecurrence = everyXRecurrenceState,
            onEveryXRecurrenceChanged = { everyXRecurrenceState = it },
            everyXRecurrenceInputError = everyXRecurrenceInputError.value,
            selectedRecurrence = selectedRecurrence,
            onSelectRecurrence = { selectedRecurrence = it },
            onNext = { localFocusManager.clearFocus() },
        )
        FirstPaymentOption(
            date = firstPaymentDate,
            onDateSelected = { firstPaymentDate = it },
        )
        ColorOption(
            expenseColor = expenseColor,
            onExpenseColorSelected = { expenseColor = it },
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
                        text = stringResource(R.string.edit_expense_button_delete),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
            Button(
                onClick = {
                    onConfirmClicked(
                        nameInputError,
                        priceInputError,
                        everyXRecurrenceInputError,
                        nameState,
                        descriptionState,
                        priceState,
                        everyXRecurrenceState,
                        selectedRecurrence,
                        firstPaymentDate,
                        expenseColor,
                        onUpdateExpense,
                        currentData,
                    )
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

private fun onConfirmClicked(
    nameInputError: MutableState<Boolean>,
    priceInputError: MutableState<Boolean>,
    everyXRecurrenceInputError: MutableState<Boolean>,
    nameState: TextFieldValue,
    descriptionState: TextFieldValue,
    priceState: TextFieldValue,
    everyXRecurrenceState: TextFieldValue,
    selectedRecurrence: Recurrence,
    firstPayment: Long,
    expenseColor: ExpenseColor,
    onUpdateExpense: (RecurringExpenseData) -> Unit,
    currentData: RecurringExpenseData?,
) {
    nameInputError.value = false
    priceInputError.value = false
    everyXRecurrenceInputError.value = false

    val name = nameState.text
    val description = descriptionState.text
    val price = priceState.text
    val everyXRecurrence = everyXRecurrenceState.text
    if (verifyUserInput(
            name = name,
            onNameInputError = { nameInputError.value = true },
            price = price,
            onPriceInputError = { priceInputError.value = true },
            everyXRecurrence = everyXRecurrence,
            onEveryXRecurrenceError = { everyXRecurrenceInputError.value = true },
        )
    ) {
        onUpdateExpense(
            RecurringExpenseData(
                id = currentData?.id ?: 0,
                name = name,
                description = description,
                price = price.toFloatLocaleAware() ?: 0f,
                monthlyPrice = price.toFloatLocaleAware() ?: 0f,
                everyXRecurrence = everyXRecurrence.toIntOrNull() ?: 1,
                recurrence = selectedRecurrence,
                firstPayment = firstPayment,
                color = expenseColor,
            ),
        )
    }
}

private fun verifyUserInput(
    name: String,
    onNameInputError: () -> Unit,
    price: String,
    onPriceInputError: () -> Unit,
    everyXRecurrence: String,
    onEveryXRecurrenceError: () -> Unit,
): Boolean {
    var everythingCorrect = true
    if (!isNameValid(name)) {
        onNameInputError()
        everythingCorrect = false
    }
    if (!isPriceValid(price)) {
        onPriceInputError()
        everythingCorrect = false
    }
    if (!isEveryXRecurrenceValid(everyXRecurrence)) {
        onEveryXRecurrenceError()
        everythingCorrect = false
    }
    return everythingCorrect
}

private fun isNameValid(name: String): Boolean {
    return name.isNotBlank()
}

private fun isPriceValid(price: String): Boolean {
    return price.toFloatLocaleAware() != null
}

private fun isEveryXRecurrenceValid(everyXRecurrence: String): Boolean {
    return everyXRecurrence.isBlank() || everyXRecurrence.toIntOrNull() != null
}

@PreviewLightDark
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
