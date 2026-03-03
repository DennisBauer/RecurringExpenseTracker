package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.model.IExchangeRateProvider
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.getSystemCurrencyCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecurringExpenseViewModel(
    private val expenseRepository: IExpenseRepository,
    private val exchangeRateProvider: IExchangeRateProvider,
    private val userPreferencesRepository: IUserPreferencesRepository,
) : ViewModel() {
    private val _recurringExpenseData = mutableStateListOf<RecurringExpenseData>()
    val recurringExpenseData: List<RecurringExpenseData>
        get() = _recurringExpenseData

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()
    private val showConvertedCurrency = userPreferencesRepository.showConvertedCurrency.get()

    private var _showPersonalExpenses by mutableStateOf(false)
    val showPersonalExpenses: Boolean get() = _showPersonalExpenses

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
        viewModelScope.launch {
            userPreferencesRepository.showPersonalExpenses.get().collect { value ->
                _showPersonalExpenses = value
                updateExpenseSummary()
            }
        }
    }

    fun setShowPersonalExpenses(value: Boolean) {
        _showPersonalExpenses = value
        viewModelScope.launch {
            userPreferencesRepository.showPersonalExpenses.save(value)
        }
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpenseData>) {
        _recurringExpenseData.clear()
        val defaultCurrencyCode = getDefaultCurrencyCode()
        var atLeastOneWasExchanged = false
        recurringExpenses.forEach {
            var expense = it
            if (expense.price.currencyCode != defaultCurrencyCode) {
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
        var personalPrice = 0f
        _recurringExpenseData.forEach {
            val value = it.monthlyPrice.exchangeToDefaultCurrency()?.value ?: it.monthlyPrice.value
            price += value
            val divided =
                if (it.isSplit && it.splitBetweenPeople > 1) {
                    value / it.splitBetweenPeople
                } else {
                    value
                }
            personalPrice += divided
        }

        val summaryPrice = if (showPersonalExpenses) personalPrice else price
        weeklyExpense = summaryPrice / (52 / 12f)
        monthlyExpense = summaryPrice
        yearlyExpense = summaryPrice * 12
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
