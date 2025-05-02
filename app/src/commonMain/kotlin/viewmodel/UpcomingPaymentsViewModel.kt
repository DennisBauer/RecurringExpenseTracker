package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.CurrencyValue
import data.RecurringExpenseData
import data.UpcomingPaymentData
import getDefaultCurrencyCode
import getNextPaymentDays
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import model.ExchangeRateProvider
import model.database.ExpenseRepository
import model.database.RecurringExpense
import model.database.UserPreferencesRepository
import model.getSystemCurrencyCode
import toCurrencyString
import toLocaleString
import ui.customizations.ExpenseColor

class UpcomingPaymentsViewModel(
    private val expenseRepository: ExpenseRepository,
    private val exchangeRateProvider: ExchangeRateProvider,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _upcomingPaymentsData = mutableStateListOf<UpcomingPaymentData>()
    val upcomingPaymentsData: List<UpcomingPaymentData>
        get() = _upcomingPaymentsData

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()

    var remainingExpenseThisMonth by mutableStateOf("")
        private set

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
        var atLeastOneWasExchanged = false
        var remainingExpenseThisMonthValue = 0f
        recurringExpenses.forEach { expense ->
            expense.getNextPaymentDay()?.let { nextPaymentDay ->
                val nextPaymentRemainingDays = nextPaymentDay.getNextPaymentDays()
                val nextPaymentDate = nextPaymentDay.atStartOfDayIn(TimeZone.UTC).toLocaleString()
                val currencyValue =
                    CurrencyValue(
                        expense.price!!,
                        expense.currencyCode.ifBlank { defaultCurrency.getDefaultCurrencyCode() },
                    )
                _upcomingPaymentsData.add(
                    UpcomingPaymentData(
                        id = expense.id,
                        name = expense.name!!,
                        price = currencyValue,
                        nextPaymentRemainingDays = nextPaymentRemainingDays,
                        nextPaymentDate = nextPaymentDate,
                        color = ExpenseColor.fromInt(expense.color),
                    ),
                )

                // Calculate remaining expense this month
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                val isPaymentInCurrentMonth =
                    nextPaymentDay.monthNumber == now.monthNumber && nextPaymentDay.year == now.year
                if (isPaymentInCurrentMonth) {
                    remainingExpenseThisMonthValue += currencyValue.exchangeToDefaultCurrency()?.value ?: 0f
                    val currencyCode = expense.currencyCode.ifBlank { defaultCurrency.getDefaultCurrencyCode() }
                    if (currencyCode != defaultCurrency.getDefaultCurrencyCode()) {
                        atLeastOneWasExchanged = true
                    }
                }
            }
        }
        _upcomingPaymentsData.sortBy { it.nextPaymentRemainingDays }

        // Calculate remaining expense this month
        val currencyPrefix = if (atLeastOneWasExchanged) "~" else ""
        remainingExpenseThisMonth =
            currencyPrefix + remainingExpenseThisMonthValue.toCurrencyString(defaultCurrency.first())
    }

    private suspend fun CurrencyValue.exchangeToDefaultCurrency(): CurrencyValue? {
        return exchangeRateProvider.exchangeCurrencyValue(this, getDefaultCurrencyCode())
    }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
