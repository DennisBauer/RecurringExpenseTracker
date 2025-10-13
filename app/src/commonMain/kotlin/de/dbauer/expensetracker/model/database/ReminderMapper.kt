package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.Reminder
import kotlin.time.Instant

fun ReminderEntry.toReminder(): Reminder {
    return Reminder(
        id = this.id,
        daysBeforePayment = this.daysBeforePayment,
        lastNotificationDate =
            this.lastNotificationDate?.let {
                Instant.fromEpochMilliseconds(it)
            },
    )
}

fun Reminder.toReminderEntry(expenseId: Int): ReminderEntry {
    return ReminderEntry(
        id = this.id,
        expenseId = expenseId,
        daysBeforePayment = this.daysBeforePayment,
        lastNotificationDate = this.lastNotificationDate?.toEpochMilliseconds(),
    )
}

fun List<ReminderEntry>.toReminders(): List<Reminder> {
    return this.map { it.toReminder() }
}
