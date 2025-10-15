package de.dbauer.expensetracker.model.notification

import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.Reminder
import de.dbauer.expensetracker.model.database.FakeExpenseRepository
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.datastore.FakeUserPreferencesRepository
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ExpenseNotificationManagerTest {
    // Fixed test time: October 15, 2025 at 00:00:00 UTC
    private val testCurrentTime = LocalDateTime(2025, 10, 15, 0, 0, 0, 0).toInstant(TimeZone.UTC)

    private class TestableExpenseNotificationManager(
        expenseRepository: IExpenseRepository,
        userPreferencesRepository: IUserPreferencesRepository,
        currentTimeProvider: () -> Instant,
    ) : ExpenseNotificationManager(expenseRepository, userPreferencesRepository, currentTimeProvider) {
        // Override to provide test descriptions without needing resources
        override suspend fun getNotificationDescription(daysToNextPayment: Int): String {
            return when (daysToNextPayment) {
                0 -> "Due today"
                1 -> "Due tomorrow"
                else -> "Due in $daysToNextPayment days"
            }
        }
    }

    private fun createTestExpense(
        id: Int,
        name: String = "Test Expense",
        notifyForExpense: Boolean = true,
        firstPayment: Instant? = LocalDateTime(2025, 1, 1, 0, 0, 0, 0).toInstant(TimeZone.UTC),
        reminders: List<Reminder> = emptyList(),
    ): RecurringExpenseData {
        return RecurringExpenseData(
            id = id,
            name = name,
            description = "",
            price = CurrencyValue(10f, "EUR"),
            monthlyPrice = CurrencyValue(10f, "EUR"),
            everyXRecurrence = 1,
            recurrence = Recurrence.Monthly,
            tags = emptyList(),
            firstPayment = firstPayment,
            notifyForExpense = notifyForExpense,
            reminders = reminders,
        )
    }

    @Test
    fun `expense with notifications disabled returns no notifications`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val expense =
                createTestExpense(
                    id = 1,
                    notifyForExpense = false,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `expense with no next payment date returns no notifications`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = null,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `expense with default reminder within notification window creates notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            // Create expense with payment 2 days from now (within 3-day window)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Netflix Subscription",
                    firstPayment = LocalDateTime(2025, 10, 15, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertEquals(1, notifications.size)
            assertEquals(1, notifications[0].id)
            assertEquals("Netflix Subscription", notifications[0].title)
            assertEquals(NotificationChannel.ExpenseReminder, notifications[0].channel)
            // Description is set but the exact text can not be verified the without mocking resources
        }

    @Test
    fun `expense with default reminder outside notification window returns no notifications`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            // Create expense with payment 10 days from now (outside 3-day window)
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = LocalDateTime(2025, 10, 23, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `expense with custom reminder within window creates notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 5,
                    lastNotificationDate = null,
                )

            // Create expense with payment 3 days from now (within 5-day reminder window)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Rent Payment",
                    firstPayment = LocalDateTime(2025, 10, 16, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                    reminders = listOf(reminder),
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertEquals(1, notifications.size)
            assertEquals("Rent Payment", notifications[0].title)
            assertEquals(1, notifications[0].id)
        }

    @Test
    fun `expense with multiple reminders returns only one notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val reminders =
                listOf(
                    Reminder(id = 1, daysBeforePayment = 7),
                    Reminder(id = 2, daysBeforePayment = 3),
                    Reminder(id = 3, daysBeforePayment = 1),
                )

            // Create expense with payment 5 days from now (matches reminders with 7 and 3 days)
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = LocalDateTime(2025, 10, 18, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                    reminders = reminders,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            // Should only get ONE notification even though multiple reminders match
            assertEquals(1, notifications.size)
            assertEquals(1, notifications[0].id)
        }

    @Test
    fun `expense with already shown reminder does not create duplicate notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val nextPaymentDate = LocalDateTime(2025, 10, 16, 0, 0, 0, 0).toInstant(TimeZone.UTC)
            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 5,
                    lastNotificationDate = nextPaymentDate, // Already notified for this payment
                )

            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = LocalDateTime(2025, 10, 16, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                    reminders = listOf(reminder),
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `markNotificationAsShown updates reminder with last notification date`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 5,
                    lastNotificationDate = null,
                )

            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = LocalDateTime(2025, 10, 16, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                    reminders = listOf(reminder),
                )
            repository.insert(expense)

            manager.markNotificationAsShown(1)

            val updatedExpense = repository.getRecurringExpenseById(1)
            assertNotNull(updatedExpense)
            val updatedReminder = updatedExpense.reminders.first()
            assertNotNull(updatedReminder.lastNotificationDate)
        }

    @Test
    fun `markNotificationAsShown updates all triggered reminders`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val reminders =
                listOf(
                    Reminder(id = 1, daysBeforePayment = 7),
                    Reminder(id = 2, daysBeforePayment = 3),
                    Reminder(id = 3, daysBeforePayment = 1),
                )

            // Payment is 3 days away, so reminders with 7 and 3 days should be marked as shown
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = LocalDateTime(2025, 10, 18, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                    reminders = reminders,
                )
            repository.insert(expense)

            manager.markNotificationAsShown(1)

            val updatedExpense = repository.getRecurringExpenseById(1)
            assertNotNull(updatedExpense)
            val updated = updatedExpense.reminders

            // Reminders with >= 3 days should be marked as shown (7 and 3 day reminders)
            assertNotNull(updated[0].lastNotificationDate) // 7 days
            assertNotNull(updated[1].lastNotificationDate) // 3 days
            assertNull(updated[2].lastNotificationDate) // 1 day (not triggered yet)
        }

    @Test
    fun `markNotificationAsShown creates synthetic reminder for default notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = LocalDateTime(2025, 10, 16, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                    reminders = emptyList(), // No custom reminders
                )
            repository.insert(expense)

            manager.markNotificationAsShown(1)

            val updatedExpense = repository.getRecurringExpenseById(1)
            assertNotNull(updatedExpense)
            assertEquals(1, updatedExpense.reminders.size)
            assertEquals(3, updatedExpense.reminders.first().daysBeforePayment)
            assertNotNull(updatedExpense.reminders.first().lastNotificationDate)
        }

    @Test
    fun `multiple expenses create separate notifications`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository) { testCurrentTime }

            val expense1 =
                createTestExpense(
                    id = 1,
                    name = "Netflix",
                    firstPayment = LocalDateTime(2025, 10, 15, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                )
            val expense2 =
                createTestExpense(
                    id = 2,
                    name = "Spotify",
                    firstPayment = LocalDateTime(2025, 10, 16, 0, 0, 0, 0).toInstant(TimeZone.UTC),
                )
            repository.insert(expense1)
            repository.insert(expense2)

            val notifications = manager.getExpenseNotifications()
            assertEquals(2, notifications.size)
            assertTrue(notifications.any { it.id == 1 && it.title == "Netflix" })
            assertTrue(notifications.any { it.id == 2 && it.title == "Spotify" })
        }
}
