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
    userPreferencesRepository: IUserPreferencesRepository,
) : ViewModel() {
    private val recurringExpenseData = mutableStateListOf<RecurringExpenseData>()

    private val _filteredRecurringExpenseData = mutableStateListOf<RecurringExpenseData>()
    val filteredRecurringExpenseData: List<RecurringExpenseData>
        get() = _filteredRecurringExpenseData

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()
    private val showConvertedCurrency = userPreferencesRepository.showConvertedCurrency.get()

    var currencyPrefix by mutableStateOf("")
        private set
    private var weeklyExpense by mutableFloatStateOf(0f)
    private var monthlyExpense by mutableFloatStateOf(0f)
    private var yearlyExpense by mutableFloatStateOf(0f)

    var searchQuery by mutableStateOf("")
        private set

    var filteredWeeklyExpense by mutableFloatStateOf(0f)
        private set
    var filteredMonthlyExpense by mutableFloatStateOf(0f)
        private set
    var filteredYearlyExpense by mutableFloatStateOf(0f)
        private set

    init {
        viewModelScope.launch {
            expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
                onDatabaseUpdated(recurringExpenses)
            }
        }
        viewModelScope.launch {
            defaultCurrency.collect {
                if (recurringExpenseData.isNotEmpty()) {
                    onDatabaseUpdated(expenseRepository.allRecurringExpensesByPrice.first())
                }
            }
        }
        viewModelScope.launch {
            showConvertedCurrency.collect {
                if (recurringExpenseData.isNotEmpty()) {
                    onDatabaseUpdated(expenseRepository.allRecurringExpensesByPrice.first())
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        viewModelScope.launch {
            updateFilteredData()
        }
    }

    private suspend fun onDatabaseUpdated(recurringExpenses: List<RecurringExpenseData>) {
        recurringExpenseData.clear()
        val defaultCurrency = getDefaultCurrencyCode()
        var atLeastOneWasExchanged = false
        recurringExpenses.forEach {
            var expense = it
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
            recurringExpenseData.add(expense)
        }
        recurringExpenseData.sortByDescending { it.monthlyPrice.value }
        updateExpenseSummary()
        updateFilteredData()
        currencyPrefix = if (atLeastOneWasExchanged) "~" else ""
    }

    private suspend fun updateExpenseSummary() {
        var price = 0f
        recurringExpenseData.forEach {
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

    private suspend fun updateFilteredData() {
        _filteredRecurringExpenseData.clear()
        val query = searchQuery.trim().lowercase()
        val filtered =
            if (query.isEmpty()) {
                recurringExpenseData.toList()
            } else {
                recurringExpenseData.filter { expense ->
                    expense.name.lowercase().contains(query) ||
                        expense.description.lowercase().contains(query) ||
                        expense.tags.any { tag -> tag.title.lowercase().contains(query) }
                }
            }
        _filteredRecurringExpenseData.addAll(filtered)

        var price = 0f
        _filteredRecurringExpenseData.forEach {
            it.monthlyPrice.exchangeToDefaultCurrency()?.let { exchanged ->
                price += exchanged.value
            }
        }
        filteredWeeklyExpense = (price / (52 / 12f))
        filteredMonthlyExpense = price
        filteredYearlyExpense = (price * 12)
    }
}
