package viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.CurrencyValue
import data.RecurringExpenseData
import data.UpcomingPaymentData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import model.DateTimeCalculator
import model.database.ExpenseRepository
import model.database.RecurrenceDatabase
import model.database.RecurringExpense
import model.database.UserPreferencesRepository
import model.getSystemCurrencyCode
import toLocaleString
import ui.customizations.ExpenseColor

class UpcomingPaymentsViewModel(
    private val expenseRepository: ExpenseRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPaymentData>()
    val upcomingPaymentsData: List<UpcomingPaymentData>
        get() = _upcomingPaymentsData

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()

    init {
        viewModelScope.launch {
            expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
                onDatabaseUpdated(recurringExpenses)
            }
        }
    }

    fun onExpenseWithIdClicked(
        expenceId: Int,
        onItemClicked: (RecurringExpenseData) -> Unit,
    ) {
        viewModelScope.launch {
            expenseRepository.getRecurringExpenseById(expenceId)?.let {
                val recurringExpenseData = it.toFrontendType(getDefaultCurrencyCode())
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
                val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
                _upcomingPaymentsData.add(
                    UpcomingPaymentData(
                        id = expense.id,
                        name = expense.name!!,
                        price = CurrencyValue(expense.price!!, expense.currencyCode),
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
        return DateTimeCalculator.getDayOfNextOccurrenceFromNow(
            from = firstPayment,
            everyXRecurrence = everyXRecurrence,
            recurrence =
                when (recurrence) {
                    RecurrenceDatabase.Daily.value -> DateTimeUnit.DAY
                    RecurrenceDatabase.Weekly.value -> DateTimeUnit.WEEK
                    RecurrenceDatabase.Monthly.value -> DateTimeUnit.MONTH
                    RecurrenceDatabase.Yearly.value -> DateTimeUnit.YEAR
                    else -> DateTimeUnit.MONTH
                },
        )
    }

    private fun getNextPaymentDays(nextPaymentDay: LocalDate): Int {
        return DateTimeCalculator.getDaysFromNowUntil(nextPaymentDay)
    }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
