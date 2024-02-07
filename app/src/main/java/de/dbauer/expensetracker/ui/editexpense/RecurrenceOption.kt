package de.dbauer.expensetracker.ui.editexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.data.Recurrence

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceOption(
    everyXRecurrence: TextFieldValue,
    onEveryXRecurrenceChanged: (TextFieldValue) -> Unit,
    everyXRecurrenceInputError: Boolean,
    selectedRecurrence: Recurrence,
    onSelectRecurrence: (Recurrence) -> Unit,
    onNext: KeyboardActionScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    var recurrenceExpanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.edit_expense_recurrence),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Row {
            ExpenseTextField(
                value = everyXRecurrence,
                onValueChange = onEveryXRecurrenceChanged,
                placeholder = "1",
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                keyboardActions =
                    KeyboardActions(onNext = onNext),
                singleLine = true,
                isError = everyXRecurrenceInputError,
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
                        .weight(2f)
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
                                onSelectRecurrence(it)
                                recurrenceExpanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}
