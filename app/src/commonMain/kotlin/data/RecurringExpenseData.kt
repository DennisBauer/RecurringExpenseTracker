package data

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
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
import ui.customizations.ExpenseColor

enum class Recurrence(
    val fullStringRes: StringResource,
    val shortStringRes: StringResource,
) {
    Daily(Res.string.edit_expense_recurrence_day, Res.string.edit_expense_recurrence_day_short),
    Weekly(Res.string.edit_expense_recurrence_week, Res.string.edit_expense_recurrence_week_short),
    Monthly(Res.string.edit_expense_recurrence_month, Res.string.edit_expense_recurrence_month_short),
    Yearly(Res.string.edit_expense_recurrence_year, Res.string.edit_expense_recurrence_year_short),
}

@Immutable
data class RecurringExpenseData(
    val id: Int,
    val name: String,
    val description: String,
    val price: CurrencyValue,
    val monthlyPrice: CurrencyValue,
    val everyXRecurrence: Int,
    val recurrence: Recurrence,
    val firstPayment: Instant?,
    val color: ExpenseColor,
    val notifyForExpense: Boolean,
    val notifyXDaysBefore: Int?,
    val lastNotificationDate: Instant?,
)
