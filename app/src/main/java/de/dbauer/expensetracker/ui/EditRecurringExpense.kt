package de.dbauer.expensetracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.toFloatIgnoreSeparator
import de.dbauer.expensetracker.toLocalString
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
        windowInsets = WindowInsets.statusBars,
        modifier = modifier,
    ) {
        EditRecurringExpenseInternal(
            onUpdateExpense = onUpdateExpense,
            confirmButtonString =
                if (currentData == null) {
                    stringResource(R.string.edit_expense_button_add)
                } else {
                    stringResource(
                        R.string.edit_expense_button_update,
                    )
                },
            currentData = currentData,
            onDeleteExpense = onDeleteExpense,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
    var recurrenceExpanded by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val localFocusManager = LocalFocusManager.current

    Column(
        modifier =
            modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
    ) {
        Text(
            text = stringResource(R.string.edit_expense_name),
            style = MaterialTheme.typography.bodyLarge,
        )
        CustomTextField(
            value = nameState,
            onValueChange = { nameState = it },
            placeholder = stringResource(R.string.edit_expense_name_placeholder),
            keyboardActions =
                KeyboardActions(
                    onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
                ),
            singleLine = true,
            isError = nameInputError.value,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        )
        Text(
            text = stringResource(R.string.edit_expense_description),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        CustomTextField(
            value = descriptionState,
            onValueChange = { descriptionState = it },
            placeholder = stringResource(R.string.edit_expense_description_placeholder),
            keyboardActions =
                KeyboardActions(
                    onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
                ),
            maxLines = 2,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        )
        Text(
            text = stringResource(R.string.edit_expense_price),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        CustomTextField(
            value = priceState,
            onValueChange = { priceState = it },
            placeholder = 0f.toLocalString(),
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next,
                ),
            keyboardActions =
                KeyboardActions(
                    onNext = { localFocusManager.moveFocus(FocusDirection.Next) },
                ),
            singleLine = true,
            isError = priceInputError.value,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        )
        Text(
            text = stringResource(R.string.edit_expense_recurrence),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row {
            CustomTextField(
                value = everyXRecurrenceState,
                onValueChange = { everyXRecurrenceState = it },
                placeholder = "1",
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            onConfirmClicked(
                                nameInputError,
                                priceInputError,
                                everyXRecurrenceInputError,
                                nameState,
                                descriptionState,
                                priceState,
                                everyXRecurrenceState,
                                selectedRecurrence,
                                onUpdateExpense,
                                currentData,
                            )
                        },
                    ),
                singleLine = true,
                isError = everyXRecurrenceInputError.value,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            ExposedDropdownMenuBox(
                expanded = recurrenceExpanded,
                onExpandedChange = { recurrenceExpanded = !recurrenceExpanded },
                modifier =
                    Modifier
                        .weight(3f)
                        .padding(vertical = 8.dp),
            ) {
                TextField(
                    value = stringResource(id = selectedRecurrence.fullStringRes),
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.menuAnchor(),
                )
                ExposedDropdownMenu(
                    expanded = recurrenceExpanded,
                    onDismissRequest = { recurrenceExpanded = false },
                ) {
                    Recurrence.entries.forEach {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = it.fullStringRes)) },
                            onClick = {
                                selectedRecurrence = it
                                recurrenceExpanded = false
                            },
                        )
                    }
                }
            }
        }
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

@Composable
private fun CustomTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions =
        KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next,
        ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder) },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = isError,
        maxLines = maxLines,
        supportingText = {
            if (isError) {
                Text(
                    text = stringResource(R.string.edit_expense_invalid_input),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        trailingIcon = {
            if (isError) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
        modifier = modifier,
    )
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
                price = price.toFloatIgnoreSeparator(),
                monthlyPrice = price.toFloatIgnoreSeparator(),
                everyXRecurrence = everyXRecurrence.toIntOrNull() ?: 1,
                recurrence = selectedRecurrence,
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
    val priceConverted = price.replace(",", ".")
    return priceConverted.toFloatOrNull() != null
}

private fun isEveryXRecurrenceValid(everyXRecurrence: String): Boolean {
    return everyXRecurrence.isBlank() || everyXRecurrence.toIntOrNull() != null
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
