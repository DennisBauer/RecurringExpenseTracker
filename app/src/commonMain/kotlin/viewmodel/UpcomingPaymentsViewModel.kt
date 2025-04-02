package viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.CurrencyValue
import data.RecurringExpenseData
import data.UpcomingPaymentData
import getDefaultCurrencyCode
import getNextPaymentDays
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import model.database.ExpenseRepository
import model.database.RecurringExpense
import model.database.UserPreferencesRepository
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
                val recurringExpenseData = it.toFrontendType(defaultCurrency.getDefaultCurrencyCode())
                onItemClicked(recurringExpenseData)
            }
        }
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpense>) {
        _upcomingPaymentsData.clear()
        recurringExpenses.forEach { expense ->
            expense.getNextPaymentDay()?.let { nextPaymentDay ->
                val nextPaymentRemainingDays = nextPaymentDay.getNextPaymentDays()
                val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
                _upcomingPaymentsData.add(
                    UpcomingPaymentData(
                        id = expense.id,
                        name = expense.name!!,
                        price =
                            CurrencyValue(
                                expense.price!!,
                                expense.currencyCode.ifBlank { defaultCurrency.getDefaultCurrencyCode() },
                            ),
                        nextPaymentRemainingDays = nextPaymentRemainingDays,
                        nextPaymentDate = nextPaymentDate,
                        color = ExpenseColor.fromInt(expense.color),
                    ),
                )
            }
        }
        _upcomingPaymentsData.sortBy { it.nextPaymentRemainingDays }
    }
}
