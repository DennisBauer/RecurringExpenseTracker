package ui.editexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import data.CurrencyOption
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_invalid_input
import recurringexpensetracker.app.generated.resources.edit_expense_price
import toLocalString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceOption(
    price: String,
    onPriceChange: (String) -> Unit,
    priceInputError: Boolean,
    currencyInputError: Boolean,
    selectedCurrencyOption: CurrencyOption,
    availableCurrencyOptions: List<CurrencyOption>,
    onSelectCurrencyOption: (CurrencyOption) -> Unit,
    onNext: KeyboardActionScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    var currencyOptionsExpanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.edit_expense_price),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row {
            ExpenseTextField(
                value = price,
                onValueChange = onPriceChange,
                placeholder = 0f.toLocalString(),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next,
                    ),
                keyboardActions =
                    KeyboardActions(onNext = onNext),
                singleLine = true,
                isError = priceInputError,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
            )
            Spacer(modifier = Modifier.size(8.dp))
            var currencyInputText by rememberSaveable(selectedCurrencyOption) {
                mutableStateOf(selectedCurrencyOption.currencyName)
            }
            val filteredOptions =
                availableCurrencyOptions.filter {
                    it.currencyName.contains(currencyInputText, ignoreCase = true)
                }
            ExposedDropdownMenuBox(
                expanded = currencyOptionsExpanded,
                onExpandedChange = { currencyOptionsExpanded = !currencyOptionsExpanded },
                modifier =
                    Modifier
                        .weight(2f)
                        .padding(vertical = 8.dp),
            ) {
                CurrencyTextField(
                    currencyInputText = currencyInputText,
                    onCurrencyInputTextChange = { currencyInputText = it },
                    onCurrencyOptionsExpandedChange = { currencyOptionsExpanded = it },
                    currencyInputError = currencyInputError,
                    onSelectCurrencyOption = onSelectCurrencyOption,
                    onNext = onNext,
                )
                ExposedDropdownMenu(
                    expanded = currencyOptionsExpanded,
                    onDismissRequest = { currencyOptionsExpanded = false },
                ) {
                    filteredOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(text = it.currencyName) },
                            onClick = {
                                onSelectCurrencyOption(it)
                                currencyInputText = it.currencyName
                                currencyOptionsExpanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.CurrencyTextField(
    currencyInputText: String,
    onCurrencyInputTextChange: (String) -> Unit,
    onCurrencyOptionsExpandedChange: (Boolean) -> Unit,
    currencyInputError: Boolean,
    onSelectCurrencyOption: (CurrencyOption) -> Unit,
    onNext: KeyboardActionScope.() -> Unit,
) {
    val supportingText: (@Composable () -> Unit)? =
        if (currencyInputError) {
            {
                Text(
                    text = stringResource(Res.string.edit_expense_invalid_input),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            null
        }

    TextField(
        value = currencyInputText,
        onValueChange = { newText ->
            onCurrencyInputTextChange(newText)
            onCurrencyOptionsExpandedChange(true)
        },
        trailingIcon = {
            if (currencyInputError || currencyInputText.isEmpty()) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.error,
                )
            } else if (currencyInputText.isNotEmpty()) {
                IconButton(
                    onClick = {
                        onCurrencyInputTextChange("")
                        onSelectCurrencyOption(CurrencyOption.INVALID)
                    },
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }
        },
        isError = currencyInputError,
        supportingText = supportingText,
        singleLine = true,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        keyboardActions =
            KeyboardActions(onNext = onNext),
        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable),
    )
}
