package ui.editexpense

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_invalid_input

@Composable
fun ExpenseTextField(
    value: String,
    onValueChange: (String) -> Unit,
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
    val supportingText: (@Composable () -> Unit)? =
        if (isError) {
            {
                Text(
                    text = stringResource(Res.string.edit_expense_invalid_input),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        } else {
            null
        }
    val trailingIcon: (@Composable () -> Unit)? =
        if (isError) {
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
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder) },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        isError = isError,
        maxLines = maxLines,
        supportingText = supportingText,
        trailingIcon = trailingIcon,
        modifier = modifier,
    )
}
