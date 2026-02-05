package de.dbauer.expensetracker.shared.viewmodel

import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.Recurrence
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Reminder
import de.dbauer.expensetracker.shared.model.FakeCurrencyProvider
import de.dbauer.expensetracker.shared.model.database.FakeExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EditRecurringExpenseViewModelTest {
    private lateinit var expenseRepository: FakeExpenseRepository
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository
    private lateinit var currencyProvider: FakeCurrencyProvider
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        expenseRepository = FakeExpenseRepository()
        userPreferencesRepository = FakeUserPreferencesRepository()
        currencyProvider = FakeCurrencyProvider()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `new expense has default reminder when notifications enabled`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            assertTrue(viewModel.notifyForExpense)
            assertEquals(1, viewModel.reminders.size)
            assertEquals(3, viewModel.reminders[0].daysBeforePayment) // Default is 3 days
        }

    @Test
    fun `onNotifyForExpenseChange enables notifications and adds default reminder`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Disable notifications
            viewModel.onNotifyForExpenseChange(false)

            assertFalse(viewModel.notifyForExpense)
            assertEquals(0, viewModel.reminders.size)

            // Re-enable notifications
            viewModel.onNotifyForExpenseChange(true)

            assertTrue(viewModel.notifyForExpense)
            assertEquals(1, viewModel.reminders.size)
            assertEquals(3, viewModel.reminders[0].daysBeforePayment)
        }

    @Test
    fun `onNotifyForExpenseChange restores previous reminders when re-enabled`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Add multiple reminders
            viewModel.addReminder(7)
            viewModel.addReminder(14)

            assertEquals(3, viewModel.reminders.size)

            // Disable notifications (should store reminders)
            viewModel.onNotifyForExpenseChange(false)
            assertEquals(0, viewModel.reminders.size)

            // Re-enable notifications (should restore stored reminders)
            viewModel.onNotifyForExpenseChange(true)

            assertEquals(3, viewModel.reminders.size)
            assertTrue(viewModel.reminders.any { it.daysBeforePayment == 3 })
            assertTrue(viewModel.reminders.any { it.daysBeforePayment == 7 })
            assertTrue(viewModel.reminders.any { it.daysBeforePayment == 14 })
        }

    @Test
    fun `addReminder adds new reminder with specified days`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.addReminder(5)

            assertTrue(viewModel.reminders.any { it.daysBeforePayment == 5 })
            assertEquals(2, viewModel.reminders.size) // Default + new one
        }

    @Test
    fun `addReminder prevents duplicate days before payment`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            val initialSize = viewModel.reminders.size

            // Try to add a reminder with the same days as default
            viewModel.addReminder(3)

            // Size should not change because duplicate is prevented
            assertEquals(initialSize, viewModel.reminders.size)
        }

    @Test
    fun `updateReminder updates existing reminder at index`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.addReminder(7)
            viewModel.addReminder(14)

            // Update the first reminder (which should be 3 days after sorting)
            viewModel.updateReminder(0, 5)

            val reminders = viewModel.reminders
            assertEquals(5, reminders[0].daysBeforePayment)
            assertEquals(3, reminders.size)
        }

    @Test
    fun `removeReminder removes reminder at index`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.addReminder(7)
            viewModel.addReminder(14)

            val initialSize = viewModel.reminders.size
            assertEquals(3, initialSize)

            // Remove the first reminder (should be day 3 after sorting)
            viewModel.removeReminder(0)

            assertEquals(2, viewModel.reminders.size)
            assertTrue(viewModel.reminders.any { it.daysBeforePayment == 7 })
            assertTrue(viewModel.reminders.any { it.daysBeforePayment == 14 })
        }

    @Test
    fun `reminders are sorted by daysBeforePayment`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Add reminders in non-sorted order
            viewModel.addReminder(14)
            viewModel.addReminder(1)
            viewModel.addReminder(7)

            // The in-memory list is not sorted (maintains insertion order)
            val remindersBeforeSave = viewModel.reminders
            assertEquals(4, remindersBeforeSave.size) // 3 (default) + 3 added
            assertEquals(3, remindersBeforeSave[0].daysBeforePayment) // Default reminder
            assertEquals(14, remindersBeforeSave[1].daysBeforePayment)
            assertEquals(1, remindersBeforeSave[2].daysBeforePayment)
            assertEquals(7, remindersBeforeSave[3].daysBeforePayment)

            // Set required fields
            viewModel.nameState = "Test Expense"
            viewModel.priceState = "100"

            // Save the expense - this should sort the reminders
            var saveSuccessful = false
            viewModel.updateExpense { successful ->
                saveSuccessful = successful
            }
            advanceUntilIdle()

            assertTrue(saveSuccessful)

            // Load the expense in a new ViewModel - reminders should be sorted when loaded from DB
            val savedExpenses = expenseRepository.allRecurringExpenses.first()
            assertEquals(1, savedExpenses.size)
            val savedExpenseId = savedExpenses[0].id

            val loadedViewModel =
                EditRecurringExpenseViewModel(
                    expenseId = savedExpenseId,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // When loaded from database, reminders are sorted
            val loadedReminders = loadedViewModel.reminders
            assertEquals(4, loadedReminders.size)
            assertEquals(1, loadedReminders[0].daysBeforePayment)
            assertEquals(3, loadedReminders[1].daysBeforePayment)
            assertEquals(7, loadedReminders[2].daysBeforePayment)
            assertEquals(14, loadedReminders[3].daysBeforePayment)
        }

    @Test
    fun `editing existing expense loads reminders correctly`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Test Expense",
                    description = "Test Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(id = 1, daysBeforePayment = 5),
                            Reminder(id = 2, daysBeforePayment = 10),
                        ),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            assertEquals(2, viewModel.reminders.size)
            assertEquals(5, viewModel.reminders[0].daysBeforePayment)
            assertEquals(10, viewModel.reminders[1].daysBeforePayment)
            assertTrue(viewModel.notifyForExpense)
        }

    @Test
    fun `editing expense with notifications enabled but no reminders shows default`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Test Expense",
                    description = "Test Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders = emptyList(),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Should show the global default reminder
            assertEquals(1, viewModel.reminders.size)
            assertEquals(3, viewModel.reminders[0].daysBeforePayment)
            assertTrue(viewModel.notifyForExpense)
        }

    @Test
    fun `editing expense with notifications disabled has no reminders`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Test Expense",
                    description = "Test Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = false,
                    reminders = emptyList(),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            assertEquals(0, viewModel.reminders.size)
            assertFalse(viewModel.notifyForExpense)
        }

    @Test
    fun `onDismissUnsavedChangesDialog hides the dialog`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "Test"
            viewModel.onBackPressedWithUnsavedChanges()

            advanceUntilIdle()

            assertTrue(viewModel.showDismissUnsavedChangesDialog)

            viewModel.onDismissUnsavedChangesDialog()

            assertFalse(viewModel.showDismissUnsavedChangesDialog)
        }

    @Test
    fun `onDiscardChanges dismisses and calls callback`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "Test"
            viewModel.onBackPressedWithUnsavedChanges()

            advanceUntilIdle()

            assertTrue(viewModel.showDismissUnsavedChangesDialog)

            var dismissed = false
            viewModel.onDiscardChanges { dismissed = true }

            assertFalse(viewModel.showDismissUnsavedChangesDialog)
            assertTrue(dismissed)
        }

    @Test
    fun `existing expense with default reminder shown when DB has no reminders matches default`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Netflix",
                    description = "Streaming",
                    price = CurrencyValue(9.99f, "USD"),
                    monthlyPrice = CurrencyValue(9.99f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders = emptyList(),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Should show default reminder but no unsaved changes
            assertEquals(1, viewModel.reminders.size)
            assertEquals(3, viewModel.reminders[0].daysBeforePayment)

            // Should have no unsaved changes since the default reminder matches expectations
            assertFalse(viewModel.hasUnsavedChanges)
        }

    @Test
    fun `updateExpense fails when name is blank`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = ""
            viewModel.priceState = "10.00"

            var updateSuccessful: Boolean? = null
            viewModel.updateExpense { successful ->
                updateSuccessful = successful
            }

            advanceUntilIdle()

            assertFalse(updateSuccessful!!)
            assertTrue(viewModel.nameInputError.value)
        }

    @Test
    fun `updateExpense fails when price is invalid`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "Netflix"
            viewModel.priceState = "invalid"

            var updateSuccessful: Boolean? = null
            viewModel.updateExpense { successful ->
                updateSuccessful = successful
            }

            advanceUntilIdle()

            assertFalse(updateSuccessful!!)
            assertTrue(viewModel.priceInputError.value)
        }

    @Test
    fun `updateExpense succeeds with valid input for new expense`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "Netflix"
            viewModel.priceState = "12.99"

            var updateSuccessful: Boolean? = null
            viewModel.updateExpense { successful ->
                updateSuccessful = successful
            }

            advanceUntilIdle()

            assertTrue(updateSuccessful!!)
            assertFalse(viewModel.nameInputError.value)
            assertFalse(viewModel.priceInputError.value)

            // Verify the expense was saved
            val expenses = expenseRepository.allRecurringExpenses.first()
            assertEquals(1, expenses.size)
            assertEquals("Netflix", expenses[0].name)
        }

    @Test
    fun `updateExpense succeeds with valid input for existing expense`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Netflix",
                    description = "Streaming",
                    price = CurrencyValue(9.99f, "USD"),
                    monthlyPrice = CurrencyValue(9.99f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders = listOf(Reminder(id = 1, daysBeforePayment = 3)),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "Netflix Premium"
            viewModel.priceState = "15.99"

            var updateSuccessful: Boolean? = null
            viewModel.updateExpense { successful ->
                updateSuccessful = successful
            }

            advanceUntilIdle()

            assertTrue(updateSuccessful!!)

            // Verify the expense was updated
            val updatedExpense = expenseRepository.getRecurringExpenseById(1)
            assertEquals("Netflix Premium", updatedExpense?.name)
            assertEquals(15.99f, updatedExpense?.price?.value)
        }

    @Test
    fun `onDeleteClick shows delete confirmation dialog`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Netflix",
                    description = "Streaming",
                    price = CurrencyValue(9.99f, "USD"),
                    monthlyPrice = CurrencyValue(9.99f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders = emptyList(),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            assertFalse(viewModel.showDeleteConfirmDialog)

            viewModel.onDeleteClick()

            assertTrue(viewModel.showDeleteConfirmDialog)
        }

    @Test
    fun `onDismissDeleteDialog hides delete confirmation dialog`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Netflix",
                    description = "Streaming",
                    price = CurrencyValue(9.99f, "USD"),
                    monthlyPrice = CurrencyValue(9.99f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders = emptyList(),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.onDeleteClick()
            assertTrue(viewModel.showDeleteConfirmDialog)

            viewModel.onDismissDeleteDialog()
            assertFalse(viewModel.showDeleteConfirmDialog)
        }

    @Test
    fun `deleteExpense deletes the expense`() =
        runTest {
            val existingExpense =
                RecurringExpenseData(
                    id = 1,
                    name = "Netflix",
                    description = "Streaming",
                    price = CurrencyValue(9.99f, "USD"),
                    monthlyPrice = CurrencyValue(9.99f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders = emptyList(),
                )

            expenseRepository.insert(existingExpense)

            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = 1,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Verify expense exists
            var expenses = expenseRepository.allRecurringExpenses.first()
            assertEquals(1, expenses.size)

            viewModel.deleteExpense()
            advanceUntilIdle()

            // Verify expense was deleted
            expenses = expenseRepository.allRecurringExpenses.first()
            assertEquals(0, expenses.size)
            assertFalse(viewModel.showDeleteConfirmDialog)
        }

    @Test
    fun `isReminderDuplicate returns true for duplicate days`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.addReminder(7)

            // Try to check if 3 (default) is a duplicate when editing the second reminder
            assertTrue(viewModel.isReminderDuplicate(1, 3))
            assertFalse(viewModel.isReminderDuplicate(1, 14))
        }

    @Test
    fun `isNewReminderDuplicate returns true for existing days`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Default reminder is 3 days
            assertTrue(viewModel.isNewReminderDuplicate(3))
            assertFalse(viewModel.isNewReminderDuplicate(7))

            viewModel.addReminder(7)

            assertTrue(viewModel.isNewReminderDuplicate(7))
            assertFalse(viewModel.isNewReminderDuplicate(14))
        }

    @Test
    fun `updateReminder prevents creating duplicates`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.addReminder(7)
            viewModel.addReminder(14)

            val initialSize = viewModel.reminders.size
            assertEquals(3, initialSize) // 3, 7, 14

            // Try to update the second reminder (7) to 3 (which already exists)
            viewModel.updateReminder(1, 3)

            // Should not have changed
            assertEquals(initialSize, viewModel.reminders.size)
            assertTrue(viewModel.reminders.any { it.daysBeforePayment == 7 })
        }

    @Test
    fun `removing last reminder disables notifications`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            assertTrue(viewModel.notifyForExpense)
            assertEquals(1, viewModel.reminders.size)

            viewModel.removeReminder(0)

            assertFalse(viewModel.notifyForExpense)
            assertEquals(0, viewModel.reminders.size)
        }
}
