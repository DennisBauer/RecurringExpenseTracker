package de.dbauer.expensetracker.data

import de.dbauer.expensetracker.model.DateTimeCalculator
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.StringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_day
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_day_short
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_month
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_month_short
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_week
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_week_short
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_year
import recurringexpensetracker.app.generated.resources.edit_expense_recurrence_year_short
import kotlin.time.Instant

enum class Recurrence(
    val fullStringRes: StringResource,
    val shortStringRes: StringResource,
) {
    Daily(Res.string.edit_expense_recurrence_day, Res.string.edit_expense_recurrence_day_short),
    Weekly(Res.string.edit_expense_recurrence_week, Res.string.edit_expense_recurrence_week_short),
    Monthly(Res.string.edit_expense_recurrence_month, Res.string.edit_expense_recurrence_month_short),
    Yearly(Res.string.edit_expense_recurrence_year, Res.string.edit_expense_recurrence_year_short),
}

data class RecurringExpenseData(
    val id: Int,
    val name: String,
    val description: String,
    val price: CurrencyValue,
    val monthlyPrice: CurrencyValue,
    val everyXRecurrence: Int,
    val recurrence: Recurrence,
    val firstPayment: Instant?,
    val notifyForExpense: Boolean,
    val notifyXDaysBefore: Int?,
    val lastNotificationDate: Instant?,
) {
    fun getNextPaymentDay(): LocalDate? {
        if (firstPayment == null) return null

        return DateTimeCalculator.getDayOfNextOccurrenceFromNow(
            from = firstPayment,
            everyXRecurrence = everyXRecurrence,
            recurrence = recurrence.toDateTimeUnit(),
        )
    }

    fun getNextPaymentDayAfter(afterDate: LocalDate): LocalDate? {
        if (firstPayment == null) return null

        return DateTimeCalculator.getDayOfNextOccurrence(
            afterDay = afterDate,
            first = firstPayment,
            everyXRecurrence = everyXRecurrence,
            recurrence = recurrence.toDateTimeUnit(),
        )
    }
}

private fun Recurrence?.toDateTimeUnit(): DateTimeUnit.DateBased {
    return when (this) {
        Recurrence.Daily -> DateTimeUnit.DAY
        Recurrence.Weekly -> DateTimeUnit.WEEK
        Recurrence.Monthly -> DateTimeUnit.MONTH
        Recurrence.Yearly -> DateTimeUnit.YEAR
        else -> DateTimeUnit.MONTH
    }
}
