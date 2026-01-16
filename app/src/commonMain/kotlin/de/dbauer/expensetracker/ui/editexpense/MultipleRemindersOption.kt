package de.dbauer.expensetracker.ui.editexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.data.Reminder
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerThemePreview
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_notification_add_reminder
import recurringexpensetracker.app.generated.resources.edit_expense_notification_add_reminder_content_desc
import recurringexpensetracker.app.generated.resources.edit_expense_notification_days_before_payment
import recurringexpensetracker.app.generated.resources.edit_expense_notification_get_notified
import recurringexpensetracker.app.generated.resources.edit_expense_notification_remove_reminder_content_desc
import recurringexpensetracker.app.generated.resources.settings_notifications_upcoming

@Composable
fun MultipleRemindersOption(
    expenseNotificationEnabledGlobally: Boolean,
    notifyForExpense: Boolean,
    onNotifyForExpenseChange: (Boolean) -> Unit,
    reminders: List<Reminder>,
    onAddReminder: (Int) -> Unit,
    onUpdateReminder: (Int, Int) -> Unit,
    onRemoveReminder: (Int) -> Unit,
    isReminderDuplicate: (Int, Int) -> Boolean,
    isNewReminderDuplicate: (Int) -> Boolean,
    modifier: Modifier = Modifier,
) {
    var newReminderDays by remember { mutableStateOf("") }

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
                // Display existing reminders
                reminders.forEachIndexed { index, reminder ->
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        var reminderText by remember(index) {
                            mutableStateOf(reminder.daysBeforePayment.toString())
                        }

                        val isDuplicate =
                            reminderText.toIntOrNull()?.let { days ->
                                isReminderDuplicate(index, days)
                            } ?: false

                        ExpenseTextField(
                            value = reminderText,
                            onValueChange = {
                                if (it.matches(Regex("\\d{0,2}"))) {
                                    reminderText = it
                                    it.toIntOrNull()?.let { days ->
                                        onUpdateReminder(index, days)
                                    }
                                }
                            },
                            visualTransformation =
                                DaysBeforePaymentTransformation(
                                    stringResource(Res.string.edit_expense_notification_days_before_payment),
                                ),
                            placeholder = "",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            isError = isDuplicate,
                            modifier = Modifier.weight(1f),
                        )

                        IconButton(
                            onClick = { onRemoveReminder(index) },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription =
                                    stringResource(
                                        Res.string.edit_expense_notification_remove_reminder_content_desc,
                                    ),
                            )
                        }
                    }
                }

                // Add new reminder section
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val isNewDuplicate =
                        newReminderDays.toIntOrNull()?.let { days ->
                            isNewReminderDuplicate(days)
                        } ?: false

                    ExpenseTextField(
                        value = newReminderDays,
                        onValueChange = {
                            if (it.matches(Regex("\\d{0,2}"))) {
                                newReminderDays = it
                            }
                        },
                        visualTransformation =
                            DaysBeforePaymentTransformation(
                                stringResource(Res.string.edit_expense_notification_days_before_payment),
                            ),
                        placeholder = "",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = isNewDuplicate,
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedButton(
                        onClick = {
                            newReminderDays.toIntOrNull()?.let { days ->
                                onAddReminder(days)
                                newReminderDays = ""
                            }
                        },
                        enabled = newReminderDays.toIntOrNull() != null && !isNewDuplicate,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription =
                                stringResource(
                                    Res.string.edit_expense_notification_add_reminder_content_desc,
                                ),
                        )
                        Text(
                            text = stringResource(Res.string.edit_expense_notification_add_reminder),
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        } else {
            Text(
                text = stringResource(Res.string.edit_expense_notification_get_notified),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
    }
}

private class DaysBeforePaymentTransformation(
    private val formatString: String,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        val displayText =
            if (originalText.isEmpty()) {
                ""
            } else {
                formatString.replace("%1\$s", originalText)
            }

        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return offset
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return offset.coerceAtMost(originalText.length)
                }
            }

        return TransformedText(
            text = AnnotatedString(displayText),
            offsetMapping = offsetMapping,
        )
    }
}

@Composable
@Preview
private fun MultipleRemindersOptionPreview() {
    ExpenseTrackerThemePreview {
        Surface {
            MultipleRemindersOption(
                expenseNotificationEnabledGlobally = true,
                notifyForExpense = true,
                onNotifyForExpenseChange = {},
                reminders =
                    listOf(
                        Reminder(id = 1, daysBeforePayment = 1),
                        Reminder(id = 2, daysBeforePayment = 3),
                        Reminder(id = 3, daysBeforePayment = 7),
                    ),
                onAddReminder = {},
                onUpdateReminder = { _, _ -> },
                onRemoveReminder = {},
                isReminderDuplicate = { _, _ -> false },
                isNewReminderDuplicate = { false },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
