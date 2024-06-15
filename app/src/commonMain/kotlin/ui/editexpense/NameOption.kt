package ui.editexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_name
import recurringexpensetracker.app.generated.resources.edit_expense_name_placeholder

@Composable
fun NameOption(
    name: String,
    onNameChange: (String) -> Unit,
    nameInputError: Boolean,
    onNext: KeyboardActionScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.edit_expense_name),
            style = MaterialTheme.typography.bodyLarge,
        )
        ExpenseTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = stringResource(Res.string.edit_expense_name_placeholder),
            keyboardActions =
                KeyboardActions(onNext = onNext),
            singleLine = true,
            isError = nameInputError,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        )
    }
}
