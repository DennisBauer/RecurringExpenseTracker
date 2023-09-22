package de.erzock.expensetracker.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import de.erzock.expensetracker.toValueString
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

class MainActivityViewModel : ViewModel() {
    private val _recurringExpenseData = mutableStateListOf<RecurringExpenseData>(
        RecurringExpenseData(
            name = "Netflix",
            description = "My Netflix description",
            priceValue = 9.99f,
        ),
        RecurringExpenseData(
            name = "Disney Plus",
            description = "My Disney Plus description",
            priceValue = 5f,
        ),
        RecurringExpenseData(
            name = "Amazon Prime",
            description = "My Amazon Prime description",
            priceValue = 7.95f,
        ),
        RecurringExpenseData(
            name = "Netflix",
            description = "My Netflix description",
            priceValue = 9.99f,
        ),
        RecurringExpenseData(
            name = "Disney Plus",
            description = "My Disney Plus description",
            priceValue = 5f,
        ),
        RecurringExpenseData(
            name = "Amazon Prime",
            description = "My Amazon Prime description",
            priceValue = 7.95f,
        ),
        RecurringExpenseData(
            name = "Netflix",
            description = "My Netflix description",
            priceValue = 9.99f,
        ),
        RecurringExpenseData(
            name = "Disney Plus",
            description = "My Disney Plus description",
            priceValue = 5f,
        ),
        RecurringExpenseData(
            name = "Amazon Prime",
            description = "My Amazon Prime description",
            priceValue = 7.95f,
        ),
    )
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
        updateExpenseSummary()
    }

    fun addRecurringExpense(recurringExpense: RecurringExpenseData) {
        _recurringExpenseData.add(recurringExpense)
        updateExpenseSummary()
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
}