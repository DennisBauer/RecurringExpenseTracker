package de.erzock.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.erzock.expensetracker.data.RecurringExpenseData
import de.erzock.expensetracker.toValueString
import de.erzock.expensetracker.viewmodel.database.ExpenseRepository
import de.erzock.expensetracker.viewmodel.database.RecurringExpense
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

class MainActivityViewModel(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {
    private val _recurringExpenseData = mutableStateListOf<RecurringExpenseData>()
    val recurringExpenseData: ImmutableList<RecurringExpenseData>
        get() = _recurringExpenseData.toImmutableList()

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
            expenseRepository.allRecurringExpensesByPrice.collect() { recurringExpenses ->
                recurringExpenses.forEach {
                    _recurringExpenseData.add(RecurringExpenseData(
                        name = it.name!!,
                        description = it.description!!,
                        priceValue = it.price!!
                    ))
                }
                updateExpenseSummary()
            }
        }
    }

    fun addRecurringExpense(recurringExpense: RecurringExpenseData) {
        viewModelScope.launch {
            expenseRepository.insert(RecurringExpense(
                id = 0,
                name = recurringExpense.name,
                description = recurringExpense.description,
                price = recurringExpense.priceValue,
            ))
        }
    }

    private fun updateExpenseSummary() {
        var price = 0f
        _recurringExpenseData.forEach {
            price += it.priceValue
        }
        _weeklyExpense = "${(price / 30f).toValueString()} €" // TODO: Make currency dynamic
        _monthlyExpense = "${price.toValueString()} €" // TODO: Make currency dynamic
        _yearlyExpense = "${(price * 12).toValueString()} €" // TODO: Make currency dynamic
    }

    companion object {
        fun create(expenseRepository: ExpenseRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MainActivityViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MainActivityViewModel(expenseRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}