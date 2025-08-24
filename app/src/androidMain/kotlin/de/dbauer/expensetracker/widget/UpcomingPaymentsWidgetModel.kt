package de.dbauer.expensetracker.widget

import androidx.compose.runtime.mutableStateListOf
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.UpcomingPaymentData
import de.dbauer.expensetracker.model.DateTimeCalculator
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.toLocaleString
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Instant

class UpcomingPaymentsWidgetModel(
    private val expenseRepository: IExpenseRepository,
) {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPaymentData>()
    val upcomingPaymentsData: List<UpcomingPaymentData>
        get() = _upcomingPaymentsData

    suspend fun init() {
        expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
            onDatabaseUpdated(recurringExpenses)
        }
    }

    private fun onDatabaseUpdated(recurringExpenses: List<RecurringExpenseData>) {
        _upcomingPaymentsData.clear()
        recurringExpenses.forEach { expense ->
            expense.firstPayment?.let { firstPayment ->
                val nextPaymentDay =
                    getNextPaymentDay(firstPayment, expense.everyXRecurrence, expense.recurrence)
                val nextPaymentRemainingDays = DateTimeCalculator.getDaysFromNowUntil(nextPaymentDay)
                val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
                _upcomingPaymentsData.add(
                    UpcomingPaymentData(
                        id = expense.id,
                        name = expense.name,
                        price = expense.price,
                        nextPaymentRemainingDays = nextPaymentRemainingDays,
                        nextPaymentDate = nextPaymentDate,
                        tags = expense.tags,
                    ),
                )
            }
        }
        _upcomingPaymentsData.sortBy { it.nextPaymentRemainingDays }
    }

    private fun getNextPaymentDay(
        firstPayment: Instant,
        everyXRecurrence: Int,
        recurrence: Recurrence,
    ): LocalDate {
        return DateTimeCalculator.getDayOfNextOccurrenceFromNow(
            from = firstPayment,
            everyXRecurrence = everyXRecurrence,
            recurrence =
                when (recurrence) {
                    Recurrence.Daily -> DateTimeUnit.DAY
                    Recurrence.Weekly -> DateTimeUnit.WEEK
                    Recurrence.Monthly -> DateTimeUnit.MONTH
                    Recurrence.Yearly -> DateTimeUnit.YEAR
                },
        )
    }
}
