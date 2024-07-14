package viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.Recurrence
import data.RecurringExpenseData
import data.UpcomingPaymentData
import isInDaysAfter
import isSameDay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ui.customizations.ExpenseColor
import viewmodel.database.ExpenseRepository
import viewmodel.database.RecurrenceDatabase
import viewmodel.database.RecurringExpense

class UpcomingPaymentsViewModel(
    private val expenseRepository: ExpenseRepository?,
) : ViewModel() {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPaymentData>()
    val upcomingPaymentsData: List<UpcomingPaymentData>
        get() = _upcomingPaymentsData

    init {
        viewModelScope.launch {
            expenseRepository?.allRecurringExpensesByPrice?.collect { recurringExpenses ->
                onDatabaseUpdated(recurringExpenses)
            }
        }
    }

    fun onExpenseWithIdClicked(
        expenceId: Int,
        onItemClicked: (RecurringExpenseData) -> Unit,
    ) {
        viewModelScope.launch {
            expenseRepository?.getRecurringExpenseById(expenceId)?.let {
                val recurringExpenseData =
                    RecurringExpenseData(
                        id = it.id,
                        name = it.name!!,
                        description = it.description!!,
                        price = it.price!!,
                        monthlyPrice = it.getMonthlyPrice(),
                        everyXRecurrence = it.everyXRecurrence!!,
                        recurrence = getRecurrenceFromDatabaseInt(it.recurrence!!),
                        firstPayment = it.firstPayment?.let { Instant.fromEpochMilliseconds(it) },
                        color = ExpenseColor.fromInt(it.color),
                    )
                onItemClicked(recurringExpenseData)
            }
        }
    }

    private fun onDatabaseUpdated(recurringExpenses: List<RecurringExpense>) {
        _upcomingPaymentsData.clear()
        recurringExpenses.forEach { expense ->
            expense.firstPayment?.let { Instant.fromEpochMilliseconds(it) }?.let { firstPayment ->
                val nextPaymentDay =
                    getNextPaymentDay(firstPayment, expense.everyXRecurrence!!, expense.recurrence!!)
                val nextPaymentRemainingDays = getNextPaymentDays(nextPaymentDay)
                val nextPaymentDate = nextPaymentDay.toString()
                _upcomingPaymentsData.add(
                    UpcomingPaymentData(
                        id = expense.id,
                        name = expense.name!!,
                        price = expense.price!!,
                        nextPaymentRemainingDays = nextPaymentRemainingDays,
                        nextPaymentDate = nextPaymentDate,
                        color = ExpenseColor.fromInt(expense.color),
                    ),
                )
            }
        }
        _upcomingPaymentsData.sortBy { it.nextPaymentRemainingDays }
    }

    private fun getNextPaymentDay(
        firstPayment: Instant,
        everyXRecurrence: Int,
        recurrence: Int,
    ): LocalDate {
        val today =
            Clock.System
                .now()
                .toLocalDateTime(TimeZone.UTC)
                .date
        var nextPayment = firstPayment.toLocalDateTime(TimeZone.UTC).date

        while (today.isInDaysAfter(nextPayment) && !today.isSameDay(nextPayment)) {
            val field =
                when (recurrence) {
                    RecurrenceDatabase.Daily.value -> DateTimeUnit.DAY
                    RecurrenceDatabase.Weekly.value -> DateTimeUnit.WEEK
                    RecurrenceDatabase.Monthly.value -> DateTimeUnit.MONTH
                    RecurrenceDatabase.Yearly.value -> DateTimeUnit.YEAR
                    else -> DateTimeUnit.MONTH
                }
            nextPayment = nextPayment.plus(everyXRecurrence, field)
        }
        return nextPayment
    }

    private fun getNextPaymentDays(nextPaymentDay: LocalDate): Int {
        return Clock.System.now().daysUntil(nextPaymentDay.atStartOfDayIn(TimeZone.UTC), TimeZone.UTC)
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
}
