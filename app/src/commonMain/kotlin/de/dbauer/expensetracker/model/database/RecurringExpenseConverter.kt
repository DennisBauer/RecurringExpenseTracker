package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import kotlin.time.Instant

internal fun EntryRecurringExpense.toRecurringExpenseData(defaultCurrencyCode: String): RecurringExpenseData {
    return RecurringExpenseData(
        id = this.id,
        name = this.name!!,
        description = this.description!!,
        price = CurrencyValue(this.price!!, this.currencyCode.ifBlank { defaultCurrencyCode }),
        monthlyPrice = CurrencyValue(this.getMonthlyPrice(), this.currencyCode.ifBlank { defaultCurrencyCode }),
        everyXRecurrence = this.everyXRecurrence!!,
        recurrence = getRecurrenceFromDatabaseInt(this.recurrence!!),
        firstPayment = this.firstPayment?.let { Instant.fromEpochMilliseconds(it) },
        notifyForExpense = this.notifyForExpense,
        notifyXDaysBefore = this.notifyXDaysBefore,
        lastNotificationDate = this.lastNotificationDate?.let { Instant.fromEpochMilliseconds(it) },
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
