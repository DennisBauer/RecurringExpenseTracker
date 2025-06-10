package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.CurrencyValue
import data.RecurringExpenseData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import model.IExchangeRateProvider
import model.database.IExpenseRepository
import model.database.RecurringExpense
import model.datastore.IUserPreferencesRepository
import model.getSystemCurrencyCode

class RecurringExpenseViewModel(
    private val expenseRepository: IExpenseRepository,
    private val exchangeRateProvider: IExchangeRateProvider,
    userPreferencesRepository: IUserPreferencesRepository,
) : ViewModel() {
    private val _recurringExpenseData = mutableStateListOf<RecurringExpenseData>()
    val recurringExpenseData: List<RecurringExpenseData>
        get() = _recurringExpenseData

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()
    private val showConvertedCurrency = userPreferencesRepository.showConvertedCurrency.get()

    var currencyPrefix by mutableStateOf("")
        private set
    var weeklyExpense by mutableFloatStateOf(0f)
        private set
    var monthlyExpense by mutableFloatStateOf(0f)
        private set
    var yearlyExpense by mutableFloatStateOf(0f)
        private set

    init {
        viewModelScope.launch {
            expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
                onDatabaseUpdated(recurringExpenses)
            }
        }
        viewModelScope.launch {
            defaultCurrency.collect {
                if (_recurringExpenseData.isNotEmpty()) {
                    onDatabaseUpdated(expenseRepository.allRecurringExpensesByPrice.first())
                }
            }
        }
        viewModelScope.launch {
            showConvertedCurrency.collect {
                if (_recurringExpenseData.isNotEmpty()) {
                    onDatabaseUpdated(expenseRepository.allRecurringExpensesByPrice.first())
                }
            }
        }
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpense>) {
        _recurringExpenseData.clear()
        val defaultCurrency = getDefaultCurrencyCode()
        var atLeastOneWasExchanged = false
        recurringExpenses.forEach {
            var expense = it.toFrontendType(getDefaultCurrencyCode())
            if (expense.price.currencyCode != defaultCurrency) {
                val newPrice = expense.price.currencyValueBasedOnSetting()
                val newMonthlyPrice = expense.monthlyPrice.currencyValueBasedOnSetting()
                expense =
                    expense.copy(
                        price = newPrice,
                        monthlyPrice = newMonthlyPrice,
                    )
                atLeastOneWasExchanged = true
            }
            _recurringExpenseData.add(expense)
        }
        _recurringExpenseData.sortByDescending { it.monthlyPrice.value }
        updateExpenseSummary()
        currencyPrefix = if (atLeastOneWasExchanged) "~" else ""
    }

    private suspend fun updateExpenseSummary() {
        var price = 0f
        _recurringExpenseData.forEach {
            it.monthlyPrice.exchangeToDefaultCurrency()?.let { exchanged ->
                price += exchanged.value
            }
        }
        weeklyExpense = (price / (52 / 12f))
        monthlyExpense = price
        yearlyExpense = (price * 12)
    }

    private suspend fun CurrencyValue.currencyValueBasedOnSetting(): CurrencyValue {
        return if (showConvertedCurrency.first()) {
            exchangeRateProvider.exchangeCurrencyValue(this, getDefaultCurrencyCode()) ?: this
        } else {
            this
        }
    }

    private suspend fun CurrencyValue.exchangeToDefaultCurrency(): CurrencyValue? {
        return exchangeRateProvider.exchangeCurrencyValue(this, getDefaultCurrencyCode())
    }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
