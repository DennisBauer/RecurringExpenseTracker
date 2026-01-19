package de.dbauer.expensetracker.shared.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.dbauer.expensetracker.shared.data.CurrencyOption
import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.Recurrence
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Reminder
import de.dbauer.expensetracker.shared.data.Tag
import de.dbauer.expensetracker.shared.model.CurrencyProvider
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.getSystemCurrencyCode
import de.dbauer.expensetracker.shared.toFloatLocaleAware
import de.dbauer.expensetracker.shared.toLocalString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.time.Instant

class EditRecurringExpenseViewModel(
    private val expenseId: Int?,
    private val expenseRepository: IExpenseRepository,
    private val currencyProvider: CurrencyProvider,
    private val topAppBarMediator: TopAppBarMediator,
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

    var reminders = mutableStateListOf<Reminder>()
        private set

    // Store the last reminder configuration before disabling
    private var lastRemindersBeforeDisabling = mutableListOf<Reminder>()

    val showDeleteConfirmDialog get() = topAppBarMediator.showDeleteExpenseConfirmDialog
    var showDismissUnsavedChangesDialog by mutableStateOf(false)

    val isNewExpense = expenseId == null

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()
    private var defaultReminderDays by mutableIntStateOf(0)

    init {
        topAppBarMediator.showDeleteExpenseButton.value = !isNewExpense
        viewModelScope.launch {
            val availableCurrencies =
                currencyProvider.retrieveCurrencies().map {
                    CurrencyOption(it.code, "${it.name} (${it.symbol})")
                }
            availableCurrencyOptions.addAll(availableCurrencies)

            defaultReminderDays = userPreferencesRepository.upcomingPaymentNotificationDaysAdvance.get().first()

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
                        reminders.clear()

                        // If no custom reminders exist but notifications are enabled, show global default
                        if (expense.reminders.isEmpty() && expense.notifyForExpense) {
                            reminders.add(
                                Reminder(
                                    id = 0,
                                    daysBeforePayment = defaultReminderDays,
                                ),
                            )
                        } else {
                            // Sort reminders initially when loading from database
                            reminders.addAll(expense.reminders.sortedBy { it.daysBeforePayment })
                        }

                        availableCurrencies.firstOrNull { it.currencyCode == expense.price.currencyCode }?.let {
                            selectedCurrencyOption = it
                        }
                    }
            } else {
                availableCurrencies.firstOrNull { it.currencyCode == getDefaultCurrencyCode() }?.let {
                    selectedCurrencyOption = it
                }
                if (notifyForExpense) {
                    reminders.add(Reminder(id = 0, daysBeforePayment = defaultReminderDays))
                }
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

    fun onNotifyForExpenseChange(enabled: Boolean) {
        notifyForExpense = enabled

        if (enabled) {
            // If we have stored reminders from before disabling, restore them
            if (lastRemindersBeforeDisabling.isNotEmpty()) {
                reminders.clear()
                reminders.addAll(lastRemindersBeforeDisabling)
                lastRemindersBeforeDisabling.clear()
            } else if (reminders.isEmpty()) {
                // Otherwise add a default reminder if none exist
                reminders.add(Reminder(id = 0, daysBeforePayment = defaultReminderDays))
            }
        } else {
            if (reminders.isNotEmpty()) {
                lastRemindersBeforeDisabling.clear()
                lastRemindersBeforeDisabling.addAll(reminders)
            }
            reminders.clear()
        }
    }

    fun addReminder(daysBeforePayment: Int) {
        // Prevent adding duplicate reminders with the same days before payment
        if (reminders.none { it.daysBeforePayment == daysBeforePayment }) {
            reminders.add(Reminder(id = 0, daysBeforePayment = daysBeforePayment))
        }
    }

    fun updateReminder(
        index: Int,
        daysBeforePayment: Int,
    ) {
        val sortedReminders = reminders
        if (index in sortedReminders.indices) {
            val reminderToUpdate = sortedReminders[index]

            // Check if another reminder already has this daysBeforePayment value
            val duplicateExists =
                reminders.any {
                    it.daysBeforePayment == daysBeforePayment &&
                        (
                            it.id != reminderToUpdate.id ||
                                it.daysBeforePayment != reminderToUpdate.daysBeforePayment
                        )
                }

            if (duplicateExists) {
                return // Don't update if it would create a duplicate
            }

            val actualIndex =
                reminders.indexOfFirst {
                    it.id == reminderToUpdate.id && it.daysBeforePayment == reminderToUpdate.daysBeforePayment
                }
            if (actualIndex != -1) {
                reminders[actualIndex] = reminders[actualIndex].copy(daysBeforePayment = daysBeforePayment)
            }
        }
    }

    fun removeReminder(index: Int) {
        val sortedReminders = reminders
        if (index in sortedReminders.indices) {
            val reminderToRemove = sortedReminders[index]
            val actualIndex =
                reminders.indexOfFirst {
                    it.id == reminderToRemove.id && it.daysBeforePayment == reminderToRemove.daysBeforePayment
                }
            if (actualIndex != -1) {
                reminders.removeAt(actualIndex)

                if (reminders.isEmpty()) {
                    notifyForExpense = false
                }
            }
        }
    }

    fun isReminderDuplicate(
        index: Int,
        daysBeforePayment: Int,
    ): Boolean {
        val sortedReminders = reminders
        if (index !in sortedReminders.indices) return false

        val reminderToCheck = sortedReminders[index]

        return reminders.any {
            it.daysBeforePayment == daysBeforePayment &&
                (it.id != reminderToCheck.id || it.daysBeforePayment != reminderToCheck.daysBeforePayment)
        }
    }

    fun isNewReminderDuplicate(daysBeforePayment: Int): Boolean {
        return reminders.any { it.daysBeforePayment == daysBeforePayment }
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

    fun onDismissDeleteDialog() {
        topAppBarMediator.showDeleteExpenseConfirmDialog = false
    }

    fun deleteExpense() {
        if (expenseId == null) {
            throw IllegalStateException("Deleting an new expense not created yet is not allowed")
        }
        topAppBarMediator.showDeleteExpenseConfirmDialog = false
        viewModelScope.launch {
            val recurringExpense = createRecurringExpenseData()
            expenseRepository.delete(recurringExpense)
        }
    }

    fun onBackPressed(onConfirmedDismiss: () -> Unit) {
        viewModelScope.launch {
            if (hasUnsavedChanges()) {
                showDismissUnsavedChangesDialog = true
            } else {
                onConfirmedDismiss()
            }
        }
    }

    fun onDismissUnsavedChangesDialog() {
        showDismissUnsavedChangesDialog = false
    }

    fun onDiscardChanges(onDismiss: () -> Unit) {
        showDismissUnsavedChangesDialog = false
        onDismiss()
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
            reminders = reminders.sortedBy { it.daysBeforePayment },
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

    /**
     * Check if there are unsaved changes compared to the database values
     */
    private suspend fun hasUnsavedChanges(): Boolean {
        if (expenseId == null) {
            // For new expenses, check if any field has been filled in
            return nameState.isNotBlank() ||
                descriptionState.isNotBlank() ||
                priceState.isNotBlank() ||
                everyXRecurrenceState.isNotBlank() ||
                selectedRecurrence != Recurrence.Monthly ||
                firstPaymentDate != null ||
                tags.any { it.second } ||
                !notifyForExpense ||
                reminders.size != 1 ||
                reminders.firstOrNull()?.daysBeforePayment != defaultReminderDays
        } else {
            // For existing expenses, compare with database values
            val expense = expenseRepository.getRecurringExpenseById(expenseId) ?: return false

            // Compare each field
            if (nameState != expense.name) return true
            if (descriptionState != expense.description) return true
            if (priceState != expense.price.value.toLocalString()) return true
            if (selectedCurrencyOption.currencyCode != expense.price.currencyCode) return true
            if (everyXRecurrenceState != expense.everyXRecurrence.toString()) return true
            if (selectedRecurrence != expense.recurrence) return true
            if (firstPaymentDate != expense.firstPayment) return true
            if (notifyForExpense != expense.notifyForExpense) return true

            // Compare tags
            val currentSelectedTags = tags.filter { it.second }.map { it.first }.toSet()
            val dbSelectedTags = expense.tags.toSet()
            if (currentSelectedTags != dbSelectedTags) return true

            // Compare reminders
            val currentReminders = reminders.sortedBy { it.daysBeforePayment }
            val dbReminders = expense.reminders.sortedBy { it.daysBeforePayment }

            // Handle the case where DB has no reminders but UI shows default
            val dbRemindersToCompare =
                if (dbReminders.isEmpty() && expense.notifyForExpense) {
                    listOf(Reminder(id = 0, daysBeforePayment = defaultReminderDays))
                } else {
                    dbReminders
                }

            if (currentReminders.size != dbRemindersToCompare.size) return true
            currentReminders.zip(dbRemindersToCompare).forEach { (current, db) ->
                if (current.daysBeforePayment != db.daysBeforePayment) return true
            }

            return false
        }
    }
}
