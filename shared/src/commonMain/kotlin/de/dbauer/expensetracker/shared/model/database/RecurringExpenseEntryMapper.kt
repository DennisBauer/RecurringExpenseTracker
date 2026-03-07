package de.dbauer.expensetracker.shared.model.database

import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.Recurrence
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Reminder
import de.dbauer.expensetracker.shared.data.Tag
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

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
        isSplit = this.isSplit,
        splitBetweenPeople = this.splitBetweenPeople,
    )
}

internal fun RecurringExpenseEntry.toRecurringExpenseData(
    defaultCurrencyCode: String,
    tags: List<Tag>,
    reminders: List<Reminder>,
): RecurringExpenseData {
    val currencyCode = this.currencyCode.ifBlank { defaultCurrencyCode }
    val recurrence = getUIRecurrenceFromDatabaseInt(this.recurrence ?: RecurrenceDatabase.Monthly.value)
    val price = CurrencyValue(this.price ?: 0f, currencyCode)
    return RecurringExpenseData(
        id = this.id,
        name = this.name ?: "",
        description = this.description ?: "",
        price = price,
        monthlyPrice = getMonthlyPrice(price, this.everyXRecurrence ?: 1, recurrence),
        everyXRecurrence = this.everyXRecurrence ?: 1,
        recurrence = recurrence,
        tags = tags,
        firstPayment =
            this.firstPayment?.milliseconds?.let {
                Instant.fromEpochMilliseconds(
                    it.inWholeMilliseconds,
                )
            },
        notifyForExpense = this.notifyForExpense,
        reminders = reminders,
        isSplit = this.isSplit,
        splitBetweenPeople = this.splitBetweenPeople,
    )
}

private fun getMonthlyPrice(
    price: CurrencyValue,
    everyXRecurrence: Int,
    recurrence: Recurrence,
): CurrencyValue {
    val monthlyValue =
        when (recurrence) {
            Recurrence.Daily -> {
                (365 / 12f) / everyXRecurrence * price.value
            }

            Recurrence.Weekly -> {
                (52 / 12f) / everyXRecurrence * price.value
            }

            Recurrence.Monthly -> {
                1f / everyXRecurrence * price.value
            }

            Recurrence.Yearly -> {
                price.value / (everyXRecurrence * 12f)
            }
        }
    return price.copy(value = monthlyValue)
}

private fun getRecurrenceIntFromUIRecurrence(recurrence: Recurrence): Int {
    return when (recurrence) {
        Recurrence.Daily -> RecurrenceDatabase.Daily.value
        Recurrence.Weekly -> RecurrenceDatabase.Weekly.value
        Recurrence.Monthly -> RecurrenceDatabase.Monthly.value
        Recurrence.Yearly -> RecurrenceDatabase.Yearly.value
    }
}

private fun getUIRecurrenceFromDatabaseInt(recurrence: Int): Recurrence {
    return when (recurrence) {
        RecurrenceDatabase.Daily.value -> Recurrence.Daily
        RecurrenceDatabase.Weekly.value -> Recurrence.Weekly
        RecurrenceDatabase.Monthly.value -> Recurrence.Monthly
        RecurrenceDatabase.Yearly.value -> Recurrence.Yearly
        else -> Recurrence.Monthly
    }
}
