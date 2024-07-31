package viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.CurrencyOption
import data.CurrencyValue
import data.Recurrence
import data.RecurringExpenseData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import model.CurrencyProvider
import model.database.ExpenseRepository
import model.database.UserPreferencesRepository
import model.getSystemCurrencyCode
import toFloatLocaleAware
import toLocalString
import ui.customizations.ExpenseColor

class EditRecurringExpenseViewModel(
    private val expenseId: Int?,
    private val expenseRepository: ExpenseRepository,
    private val currencyProvider: CurrencyProvider,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    var nameState by mutableStateOf("")
    val nameInputError = mutableStateOf(false)
    var descriptionState by mutableStateOf("")
    var priceState by mutableStateOf("")
    var selectedCurrencyOption by mutableStateOf(CurrencyOption("", ""))
    var availableCurrencyOptions = mutableStateListOf<CurrencyOption>()
    val priceInputError = mutableStateOf(false)
    var everyXRecurrenceState by mutableStateOf("")
    val everyXRecurrenceInputError = mutableStateOf(false)
    var selectedRecurrence by mutableStateOf(Recurrence.Monthly)
    var firstPaymentDate: Instant? by mutableStateOf(null)
    var expenseColor by mutableStateOf(ExpenseColor.Dynamic)

    var showDeleteConfirmDialog by mutableStateOf(false)

    val isNewExpense = expenseId == null
    val showDeleteButton = !isNewExpense

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()

    init {
        viewModelScope.launch {
            val availableCurrencies =
                currencyProvider.retrieveCurrencies().map {
                    CurrencyOption(it.code, "${it.name} (${it.symbol})")
                }
            availableCurrencyOptions.addAll(availableCurrencies)

            if (expenseId != null) {
                expenseRepository
                    .getRecurringExpenseById(expenseId)
                    ?.toFrontendType(getDefaultCurrencyCode())
                    ?.let { expense ->
                        nameState = expense.name
                        descriptionState = expense.description
                        priceState = expense.price.value.toLocalString()
                        everyXRecurrenceState = expense.everyXRecurrence.toString()
                        selectedRecurrence = expense.recurrence
                        firstPaymentDate = expense.firstPayment
                        expenseColor = expense.color

                        availableCurrencies.firstOrNull { it.currencyCode == expense.price.currencyCode }?.let {
                            selectedCurrencyOption = it
                        }
                    }
            } else {
                availableCurrencies.firstOrNull { it.currencyCode == getDefaultCurrencyCode() }?.let {
                    selectedCurrencyOption = it
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
                    expenseRepository.update(recurringExpense.toBackendType(getDefaultCurrencyCode()))
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
            expenseRepository.delete(recurringExpense.toBackendType(getDefaultCurrencyCode()))
        }
    }

    private suspend fun addExpense() {
        val recurringExpense = createRecurringExpenseData()
        expenseRepository.insert(recurringExpense.toBackendType(getDefaultCurrencyCode()))
    }

    private fun createRecurringExpenseData(): RecurringExpenseData {
        return RecurringExpenseData(
            id = expenseId ?: 0,
            name = nameState,
            description = descriptionState,
            price =
                CurrencyValue(
                    priceState.toFloatLocaleAware() ?: 0f,
                    selectedCurrencyOption.currencyCode,
                ),
            monthlyPrice =
                CurrencyValue(
                    priceState.toFloatLocaleAware() ?: 0f,
                    selectedCurrencyOption.currencyCode,
                ),
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

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
