package de.dbauer.expensetracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.toFloatIgnoreSeparator
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecurringExpense(
    onUpdateExpense: (RecurringExpenseData) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    currentData: RecurringExpenseData? = null,
) {
    val sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        EditRecurringExpenseInternal(
            onUpdateExpense = onUpdateExpense,
            confirmButtonString = if (currentData == null) "Add Expense" else "Update Expense",
            currentData = currentData,
        )
    }
}

@Composable
private fun EditRecurringExpenseInternal(
    onUpdateExpense: (RecurringExpenseData) -> Unit,
    confirmButtonString: String,
    modifier: Modifier = Modifier,
    currentData: RecurringExpenseData? = null,
) {
    var nameState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentData?.name ?: ""))
    }
    var nameInputError by rememberSaveable {
        mutableStateOf(false)
    }
    var descriptionState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentData?.description ?: ""))
    }
    var descriptionInputError by rememberSaveable {
        mutableStateOf(false)
    }
    var priceState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(currentData?.priceValue.toString()))
    }
    var priceInputError by rememberSaveable {
        mutableStateOf(false)
    }

    Column(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Name",
            style = MaterialTheme.typography.bodyLarge,
        )
        CustomTextField(
            value = nameState,
            onValueChange = { nameState = it },
            placeholder = "e.g. Netflix",
            singleLine = true,
            isError = nameInputError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(
            text = "Description",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        CustomTextField(
            value = descriptionState,
            onValueChange = { descriptionState = it },
            placeholder = "e.g. special subscription",
            maxLines = 2,
            isError = descriptionInputError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Text(
            text = "Price",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp)
        )
        CustomTextField(
            value = priceState,
            onValueChange = { priceState = it },
            placeholder = "0,00",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            isError = priceInputError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Button(
            onClick = {
                nameInputError = false
                descriptionInputError = false
                priceInputError = false

                val name = nameState.text
                val description = descriptionState.text
                val price = priceState.text
                if (verifyUserInput(name = name,
                        onNameInputError = { nameInputError = true },
                        description = description,
                        onDescriptionInputError = { descriptionInputError = true },
                        price = price,
                        onPriceInputError = { priceInputError = true })
                ) {
                    onUpdateExpense(
                        RecurringExpenseData(
                            id = currentData?.id ?: 0,
                            name = name,
                            description = description,
                            priceValue = price.toFloatIgnoreSeparator()
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentWidth(align = Alignment.CenterHorizontally)
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Text(
                text = confirmButtonString,
                modifier = Modifier.padding(vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun CustomTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder) },
        keyboardOptions = keyboardOptions,
        singleLine = singleLine,
        isError = isError,
        maxLines = maxLines,
        supportingText = {
            if (isError) {
                Text(text = "Invalid input", color = MaterialTheme.colorScheme.error)
            }
        },
        trailingIcon = {
            if (isError) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        modifier = modifier
    )
}

private fun verifyUserInput(
    name: String,
    onNameInputError: () -> Unit,
    description: String,
    onDescriptionInputError: () -> Unit,
    price: String,
    onPriceInputError: () -> Unit,
): Boolean {
    var everythingCorrect = true
    if (!isNameValid(name)) {
        onNameInputError()
        everythingCorrect = false
    }
    if (!isDescriptionValid(description)) {
        onDescriptionInputError()
        everythingCorrect = false
    }
    if (!isPriceValid(price)) {
        onPriceInputError()
        everythingCorrect = false
    }
    return everythingCorrect
}

private fun isNameValid(name: String): Boolean {
    return name.isNotBlank()
}

private fun isDescriptionValid(description: String): Boolean {
    return description.isNotBlank()
}

private fun isPriceValid(price: String): Boolean {
    val priceConverted = price.replace(",", ".")
    return priceConverted.toFloatOrNull() != null
}

@Preview
@Composable
private fun EditRecurringExpensePreview() {
    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            EditRecurringExpenseInternal(
                onUpdateExpense = {}, confirmButtonString = "Add Expense"
            )
        }
    }
}