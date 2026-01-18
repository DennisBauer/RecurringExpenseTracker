package de.dbauer.expensetracker.shared.model.notification

import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.notification_expense_reminder

@Serializable
data class NotificationData(
    val id: Int,
    val title: String,
    val description: String,
    val channel: NotificationChannel,
    val action: String? = null,
)

enum class NotificationChannel(
    val id: String,
    val displayNameRes: StringResource,
) {
    ExpenseReminder(
        id = "expenseReminderChannel",
        displayNameRes = Res.string.notification_expense_reminder,
    ),
}
