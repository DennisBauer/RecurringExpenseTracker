package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.model.database.EntryTag.Companion.toTags
import kotlin.time.Instant

internal fun EntryRecurringExpenseWithTags.toRecurringExpenseData(
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
        tags = this.tags.toTags(),
        firstPayment = this.expense.firstPayment?.let { Instant.fromEpochMilliseconds(it) },
        notifyForExpense = this.expense.notifyForExpense,
        notifyXDaysBefore = this.expense.notifyXDaysBefore,
        lastNotificationDate = this.expense.lastNotificationDate?.let { Instant.fromEpochMilliseconds(it) },
    )
}

internal fun RecurringExpenseData.toEntryRecurringExpense(defaultCurrencyCode: String): EntryRecurringExpense {
    return EntryRecurringExpense(
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

private fun getRecurrenceFromDatabaseInt(recurrenceInt: Int): Recurrence {
    return when (recurrenceInt) {
        RecurrenceDatabase.Daily.value -> Recurrence.Daily
        RecurrenceDatabase.Weekly.value -> Recurrence.Weekly
        RecurrenceDatabase.Monthly.value -> Recurrence.Monthly
        RecurrenceDatabase.Yearly.value -> Recurrence.Yearly
        else -> Recurrence.Monthly
    }
}

private fun getRecurrenceIntFromUIRecurrence(recurrence: Recurrence): Int {
    return when (recurrence) {
        Recurrence.Daily -> RecurrenceDatabase.Daily.value
        Recurrence.Weekly -> RecurrenceDatabase.Weekly.value
        Recurrence.Monthly -> RecurrenceDatabase.Monthly.value
        Recurrence.Yearly -> RecurrenceDatabase.Yearly.value
    }
}
