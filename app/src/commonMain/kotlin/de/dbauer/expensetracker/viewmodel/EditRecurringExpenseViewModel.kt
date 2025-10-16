package de.dbauer.expensetracker.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import de.dbauer.expensetracker.data.Reminder
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.model.CurrencyProvider
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.model.getSystemCurrencyCode
import de.dbauer.expensetracker.toFloatLocaleAware
import de.dbauer.expensetracker.toLocalString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

    var reminders = mutableStateListOf<Reminder>()
        private set

    // Store the last reminder configuration before disabling
    private var lastRemindersBeforeDisabling = mutableListOf<Reminder>()

    var showDeleteConfirmDialog by mutableStateOf(false)
    var showUnsavedChangesDialog by mutableStateOf(false)

    val isNewExpense = expenseId == null
    val showDeleteButton = !isNewExpense

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()
    private var defaultReminderDays by mutableIntStateOf(0)

    // Store the original database expense for change detection
    private var originalExpense: RecurringExpenseData? = null

    init {
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
                        // Store the original expense for change detection
                        originalExpense = expense
                        
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
                // For new expenses, originalExpense remains null
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

    fun hasUnsavedChanges(): Boolean {
        if (originalExpense != null) {
            // Editing existing expense - compare with database entry
            val expense = originalExpense!!
            
            if (nameState != expense.name) return true
            if (descriptionState != expense.description) return true
            if (priceState != expense.price.value.toLocalString()) return true
            if (selectedCurrencyOption.currencyCode != expense.price.currencyCode) return true
            if (everyXRecurrenceState != expense.everyXRecurrence.toString()) return true
            if (selectedRecurrence != expense.recurrence) return true
            if (firstPaymentDate != expense.firstPayment) return true
            if (notifyForExpense != expense.notifyForExpense) return true
            
            // Compare tags
            val originalSelectedTags = expense.tags.toSet()
            val currentSelectedTags = _tags.filter { it.value }.keys.toSet()
            if (originalSelectedTags != currentSelectedTags) return true
            
            // Compare reminders
            val currentRemindersSorted = reminders.sortedBy { it.daysBeforePayment }
            val originalRemindersSorted = if (expense.reminders.isEmpty() && expense.notifyForExpense) {
                // If original had no reminders but notifications enabled, it would show default
                listOf(Reminder(id = 0, daysBeforePayment = defaultReminderDays))
            } else {
                expense.reminders.sortedBy { it.daysBeforePayment }
            }
            
            if (currentRemindersSorted.size != originalRemindersSorted.size) return true
            if (currentRemindersSorted.zip(originalRemindersSorted).any { (current, original) ->
                    current.daysBeforePayment != original.daysBeforePayment
                }
            ) {
                return true
            }
        } else {
            // New expense - compare with default/empty values
            if (nameState.isNotEmpty()) return true
            if (descriptionState.isNotEmpty()) return true
            if (priceState.isNotEmpty()) return true
            if (everyXRecurrenceState.isNotEmpty()) return true
            if (selectedRecurrence != Recurrence.Monthly) return true
            if (firstPaymentDate != null) return true
            
            // Check if any tags are selected
            if (_tags.any { it.value }) return true
            
            // For new expense with notifications enabled, check if reminders differ from default
            if (!notifyForExpense) return true // notifications are disabled by default, so this is a change
            val currentRemindersSorted = reminders.sortedBy { it.daysBeforePayment }
            val defaultReminders = listOf(Reminder(id = 0, daysBeforePayment = defaultReminderDays))
            if (currentRemindersSorted.size != defaultReminders.size) return true
            if (currentRemindersSorted.zip(defaultReminders).any { (current, default) ->
                    current.daysBeforePayment != default.daysBeforePayment
                }
            ) {
                return true
            }
        }
        
        return false
    }

    fun onBackPressed(onNavigateBack: () -> Unit) {
        if (hasUnsavedChanges()) {
            showUnsavedChangesDialog = true
        } else {
            onNavigateBack()
        }
    }

    fun onDismissUnsavedChangesDialog() {
        showUnsavedChangesDialog = false
    }

    fun onDiscardChanges(onDismiss: () -> Unit) {
        showUnsavedChangesDialog = false
        onDismiss()
    }

    fun onSaveChanges(onDismiss: () -> Unit) {
        updateExpense { successful ->
            if (successful) {
                showUnsavedChangesDialog = false
                onDismiss()
            }
            // If not successful, dialog remains open and validation errors are shown
        }
    }
}
