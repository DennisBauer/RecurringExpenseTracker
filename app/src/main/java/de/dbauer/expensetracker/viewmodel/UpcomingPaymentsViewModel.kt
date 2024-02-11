package de.dbauer.expensetracker.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.UpcomingPaymentData
import de.dbauer.expensetracker.helper.UtcDateFormat
import de.dbauer.expensetracker.isInDaysAfter
import de.dbauer.expensetracker.isSameDay
import de.dbauer.expensetracker.ui.customizations.ExpenseColor
import de.dbauer.expensetracker.viewmodel.database.ExpenseRepository
import de.dbauer.expensetracker.viewmodel.database.RecurrenceDatabase
import de.dbauer.expensetracker.viewmodel.database.RecurringExpense
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class UpcomingPaymentsViewModel(
    private val expenseRepository: ExpenseRepository?,
) : ViewModel() {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPaymentData>()
    val upcomingPaymentsData: ImmutableList<UpcomingPaymentData>
        get() = _upcomingPaymentsData.toImmutableList()

    init {
        viewModelScope.launch {
            expenseRepository?.allRecurringExpensesByPrice?.collect { recurringExpenses ->
                onDatabaseUpdated(recurringExpenses)
            }
        }
    }

    fun onDatabaseRestored() {
        viewModelScope.launch {
            expenseRepository?.allRecurringExpensesByPrice?.first()?.let { recurringExpenses ->
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
                        firstPayment = it.firstPayment!!,
                        color = ExpenseColor.fromInt(it.color),
                    )
                onItemClicked(recurringExpenseData)
            }
        }
    }

    private fun onDatabaseUpdated(recurringExpenses: List<RecurringExpense>) {
        _upcomingPaymentsData.clear()
        recurringExpenses.forEach {
            val firstPayment = it.firstPayment!!
            val nextPaymentInMilliseconds =
                getNextPaymentInMilliseconds(firstPayment, it.everyXRecurrence!!, it.recurrence!!)
            val nextPaymentRemainingDays = getNextPaymentDays(nextPaymentInMilliseconds)
            val nextPaymentDate = UtcDateFormat.getDateInstance().format(Date(nextPaymentInMilliseconds))
            if (firstPayment > 0L) {
                _upcomingPaymentsData.add(
                    UpcomingPaymentData(
                        id = it.id,
                        name = it.name!!,
                        price = it.price!!,
                        nextPaymentRemainingDays = nextPaymentRemainingDays,
                        nextPaymentDate = nextPaymentDate,
                        color = ExpenseColor.fromInt(it.color),
                    ),
                )
            }
        }
        _upcomingPaymentsData.sortBy { it.nextPaymentRemainingDays }
    }

    private fun getNextPaymentInMilliseconds(
        firstPayment: Long,
        everyXRecurrence: Int,
        recurrence: Int,
    ): Long {
        val today = Calendar.getInstance()
        val nextPayment = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        nextPayment.timeInMillis = firstPayment

        while (today.isInDaysAfter(nextPayment) && !today.isSameDay(nextPayment)) {
            val field =
                when (recurrence) {
                    RecurrenceDatabase.Daily.value -> Calendar.DAY_OF_MONTH
                    RecurrenceDatabase.Weekly.value -> Calendar.WEEK_OF_YEAR
                    RecurrenceDatabase.Monthly.value -> Calendar.MONTH
                    RecurrenceDatabase.Yearly.value -> Calendar.YEAR
                    else -> Calendar.DAY_OF_MONTH
                }
            nextPayment.add(field, everyXRecurrence)
        }
        return nextPayment.timeInMillis
    }

    private fun getNextPaymentDays(nextPaymentInMilliseconds: Long): Int {
        val todayInUTC =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                val now = Calendar.getInstance()
                set(Calendar.YEAR, now.get(Calendar.YEAR))
                set(Calendar.MONTH, now.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        val difference = nextPaymentInMilliseconds - todayInUTC.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(difference).toInt()
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

    companion object {
        fun create(expenseRepository: ExpenseRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    UpcomingPaymentsViewModel(expenseRepository)
                }
            }
        }
    }
}
