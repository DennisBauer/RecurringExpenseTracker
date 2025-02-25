package ui.editexpense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_notification_get_notified
import recurringexpensetracker.app.generated.resources.settings_notifications_schedule_days
import recurringexpensetracker.app.generated.resources.settings_notifications_upcoming

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationOption(
    expenseNotificationEnabledGlobally: Boolean,
    notifyForExpense: Boolean,
    onNotifyForExpenseChange: (Boolean) -> Unit,
    notifyXDaysBefore: String,
    defaultXDaysPlaceholder: String,
    onNotifyXDaysBeforeChange: (String) -> Unit,
    notifyXDaysBeforeInputError: Boolean,
    onNext: KeyboardActionScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.sizeIn(minHeight = 64.dp),
        ) {
            Text(
                text = stringResource(Res.string.settings_notifications_upcoming),
                style = MaterialTheme.typography.bodyLarge,
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .weight(1f),
            )
            Switch(
                enabled = expenseNotificationEnabledGlobally,
                checked = expenseNotificationEnabledGlobally && notifyForExpense,
                onCheckedChange = onNotifyForExpenseChange,
            )
        }
        if (expenseNotificationEnabledGlobally) {
            if (notifyForExpense) {
                Text(
                    text = stringResource(Res.string.settings_notifications_schedule_days),
                    style = MaterialTheme.typography.bodyLarge,
                )
                ExpenseTextField(
                    value = notifyXDaysBefore,
                    onValueChange = {
                        if (it.matches(Regex("\\d{0,2}"))) {
                            onNotifyXDaysBeforeChange(it)
                        }
                    },
                    placeholder = defaultXDaysPlaceholder,
                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                    keyboardActions =
                        KeyboardActions(onNext = onNext),
                    singleLine = true,
                    isError = notifyXDaysBeforeInputError,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                )
            }
        } else {
            Text(
                text = stringResource(Res.string.edit_expense_notification_get_notified),
                style = MaterialTheme.typography.bodyMedium,
                modifier =
                    Modifier
                        .padding(bottom = 8.dp),
            )
        }
    }
}
