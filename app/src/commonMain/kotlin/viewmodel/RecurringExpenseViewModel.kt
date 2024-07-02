package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import data.RecurringExpenseData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import toCurrencyString
import viewmodel.database.ExpenseRepository
import viewmodel.database.RecurringExpense

class RecurringExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {
    private val _recurringExpenseData = mutableStateListOf<RecurringExpenseData>()
    val recurringExpenseData: List<RecurringExpenseData>
        get() = _recurringExpenseData

    private var _weeklyExpense by mutableStateOf("")
    private var _monthlyExpense by mutableStateOf("")
    private var _yearlyExpense by mutableStateOf("")
    val weeklyExpense: String
        get() = _weeklyExpense
    val monthlyExpense: String
        get() = _monthlyExpense
    val yearlyExpense: String
        get() = _yearlyExpense

    init {
        viewModelScope.launch {
            expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
                onDatabaseUpdated(recurringExpenses)
            }
        }
    }

    fun onDatabaseRestored() {
        viewModelScope.launch {
            val recurringExpenses = expenseRepository.allRecurringExpensesByPrice.first()
            onDatabaseUpdated(recurringExpenses)
        }
    }

    private fun onDatabaseUpdated(recurringExpenses: List<RecurringExpense>) {
        _recurringExpenseData.clear()
        recurringExpenses.forEach {
            _recurringExpenseData.add(it.toFrontendType())
        }
        _recurringExpenseData.sortByDescending { it.monthlyPrice }
        updateExpenseSummary()
    }

    private fun updateExpenseSummary() {
        var price = 0f
        _recurringExpenseData.forEach {
            price += it.monthlyPrice
        }
        _weeklyExpense = (price / (52 / 12f)).toCurrencyString()
        _monthlyExpense = price.toCurrencyString()
        _yearlyExpense = (price * 12).toCurrencyString()
    }

    companion object {
        fun create(expenseRepository: ExpenseRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    RecurringExpenseViewModel(expenseRepository)
                }
            }
        }
    }
}
