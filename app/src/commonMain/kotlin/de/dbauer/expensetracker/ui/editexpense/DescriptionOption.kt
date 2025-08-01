package de.dbauer.expensetracker.ui.editexpense

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
import recurringexpensetracker.app.generated.resources.edit_expense_description
import recurringexpensetracker.app.generated.resources.edit_expense_description_placeholder

@Composable
fun DescriptionOption(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onNext: KeyboardActionScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.edit_expense_description),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        ExpenseTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = stringResource(Res.string.edit_expense_description_placeholder),
            keyboardActions =
                KeyboardActions(onNext = onNext),
            maxLines = 2,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        )
    }
}
