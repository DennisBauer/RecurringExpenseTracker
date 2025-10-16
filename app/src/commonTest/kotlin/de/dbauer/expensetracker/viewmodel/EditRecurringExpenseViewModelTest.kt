package de.dbauer.expensetracker.viewmodel

import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.Reminder
import de.dbauer.expensetracker.model.FakeCurrencyProvider
import de.dbauer.expensetracker.model.database.FakeExpenseRepository
import de.dbauer.expensetracker.model.datastore.FakeUserPreferencesRepository
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
    fun `hasUnsavedChanges returns false when no changes made`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            assertFalse(viewModel.hasUnsavedChanges())
        }

    @Test
    fun `hasUnsavedChanges returns true when name changed`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "New Name"

            assertTrue(viewModel.hasUnsavedChanges())
        }

    @Test
    fun `hasUnsavedChanges returns true when price changed`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.priceState = "50"

            assertTrue(viewModel.hasUnsavedChanges())
        }

    @Test
    fun `hasUnsavedChanges returns true when reminder added`() =
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

            assertTrue(viewModel.hasUnsavedChanges())
        }

    @Test
    fun `hasUnsavedChanges returns false when editing existing expense without changes`() =
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
                    reminders = listOf(Reminder(id = 1, daysBeforePayment = 5)),
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

            assertFalse(viewModel.hasUnsavedChanges())
        }

    @Test
    fun `hasUnsavedChanges returns true when editing existing expense name`() =
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

            viewModel.nameState = "Modified Name"

            assertTrue(viewModel.hasUnsavedChanges())
        }

    @Test
    fun `onBackPressed shows dialog when changes exist`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "Changed Name"
            
            var navigatedBack = false
            viewModel.onBackPressed { navigatedBack = true }

            assertTrue(viewModel.showUnsavedChangesDialog)
            assertFalse(navigatedBack)
        }

    @Test
    fun `onBackPressed navigates back when no changes exist`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            var navigatedBack = false
            viewModel.onBackPressed { navigatedBack = true }

            assertFalse(viewModel.showUnsavedChangesDialog)
            assertTrue(navigatedBack)
        }

    @Test
    fun `onDiscardChanges dismisses dialog`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "Changed Name"
            
            var navigatedBack = false
            viewModel.onBackPressed { navigatedBack = true }

            assertTrue(viewModel.showUnsavedChangesDialog)
            assertFalse(navigatedBack)

            var dismissed = false
            viewModel.onDiscardChanges { dismissed = true }

            assertFalse(viewModel.showUnsavedChangesDialog)
            assertTrue(dismissed)
        }

    @Test
    fun `onSaveChanges saves and dismisses when input valid`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            viewModel.nameState = "New Expense"
            viewModel.priceState = "100"
            
            var navigatedBack = false
            viewModel.onBackPressed { navigatedBack = true }

            assertTrue(viewModel.showUnsavedChangesDialog)
            assertFalse(navigatedBack)

            var dismissed = false
            viewModel.onSaveChanges { dismissed = true }
            advanceUntilIdle()

            assertFalse(viewModel.showUnsavedChangesDialog)
            assertTrue(dismissed)

            // Verify expense was saved
            val expenses = expenseRepository.allRecurringExpenses.first()
            assertEquals(1, expenses.size)
            assertEquals("New Expense", expenses[0].name)
        }

    @Test
    fun `onSaveChanges keeps dialog open when input invalid`() =
        runTest {
            val viewModel =
                EditRecurringExpenseViewModel(
                    expenseId = null,
                    expenseRepository = expenseRepository,
                    currencyProvider = currencyProvider,
                    userPreferencesRepository = userPreferencesRepository,
                )

            advanceUntilIdle()

            // Set invalid input (empty name)
            viewModel.nameState = ""
            viewModel.priceState = "100"
            
            var navigatedBack = false
            viewModel.onBackPressed { navigatedBack = true }

            assertTrue(viewModel.showUnsavedChangesDialog)

            var dismissed = false
            viewModel.onSaveChanges { dismissed = true }
            advanceUntilIdle()

            // Dialog should remain open because save failed
            assertTrue(viewModel.showUnsavedChangesDialog)
            assertFalse(dismissed)

            // Verify no expense was saved
            val expenses = expenseRepository.allRecurringExpenses.first()
            assertEquals(0, expenses.size)
        }
}
