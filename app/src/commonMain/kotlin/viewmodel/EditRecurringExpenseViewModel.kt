package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import data.Recurrence
import data.RecurringExpenseData
import toFloatLocaleAware
import toLocalString
import ui.customizations.ExpenseColor

class EditRecurringExpenseViewModel(private val currentData: RecurringExpenseData?) : ViewModel() {
    var nameState by mutableStateOf(currentData?.name ?: "")
    val nameInputError = mutableStateOf(false)
    var descriptionState by mutableStateOf(currentData?.description ?: "")
    var priceState by mutableStateOf(currentData?.price?.toLocalString() ?: "")
    val priceInputError = mutableStateOf(false)
    var everyXRecurrenceState by mutableStateOf(currentData?.everyXRecurrence?.toString() ?: "")
    val everyXRecurrenceInputError = mutableStateOf(false)
    var selectedRecurrence by mutableStateOf(currentData?.recurrence ?: Recurrence.Monthly)
    var firstPaymentDate by mutableStateOf(currentData?.firstPayment)
    var expenseColor by mutableStateOf(currentData?.color ?: ExpenseColor.Dynamic)

    companion object {
        fun create(currentData: RecurringExpenseData?): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    EditRecurringExpenseViewModel(currentData)
                }
            }
        }
    }

    fun tryCreateUpdatedRecurringExpenseData(): RecurringExpenseData? {
        nameInputError.value = false
        priceInputError.value = false
        everyXRecurrenceInputError.value = false

        if (verifyUserInput(
                name = nameState,
                onNameInputError = { nameInputError.value = true },
                price = priceState,
                onPriceInputError = { priceInputError.value = true },
                everyXRecurrence = everyXRecurrenceState,
                onEveryXRecurrenceError = { everyXRecurrenceInputError.value = true },
            )
        ) {
            return RecurringExpenseData(
                id = currentData?.id ?: 0,
                name = nameState,
                description = descriptionState,
                price = priceState.toFloatLocaleAware() ?: 0f,
                monthlyPrice = priceState.toFloatLocaleAware() ?: 0f,
                everyXRecurrence = everyXRecurrenceState.toIntOrNull() ?: 1,
                recurrence = selectedRecurrence,
                firstPayment = firstPaymentDate,
                color = expenseColor,
            )
        }
        return null
    }

    private fun verifyUserInput(
        name: String,
        onNameInputError: () -> Unit,
        price: String,
        onPriceInputError: () -> Unit,
        everyXRecurrence: String,
        onEveryXRecurrenceError: () -> Unit,
    ): Boolean {
        var everythingCorrect = true
        if (!isNameValid(name)) {
            onNameInputError()
            everythingCorrect = false
        }
        if (!isPriceValid(price)) {
            onPriceInputError()
            everythingCorrect = false
        }
        if (!isEveryXRecurrenceValid(everyXRecurrence)) {
            onEveryXRecurrenceError()
            everythingCorrect = false
        }
        return everythingCorrect
    }

    private fun isNameValid(name: String): Boolean {
        return name.isNotBlank()
    }

    private fun isPriceValid(price: String): Boolean {
        return price.toFloatLocaleAware() != null
    }

    private fun isEveryXRecurrenceValid(everyXRecurrence: String): Boolean {
        return everyXRecurrence.isBlank() || everyXRecurrence.toIntOrNull() != null
    }
}
