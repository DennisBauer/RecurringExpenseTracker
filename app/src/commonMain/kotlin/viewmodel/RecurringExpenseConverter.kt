package viewmodel

import data.Recurrence
import data.RecurringExpenseData
import kotlinx.datetime.Instant
import ui.customizations.ExpenseColor
import viewmodel.database.RecurrenceDatabase
import viewmodel.database.RecurringExpense

internal fun RecurringExpense.toFrontendType(): RecurringExpenseData {
    return RecurringExpenseData(
        id = this.id,
        name = this.name!!,
        description = this.description!!,
        price = this.price!!,
        monthlyPrice = this.getMonthlyPrice(),
        everyXRecurrence = this.everyXRecurrence!!,
        recurrence = getRecurrenceFromDatabaseInt(this.recurrence!!),
        firstPayment = this.firstPayment?.let { Instant.fromEpochMilliseconds(it) },
        color = ExpenseColor.fromInt(this.color),
    )
}

internal fun RecurringExpenseData.toBackendType(): RecurringExpense {
    return RecurringExpense(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        everyXRecurrence = this.everyXRecurrence,
        recurrence = getRecurrenceIntFromUIRecurrence(this.recurrence),
        firstPayment = this.firstPayment?.toEpochMilliseconds(),
        color = this.color.toInt(),
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
