package de.dbauer.expensetracker.shared.model.notification

import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.Recurrence
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Reminder
import de.dbauer.expensetracker.shared.model.database.FakeExpenseRepository
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.FakeUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant

class ExpenseNotificationManagerTest {
    private class TestableExpenseNotificationManager(
        expenseRepository: IExpenseRepository,
        userPreferencesRepository: IUserPreferencesRepository,
    ) : ExpenseNotificationManager(expenseRepository, userPreferencesRepository) {
        // Override to provide test descriptions without needing resources
        override suspend fun getNotificationDescription(daysToNextPayment: Int): String {
            return when {
                daysToNextPayment < 0 -> "${-daysToNextPayment} days overdue"
                daysToNextPayment == 0 -> "Due today"
                daysToNextPayment == 1 -> "Due tomorrow"
                else -> "Due in $daysToNextPayment days"
            }
        }

        // Public wrapper for testing getNotificationDescription directly
        suspend fun testGetNotificationDescription(daysToNextPayment: Int): String {
            return getNotificationDescription(daysToNextPayment)
        }
    }

    // Helper to get today's date
    private fun getTodayLocalDate(): LocalDate {
        return Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
    }

    // Helper to create a date offset from today
    private fun getDateOffsetFromToday(days: Int): LocalDate {
        val today = getTodayLocalDate()
        return if (days >= 0) {
            today.plus(days, DateTimeUnit.DAY)
        } else {
            today.minus(-days, DateTimeUnit.DAY)
        }
    }

    private fun createTestExpense(
        id: Int,
        name: String = "Test Expense",
        notifyForExpense: Boolean = true,
        firstPayment: Instant? = null,
        reminders: List<Reminder> = emptyList(),
        recurrence: Recurrence = Recurrence.Monthly,
        requireManualConfirmation: Boolean = false,
    ): RecurringExpenseData {
        return RecurringExpenseData(
            id = id,
            name = name,
            description = "",
            price = CurrencyValue(10f, "EUR"),
            monthlyPrice = CurrencyValue(10f, "EUR"),
            everyXRecurrence = 1,
            recurrence = recurrence,
            tags = emptyList(),
            firstPayment = firstPayment,
            notifyForExpense = notifyForExpense,
            reminders = reminders,
            requireManualConfirmation = requireManualConfirmation,
        )
    }

    @Test
    fun `expense with notifications disabled returns no notifications`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // Set first payment to 2 days from now
            val firstPaymentDate = getDateOffsetFromToday(2).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    notifyForExpense = false,
                    firstPayment = firstPaymentDate,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // Create expense with payment 2 days from now (within 3-day window)
            // Use daily recurrence and set first payment to 2 days from now
            val firstPaymentDate = getDateOffsetFromToday(2).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Netflix Subscription",
                    firstPayment = firstPaymentDate,
                    recurrence = Recurrence.Daily, // Use daily to ensure next payment is exactly where we want
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertEquals(1, notifications.size)
            assertEquals(1, notifications[0].id)
            assertEquals("Netflix Subscription", notifications[0].title)
            assertEquals(NotificationChannel.ExpenseReminder, notifications[0].channel)
        }

    @Test
    fun `expense with default reminder outside notification window returns no notifications`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // Create expense with payment 10 days from now (outside 3-day window)
            val firstPaymentDate = getDateOffsetFromToday(10).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = firstPaymentDate,
                    recurrence = Recurrence.Daily,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 5,
                    lastNotificationDate = null,
                )

            // Create expense with payment 5 days from now (exactly at 5-day reminder window)
            val firstPaymentDate = getDateOffsetFromToday(5).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Rent Payment",
                    firstPayment = firstPaymentDate,
                    reminders = listOf(reminder),
                    recurrence = Recurrence.Daily,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val reminders =
                listOf(
                    Reminder(id = 1, daysBeforePayment = 7),
                    Reminder(id = 2, daysBeforePayment = 3),
                    Reminder(id = 3, daysBeforePayment = 1),
                )

            // Create expense with payment 5 days from now (matches reminders with 7 and 3 days)
            val firstPaymentDate = getDateOffsetFromToday(5).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = firstPaymentDate,
                    reminders = reminders,
                    recurrence = Recurrence.Daily,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val nextPaymentDate = getDateOffsetFromToday(5).atStartOfDayIn(TimeZone.UTC)
            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 5,
                    lastNotificationDate = nextPaymentDate, // Already notified for this payment
                )

            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = nextPaymentDate,
                    reminders = listOf(reminder),
                    recurrence = Recurrence.Daily,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 5,
                    lastNotificationDate = null,
                )

            val firstPaymentDate = getDateOffsetFromToday(5).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = firstPaymentDate,
                    reminders = listOf(reminder),
                    recurrence = Recurrence.Daily,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val reminders =
                listOf(
                    Reminder(id = 1, daysBeforePayment = 7),
                    Reminder(id = 2, daysBeforePayment = 3),
                    Reminder(id = 3, daysBeforePayment = 1),
                )

            // Payment is 3 days away, so reminders with 7 and 3 days should be marked as shown
            val firstPaymentDate = getDateOffsetFromToday(3).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = firstPaymentDate,
                    reminders = reminders,
                    recurrence = Recurrence.Daily,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val firstPaymentDate = getDateOffsetFromToday(3).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    firstPayment = firstPaymentDate,
                    reminders = emptyList(), // No custom reminders
                    recurrence = Recurrence.Daily,
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
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val firstPaymentDate1 = getDateOffsetFromToday(2).atStartOfDayIn(TimeZone.UTC)
            val firstPaymentDate2 = getDateOffsetFromToday(3).atStartOfDayIn(TimeZone.UTC)

            val expense1 =
                createTestExpense(
                    id = 1,
                    name = "Netflix",
                    firstPayment = firstPaymentDate1, // 2 days away
                    recurrence = Recurrence.Daily,
                )
            val expense2 =
                createTestExpense(
                    id = 2,
                    name = "Spotify",
                    firstPayment = firstPaymentDate2, // 3 days away
                    recurrence = Recurrence.Daily,
                )
            repository.insert(expense1)
            repository.insert(expense2)

            val notifications = manager.getExpenseNotifications()
            assertEquals(2, notifications.size)
            assertTrue(notifications.any { it.id == 1 && it.title == "Netflix" })
            assertTrue(notifications.any { it.id == 2 && it.title == "Spotify" })
        }

    @Test
    fun `manual confirmation expense due today and unpaid with default reminder creates notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // First payment is today, so daysToNextPayment == 0 (within default 3-day window)
            val firstPaymentDate = getTodayLocalDate().atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Insurance",
                    firstPayment = firstPaymentDate,
                    recurrence = Recurrence.Monthly,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertEquals(1, notifications.size)
            assertEquals("Insurance", notifications[0].title)
            assertEquals("Due today", notifications[0].description)
            assertEquals(NotificationChannel.ExpenseReminder, notifications[0].channel)
        }

    @Test
    fun `manual confirmation expense due today and already paid does not create notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val firstPaymentDate = getTodayLocalDate().atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Insurance",
                    firstPayment = firstPaymentDate,
                    recurrence = Recurrence.Monthly,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            // Mark the payment as paid
            val paymentDateEpoch = firstPaymentDate.toEpochMilliseconds()
            repository.markAsPaid(1, paymentDateEpoch)

            val notifications = manager.getExpenseNotifications()
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `manual confirmation expense not yet due does not create overdue notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // First payment is 2 days from now — within default notification window but not overdue
            val firstPaymentDate = getDateOffsetFromToday(2).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Insurance",
                    firstPayment = firstPaymentDate,
                    recurrence = Recurrence.Daily,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            // Should still get a notification from the default reminder, not the overdue path
            assertEquals(1, notifications.size)
            assertEquals("Due in 2 days", notifications[0].description)
        }

    @Test
    fun `manual confirmation expense due today with custom reminder creates one notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // Expense due today with manual confirmation and custom reminder
            val firstPaymentDate = getTodayLocalDate().atStartOfDayIn(TimeZone.UTC)
            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 3,
                    lastNotificationDate = null,
                )
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Rent",
                    firstPayment = firstPaymentDate,
                    reminders = listOf(reminder),
                    recurrence = Recurrence.Monthly,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            assertEquals(1, notifications.size)
            assertEquals("Rent", notifications[0].title)
            assertEquals("Due today", notifications[0].description)
        }

    @Test
    fun `manual confirmation expense due today with already shown reminder does not duplicate notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val firstPaymentDate = getTodayLocalDate().atStartOfDayIn(TimeZone.UTC)
            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 3,
                    lastNotificationDate = firstPaymentDate, // Already notified for this payment
                )
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Rent",
                    firstPayment = firstPaymentDate,
                    reminders = listOf(reminder),
                    recurrence = Recurrence.Monthly,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            // Reminder already shown for this payment — should not notify again
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `manual confirmation expense due today without reminders and outside default window creates no notif`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            // Set default advance to -1 so daysToNextPayment (0) > defaultDaysAdvance (-1)
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(-1)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val firstPaymentDate = getTodayLocalDate().atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Insurance",
                    firstPayment = firstPaymentDate,
                    recurrence = Recurrence.Monthly,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            val notifications = manager.getExpenseNotifications()
            // No custom reminders and outside default window — no notification
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `overdue description returns correct text for various day values`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // daysToNextPayment < 0 => overdue
            assertEquals("1 days overdue", manager.testGetNotificationDescription(-1))
            assertEquals("5 days overdue", manager.testGetNotificationDescription(-5))

            // daysToNextPayment == 0 => due today
            assertEquals("Due today", manager.testGetNotificationDescription(0))

            // daysToNextPayment == 1 => due tomorrow
            assertEquals("Due tomorrow", manager.testGetNotificationDescription(1))

            // daysToNextPayment > 1 => due in N days
            assertEquals("Due in 7 days", manager.testGetNotificationDescription(7))
        }

    @Test
    fun `upcoming manual confirmation expense already paid does not create notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            prefsRepository.upcomingPaymentNotificationDaysAdvance.save(3)
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            // Payment is 2 days from now — within default 3-day notification window
            val firstPaymentDate = getDateOffsetFromToday(2).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Insurance",
                    firstPayment = firstPaymentDate,
                    recurrence = Recurrence.Daily,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            // Mark the upcoming payment as paid in advance
            val paymentDateEpoch = getDateOffsetFromToday(2).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            repository.markAsPaid(1, paymentDateEpoch)

            val notifications = manager.getExpenseNotifications()
            // Already paid — should not notify even though within reminder window
            assertTrue(notifications.isEmpty())
        }

    @Test
    fun `upcoming manual confirmation expense with custom reminder already paid does not create notification`() =
        runTest {
            val repository = FakeExpenseRepository()
            val prefsRepository = FakeUserPreferencesRepository()
            val manager = TestableExpenseNotificationManager(repository, prefsRepository)

            val reminder =
                Reminder(
                    id = 1,
                    daysBeforePayment = 5,
                    lastNotificationDate = null,
                )

            // Payment is 3 days from now — within 5-day custom reminder window
            val firstPaymentDate = getDateOffsetFromToday(3).atStartOfDayIn(TimeZone.UTC)
            val expense =
                createTestExpense(
                    id = 1,
                    name = "Rent",
                    firstPayment = firstPaymentDate,
                    reminders = listOf(reminder),
                    recurrence = Recurrence.Daily,
                    requireManualConfirmation = true,
                )
            repository.insert(expense)

            // Mark the upcoming payment as paid in advance
            val paymentDateEpoch = getDateOffsetFromToday(3).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
            repository.markAsPaid(1, paymentDateEpoch)

            val notifications = manager.getExpenseNotifications()
            // Already paid — should not notify even though custom reminder matches
            assertTrue(notifications.isEmpty())
        }
}
