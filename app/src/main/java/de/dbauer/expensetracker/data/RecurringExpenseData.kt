package de.dbauer.expensetracker.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.ui.customizations.ExpenseColor

enum class Recurrence(
    @StringRes val fullStringRes: Int,
    @StringRes val shortStringRes: Int,
) {
    Daily(R.string.edit_expense_recurrence_day, R.string.edit_expense_recurrence_day_short),
    Weekly(R.string.edit_expense_recurrence_week, R.string.edit_expense_recurrence_week_short),
    Monthly(R.string.edit_expense_recurrence_month, R.string.edit_expense_recurrence_month_short),
    Yearly(R.string.edit_expense_recurrence_year, R.string.edit_expense_recurrence_year_short),
}

@Immutable
data class RecurringExpenseData(
    val id: Int,
    val name: String,
    val description: String,
    val price: Float,
    val monthlyPrice: Float,
    val everyXRecurrence: Int,
    val recurrence: Recurrence,
    val firstPayment: Long,
    val color: ExpenseColor,
)
