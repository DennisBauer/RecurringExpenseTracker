package de.dbauer.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.data.CurrencyOption
import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.model.CurrencyProvider
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.model.getSystemCurrencyCode
import de.dbauer.expensetracker.toFloatLocaleAware
import de.dbauer.expensetracker.toLocalString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_notification_days_advance
import kotlin.time.Instant

class EditRecurringExpenseViewModel(
    private val expenseId: Int?,
    private val expenseRepository: IExpenseRepository,
    private val currencyProvider: CurrencyProvider,
    userPreferencesRepository: IUserPreferencesRepository,
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
    var currencyError by mutableStateOf(false)
        private set
    var selectedRecurrence by mutableStateOf(Recurrence.Monthly)
    var firstPaymentDate: Instant? by mutableStateOf(null)
    private var _tags = mutableStateMapOf<Tag, Boolean>()
    val tags: List<Pair<Tag, Boolean>>
        get() =
            _tags.toList().sortedWith(
                compareByDescending<Pair<Tag, Boolean>> { it.second }.thenBy { it.first.title },
            )

    var expenseNotificationEnabledGlobally = userPreferencesRepository.upcomingPaymentNotification
    var notifyForExpense by mutableStateOf(true)
    var notifyXDaysBefore by mutableStateOf("")
    var defaultXDaysPlaceholder by mutableStateOf("")

    var showDeleteConfirmDialog by mutableStateOf(false)

    val isNewExpense = expenseId == null
    val showDeleteButton = !isNewExpense

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()
    private var lastNotificationDate: Instant? = null

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
                    ?.let { expense ->
                        nameState = expense.name
                        descriptionState = expense.description
                        priceState = expense.price.value.toLocalString()
                        everyXRecurrenceState = expense.everyXRecurrence.toString()
                        selectedRecurrence = expense.recurrence
                        firstPaymentDate = expense.firstPayment
                        expense.tags.forEach {
                            _tags[it] = true
                        }
                        notifyForExpense = expense.notifyForExpense
                        notifyXDaysBefore = expense.notifyXDaysBefore?.toString() ?: ""
                        lastNotificationDate = expense.lastNotificationDate

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
        viewModelScope.launch {
            userPreferencesRepository.upcomingPaymentNotificationDaysAdvance.get().collect {
                defaultXDaysPlaceholder = getString(Res.string.edit_expense_notification_days_advance, it)
            }
        }
        viewModelScope.launch {
            expenseRepository.allTags.collect { newTags ->
                newTags.forEach { tag ->
                    _tags.getOrPut(tag) { _tags.entries.firstOrNull { it.key.id == tag.id }?.value ?: false }
                }
                val tagsToRemove = _tags.keys.filter { it !in newTags }
                tagsToRemove.forEach {
                    _tags.remove(it)
                }
            }
        }
    }

    fun onTagClick(tag: Tag) {
        val existingState = _tags[tag] ?: false
        _tags[tag] = !existingState
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
                    expenseRepository.update(recurringExpense)
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
            expenseRepository.delete(recurringExpense)
        }
    }

    private suspend fun addExpense() {
        val recurringExpense = createRecurringExpenseData()
        expenseRepository.insert(recurringExpense)
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
            tags = tags.filter { it.second }.map { it.first },
            firstPayment = firstPaymentDate,
            notifyForExpense = notifyForExpense,
            notifyXDaysBefore = notifyXDaysBefore.takeIf { it.isNotBlank() && notifyForExpense }?.toIntOrNull(),
            lastNotificationDate = lastNotificationDate,
        )
    }

    private fun verifyUserInput(): Boolean {
        nameInputError.value = false
        priceInputError.value = false
        everyXRecurrenceInputError.value = false
        currencyError = false

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
        if (!currencyIsValid(selectedCurrencyOption.currencyName)) {
            currencyError = true
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

    private fun currencyIsValid(currency: String): Boolean {
        val currencyOption = CurrencyOption(selectedCurrencyOption.currencyCode, currency)
        return currency.isNotBlank() && availableCurrencyOptions.any { it == currencyOption }
    }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
