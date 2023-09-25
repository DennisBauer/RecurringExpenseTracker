package de.dbauer.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.toCurrencyString
import de.dbauer.expensetracker.viewmodel.database.ExpenseRepository
import de.dbauer.expensetracker.viewmodel.database.RecurringExpense
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

private enum class RecurrenceDatabase(
    val value: Int,
) {
    Daily(1),
    Weekly(2),
    Monthly(3),
    Yearly(4),
}

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
            expenseRepository.allRecurringExpensesByPrice.collect { recurringExpenses ->
                _recurringExpenseData.clear()
                recurringExpenses.forEach {
                    _recurringExpenseData.add(
                        RecurringExpenseData(
                            id = it.id,
                            name = it.name!!,
                            description = it.description!!,
                            price = it.price!!,
                            monthlyPrice = it.monthlyPrice(),
                            everyXRecurrence = it.everyXRecurrence!!,
                            recurrence = getRecurrenceFromDatabaseInt(it.recurrence!!),
                        ),
                    )
                }
                updateExpenseSummary()
            }
        }
    }

    fun addRecurringExpense(recurringExpense: RecurringExpenseData) {
        viewModelScope.launch {
            expenseRepository.insert(
                RecurringExpense(
                    id = 0,
                    name = recurringExpense.name,
                    description = recurringExpense.description,
                    price = recurringExpense.price,
                    everyXRecurrence = recurringExpense.everyXRecurrence,
                    recurrence = getRecurrenceIntFromUIRecurrence(recurringExpense.recurrence),
                ),
            )
        }
    }

    fun editRecurringExpense(recurringExpense: RecurringExpenseData) {
        viewModelScope.launch {
            expenseRepository.update(
                RecurringExpense(
                    id = recurringExpense.id,
                    name = recurringExpense.name,
                    description = recurringExpense.description,
                    price = recurringExpense.price,
                    everyXRecurrence = recurringExpense.everyXRecurrence,
                    recurrence = getRecurrenceIntFromUIRecurrence(recurringExpense.recurrence),
                ),
            )
        }
    }

    fun deleteRecurringExpense(recurringExpense: RecurringExpenseData) {
        viewModelScope.launch {
            expenseRepository.delete(
                RecurringExpense(
                    id = recurringExpense.id,
                    name = recurringExpense.name,
                    description = recurringExpense.description,
                    price = recurringExpense.price,
                    everyXRecurrence = recurringExpense.everyXRecurrence,
                    recurrence = getRecurrenceIntFromUIRecurrence(recurringExpense.recurrence),
                ),
            )
        }
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

    private fun getRecurrenceIntFromUIRecurrence(recurrence: Recurrence): Int {
        return when (recurrence) {
            Recurrence.Daily -> RecurrenceDatabase.Daily.value
            Recurrence.Weekly -> RecurrenceDatabase.Weekly.value
            Recurrence.Monthly -> RecurrenceDatabase.Monthly.value
            Recurrence.Yearly -> RecurrenceDatabase.Yearly.value
        }
    }

    private fun updateExpenseSummary() {
        var price = 0f
        _recurringExpenseData.forEach {
            price += it.monthlyPrice
        }
        _weeklyExpense = (price / 30f).toCurrencyString()
        _monthlyExpense = price.toCurrencyString()
        _yearlyExpense = (price * 12).toCurrencyString()
    }

    private fun RecurringExpense.monthlyPrice(): Float {
        return when (recurrence) {
            RecurrenceDatabase.Daily.value -> {
                (365 / 12f) / everyXRecurrence!! * price!!
            }
            RecurrenceDatabase.Weekly.value -> {
                (52 / 12f) / everyXRecurrence!! * price!!
            }
            RecurrenceDatabase.Monthly.value -> {
                1f / everyXRecurrence!! * price!!
            }
            RecurrenceDatabase.Yearly.value -> {
                price!! / (everyXRecurrence!! * 12f)
            }
            else -> 0f
        }
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
