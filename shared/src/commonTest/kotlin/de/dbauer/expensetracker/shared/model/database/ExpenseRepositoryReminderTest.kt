package de.dbauer.expensetracker.shared.model.database

import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.Recurrence
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Reminder
import de.dbauer.expensetracker.shared.model.datastore.FakeUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ExpenseRepositoryReminderTest {
    private lateinit var dao: FakeRecurringExpenseDao
    private lateinit var repository: ExpenseRepository
    private lateinit var userPreferencesRepository: FakeUserPreferencesRepository

    private class FakeRecurringExpenseDao : RecurringExpenseDao {
        val expenses = mutableMapOf<Int, RecurringExpenseWithTagsEntry>()
        val reminders = mutableMapOf<Int, ReminderEntry>()
        val tags = mutableMapOf<Int, TagEntry>()
        val crossRefs = mutableListOf<ExpenseTagCrossRefEntry>()

        private var nextExpenseId = 1
        private var nextReminderId = 1

        override fun getAllExpenses(): Flow<List<RecurringExpenseWithTagsEntry>> {
            return flowOf(expenses.values.toList())
        }

        override fun getAllExpensesByPrice(): Flow<List<RecurringExpenseWithTagsEntry>> {
            return flowOf(expenses.values.sortedByDescending { it.expense.price }.toList())
        }

        override fun getAllTags(): Flow<List<TagEntry>> {
            return flowOf(tags.values.toList())
        }

        override suspend fun getExpenseById(id: Int): RecurringExpenseWithTagsEntry? {
            val expenseEntry = expenses[id] ?: return null
            val expenseReminders = reminders.values.filter { it.expenseId == id }
            return expenseEntry.copy(reminders = expenseReminders)
        }

        override suspend fun getTagById(id: Int): TagEntry {
            return tags[id] ?: throw NoSuchElementException()
        }

        override suspend fun insert(recurringExpense: RecurringExpenseEntry): Long {
            val id = if (recurringExpense.id == 0) nextExpenseId++ else recurringExpense.id
            expenses[id] =
                RecurringExpenseWithTagsEntry(
                    expense = recurringExpense.copy(id = id),
                    tags = emptyList(),
                    reminders = emptyList(),
                )
            return id.toLong()
        }

        override suspend fun update(recurringExpense: RecurringExpenseEntry) {
            val existing = expenses[recurringExpense.id]
            if (existing != null) {
                expenses[recurringExpense.id] = existing.copy(expense = recurringExpense)
            }
        }

        override suspend fun delete(recurringExpense: RecurringExpenseEntry) {
            expenses.remove(recurringExpense.id)
        }

        override suspend fun insert(tag: TagEntry) {
            tags[tag.id] = tag
        }

        override suspend fun update(tag: TagEntry) {
            tags[tag.id] = tag
        }

        override suspend fun delete(tag: TagEntry) {
            tags.remove(tag.id)
        }

        override suspend fun upsert(expenseTagCrossRef: ExpenseTagCrossRefEntry) {
            crossRefs.add(expenseTagCrossRef)
        }

        override suspend fun delete(expenseTagCrossRef: ExpenseTagCrossRefEntry) {
            crossRefs.remove(expenseTagCrossRef)
        }

        override suspend fun deleteAllCrossRefForTagId(tagId: Int) {
            val toRemove = crossRefs.filter { it.tagId == tagId }
            crossRefs.removeAll(toRemove)
        }

        override suspend fun deleteAllCrossRefForExpenseId(expenseId: Int) {
            val toRemove = crossRefs.filter { it.expenseId == expenseId }
            crossRefs.removeAll(toRemove)
        }

        override suspend fun insertReminder(reminder: ReminderEntry): Long {
            val id = if (reminder.id == 0) nextReminderId++ else reminder.id
            val reminderWithId = reminder.copy(id = id)
            reminders[id] = reminderWithId
            return id.toLong()
        }

        override suspend fun updateReminder(reminder: ReminderEntry) {
            reminders[reminder.id] = reminder
        }

        override suspend fun deleteReminder(reminder: ReminderEntry) {
            reminders.remove(reminder.id)
        }

        override suspend fun deleteAllRemindersForExpenseId(expenseId: Int) {
            val toRemove = reminders.values.filter { it.expenseId == expenseId }.map { it.id }
            toRemove.forEach { reminders.remove(it) }
        }

        override suspend fun getRemindersForExpense(expenseId: Int): List<ReminderEntry> {
            return reminders.values.filter { it.expenseId == expenseId }
        }
    }

    @BeforeTest
    fun setup() {
        dao = FakeRecurringExpenseDao()
        userPreferencesRepository = FakeUserPreferencesRepository()
        repository = ExpenseRepository(dao, userPreferencesRepository)
    }

    @Test
    fun `insert expense with reminders saves all reminders`() =
        runTest {
            val expense =
                RecurringExpenseData(
                    id = 1,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(id = 0, daysBeforePayment = 3),
                            Reminder(id = 0, daysBeforePayment = 7),
                            Reminder(id = 0, daysBeforePayment = 14),
                        ),
                )

            repository.insert(expense)

            val savedReminders = dao.getRemindersForExpense(1)
            assertEquals(3, savedReminders.size)
            assertTrue(savedReminders.any { it.daysBeforePayment == 3 })
            assertTrue(savedReminders.any { it.daysBeforePayment == 7 })
            assertTrue(savedReminders.any { it.daysBeforePayment == 14 })
        }

    @Test
    fun `insert expense without reminders saves no reminders`() =
        runTest {
            val expense =
                RecurringExpenseData(
                    id = 2,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = false,
                    reminders = emptyList(),
                )

            repository.insert(expense)

            val savedReminders = dao.getRemindersForExpense(2)
            assertEquals(0, savedReminders.size)
        }

    @Test
    fun `update expense adds new reminders`() =
        runTest {
            // Insert initial expense with one reminder
            val initialExpense =
                RecurringExpenseData(
                    id = 3,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(id = 0, daysBeforePayment = 3),
                        ),
                )
            repository.insert(initialExpense)

            val savedReminders = dao.getRemindersForExpense(3)
            assertEquals(1, savedReminders.size)

            // Update with additional reminders
            val updatedExpense =
                initialExpense.copy(
                    reminders =
                        listOf(
                            Reminder(id = savedReminders[0].id, daysBeforePayment = 3),
                            Reminder(id = 0, daysBeforePayment = 7),
                            Reminder(id = 0, daysBeforePayment = 14),
                        ),
                )
            repository.update(updatedExpense)

            val finalReminders = dao.getRemindersForExpense(3)
            assertEquals(3, finalReminders.size)
        }

    @Test
    fun `update expense updates existing reminders`() =
        runTest {
            // Insert initial expense with one reminder
            val initialExpense =
                RecurringExpenseData(
                    id = 4,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(id = 0, daysBeforePayment = 3),
                        ),
                )
            repository.insert(initialExpense)

            val savedReminders = dao.getRemindersForExpense(4)
            val reminderId = savedReminders[0].id

            // Update the reminder's days
            val updatedExpense =
                initialExpense.copy(
                    reminders =
                        listOf(
                            Reminder(id = reminderId, daysBeforePayment = 5),
                        ),
                )
            repository.update(updatedExpense)

            val finalReminders = dao.getRemindersForExpense(4)
            assertEquals(1, finalReminders.size)
            assertEquals(5, finalReminders[0].daysBeforePayment)
            assertEquals(reminderId, finalReminders[0].id)
        }

    @Test
    fun `update expense removes deleted reminders`() =
        runTest {
            // Insert initial expense with multiple reminders
            val initialExpense =
                RecurringExpenseData(
                    id = 5,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(id = 0, daysBeforePayment = 3),
                            Reminder(id = 0, daysBeforePayment = 7),
                            Reminder(id = 0, daysBeforePayment = 14),
                        ),
                )
            repository.insert(initialExpense)

            val savedReminders = dao.getRemindersForExpense(5)
            assertEquals(3, savedReminders.size)

            // Update with only one reminder
            val updatedExpense =
                initialExpense.copy(
                    reminders =
                        listOf(
                            Reminder(id = savedReminders[0].id, daysBeforePayment = 3),
                        ),
                )
            repository.update(updatedExpense)

            val finalReminders = dao.getRemindersForExpense(5)
            assertEquals(1, finalReminders.size)
            assertEquals(3, finalReminders[0].daysBeforePayment)
        }

    @Test
    fun `delete expense removes all associated reminders`() =
        runTest {
            val expense =
                RecurringExpenseData(
                    id = 6,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(id = 0, daysBeforePayment = 3),
                            Reminder(id = 0, daysBeforePayment = 7),
                        ),
                )

            repository.insert(expense)
            assertEquals(2, dao.getRemindersForExpense(6).size)

            repository.delete(expense)

            assertEquals(0, dao.getRemindersForExpense(6).size)
        }

    @Test
    fun `getRecurringExpenseById returns expense with reminders`() =
        runTest {
            val expense =
                RecurringExpenseData(
                    id = 7,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(id = 0, daysBeforePayment = 3),
                            Reminder(id = 0, daysBeforePayment = 10),
                        ),
                )

            repository.insert(expense)

            val retrieved = repository.getRecurringExpenseById(7)

            assertNotNull(retrieved)
            assertEquals(2, retrieved.reminders.size)
            assertTrue(retrieved.reminders.any { it.daysBeforePayment == 3 })
            assertTrue(retrieved.reminders.any { it.daysBeforePayment == 10 })
        }

    @Test
    fun `reminders maintain lastNotificationDate through save and retrieve`() =
        runTest {
            val notificationDate = Instant.fromEpochMilliseconds(1234567890000L)
            val expense =
                RecurringExpenseData(
                    id = 8,
                    name = "Test Expense",
                    description = "Description",
                    price = CurrencyValue(100f, "USD"),
                    monthlyPrice = CurrencyValue(100f, "USD"),
                    everyXRecurrence = 1,
                    recurrence = Recurrence.Monthly,
                    tags = emptyList(),
                    firstPayment = null,
                    notifyForExpense = true,
                    reminders =
                        listOf(
                            Reminder(
                                id = 0,
                                daysBeforePayment = 3,
                                lastNotificationDate = notificationDate,
                            ),
                        ),
                )

            repository.insert(expense)

            val retrieved = repository.getRecurringExpenseById(8)

            assertNotNull(retrieved)
            assertEquals(1, retrieved.reminders.size)
            assertEquals(notificationDate, retrieved.reminders[0].lastNotificationDate)
        }
}
