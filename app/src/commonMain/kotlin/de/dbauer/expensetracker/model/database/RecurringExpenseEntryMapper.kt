package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData

internal fun RecurringExpenseData.toEntryRecurringExpense(defaultCurrencyCode: String): RecurringExpenseEntry {
    return RecurringExpenseEntry(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price.value,
        everyXRecurrence = this.everyXRecurrence,
        recurrence = getRecurrenceIntFromUIRecurrence(this.recurrence),
        firstPayment = this.firstPayment?.toEpochMilliseconds(),
        currencyCode = if (defaultCurrencyCode != this.price.currencyCode) this.price.currencyCode else "",
        notifyForExpense = this.notifyForExpense,
        notifyXDaysBefore = this.notifyXDaysBefore,
        lastNotificationDate = this.lastNotificationDate?.toEpochMilliseconds(),
    )
}

private fun getRecurrenceIntFromUIRecurrence(recurrence: Recurrence): Int {
    return when (recurrence) {
        Recurrence.Daily -> RecurrenceDatabase.Daily.value
        Recurrence.Weekly -> RecurrenceDatabase.Weekly.value
        Recurrence.Monthly -> RecurrenceDatabase.Monthly.value
        Recurrence.Yearly -> RecurrenceDatabase.Yearly.value
    }
}
