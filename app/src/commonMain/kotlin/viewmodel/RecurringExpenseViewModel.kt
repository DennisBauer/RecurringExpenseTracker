package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.RecurringExpenseData
import kotlinx.coroutines.launch
import viewmodel.database.ExpenseRepository
import viewmodel.database.RecurringExpense

class RecurringExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {
    private val _recurringExpenseData = mutableStateListOf<RecurringExpenseData>()
    val recurringExpenseData: List<RecurringExpenseData>
        get() = _recurringExpenseData

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
        weeklyExpense = (price / (52 / 12f))
        monthlyExpense = price
        yearlyExpense = (price * 12)
    }
}
