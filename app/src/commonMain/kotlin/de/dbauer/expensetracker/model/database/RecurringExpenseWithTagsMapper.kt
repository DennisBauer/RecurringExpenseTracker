package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import kotlin.time.Instant

internal fun RecurringExpenseWithTagsEntry.toRecurringExpenseData(
    defaultCurrencyCode: String,
): RecurringExpenseData {
    return RecurringExpenseData(
        id = this.expense.id,
        name = this.expense.name!!,
        description = this.expense.description!!,
        price = CurrencyValue(this.expense.price!!, this.expense.currencyCode.ifBlank { defaultCurrencyCode }),
        monthlyPrice =
            CurrencyValue(
                this.expense.getMonthlyPrice(),
                this.expense.currencyCode.ifBlank {
                    defaultCurrencyCode
                },
            ),
        everyXRecurrence = this.expense.everyXRecurrence!!,
        recurrence = getRecurrenceFromDatabaseInt(this.expense.recurrence!!),
        tags = this.tags.toTags().sortedBy { it.title },
        firstPayment = this.expense.firstPayment?.let { Instant.fromEpochMilliseconds(it) },
        notifyForExpense = this.expense.notifyForExpense,
        reminders = this.reminders.toReminders(),
    )
}

private fun getRecurrenceFromDatabaseInt(recurrenceInt: Int): Recurrence {
    return when (recurrenceInt) {
        RecurrenceDatabase.Daily.value -> Recurrence.Daily
        RecurrenceDatabase.Weekly.value -> Recurrence.Weekly
        RecurrenceDatabase.Monthly.value -> Recurrence.Monthly
        RecurrenceDatabase.Yearly.value -> Recurrence.Yearly
        else -> Recurrence.Monthly
    }
}
