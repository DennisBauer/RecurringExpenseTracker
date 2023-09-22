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
            description = "My Disney Plus description",
            priceValue = 7.95f,
        ),
    )
    val recurringExpenseData: ImmutableList<RecurringExpenseData>
        get() = _recurringExpenseData.toImmutableList()

    private var _montlyPrice by mutableStateOf("")
    val monthlyPrice: String
        get() = _montlyPrice

    init {
        var price = 0f
        _recurringExpenseData.forEach {
            price += it.priceValue
        }
        _montlyPrice = "${price.toValueString()} €" // TODO: Make currency dynamic
    }
}