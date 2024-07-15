package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.Recurrence
import data.RecurringExpenseData
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import model.database.ExpenseRepository
import toFloatLocaleAware
import toLocalString
import ui.customizations.ExpenseColor

class EditRecurringExpenseViewModel(
    private val expenseId: Int?,
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {
    var nameState by mutableStateOf("")
    val nameInputError = mutableStateOf(false)
    var descriptionState by mutableStateOf("")
    var priceState by mutableStateOf("")
    val priceInputError = mutableStateOf(false)
    var everyXRecurrenceState by mutableStateOf("")
    val everyXRecurrenceInputError = mutableStateOf(false)
    var selectedRecurrence by mutableStateOf(Recurrence.Monthly)
    var firstPaymentDate: Instant? by mutableStateOf(null)
    var expenseColor by mutableStateOf(ExpenseColor.Dynamic)

    var showDeleteConfirmDialog by mutableStateOf(false)

    val isNewExpense = expenseId == null
    val showDeleteButton = !isNewExpense

    init {
        if (expenseId != null) {
            viewModelScope.launch {
                expenseRepository.getRecurringExpenseById(expenseId)?.toFrontendType()?.let { expense ->
                    nameState = expense.name
                    descriptionState = expense.description
                    priceState = expense.price.toLocalString()
                    everyXRecurrenceState = expense.everyXRecurrence.toString()
                    selectedRecurrence = expense.recurrence
                    firstPaymentDate = expense.firstPayment
                    expenseColor = expense.color
                }
            }
        }
    }

    fun updateExpense(response: (successful: Boolean) -> Unit) {
        viewModelScope.launch {
            val validInput = verifyUserInput()
            response(validInput)
            if (validInput) {
                if (expenseId == null) {
                    addExpense()
                } else {
                    val recurringExpense = createRecurringExpenseData()
                    expenseRepository.update(recurringExpense.toBackendType())
                }
            }
        }
    }

    fun onDeleteClick() {
        showDeleteConfirmDialog = true
    }

    fun onDismissDeleteDialog() {
        showDeleteConfirmDialog = false
    }

    fun deleteExpense() {
        if (expenseId == null) {
            throw IllegalStateException("Deleting an new expense not created yet is not allowed")
        }
        showDeleteConfirmDialog = false
        viewModelScope.launch {
            val recurringExpense = createRecurringExpenseData()
            expenseRepository.delete(recurringExpense.toBackendType())
        }
    }

    private suspend fun addExpense() {
        val recurringExpense = createRecurringExpenseData()
        expenseRepository.insert(recurringExpense.toBackendType())
    }

    private fun createRecurringExpenseData(): RecurringExpenseData {
        return RecurringExpenseData(
            id = expenseId ?: 0,
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

    private fun verifyUserInput(): Boolean {
        nameInputError.value = false
        priceInputError.value = false
        everyXRecurrenceInputError.value = false

        var everythingCorrect = true
        if (!isNameValid(nameState)) {
            nameInputError.value = true
            everythingCorrect = false
        }
        if (!isPriceValid(priceState)) {
            priceInputError.value = true
            everythingCorrect = false
        }
        if (!isEveryXRecurrenceValid(everyXRecurrenceState)) {
            everyXRecurrenceInputError.value = true
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
