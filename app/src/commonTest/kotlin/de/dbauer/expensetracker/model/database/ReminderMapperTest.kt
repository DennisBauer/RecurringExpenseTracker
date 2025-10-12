package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.Reminder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class ReminderMapperTest {
    @Test
    fun `toReminder maps ReminderEntry to Reminder correctly`() {
        val reminderEntry =
            ReminderEntry(
                id = 1,
                expenseId = 100,
                daysBeforePayment = 3,
                lastNotificationDate = 1234567890000L,
            )

        val reminder = reminderEntry.toReminder()

        assertEquals(1, reminder.id)
        assertEquals(3, reminder.daysBeforePayment)
        assertEquals(Instant.fromEpochMilliseconds(1234567890000L), reminder.lastNotificationDate)
    }

    @Test
    fun `toReminder maps ReminderEntry with null lastNotificationDate`() {
        val reminderEntry =
            ReminderEntry(
                id = 2,
                expenseId = 200,
                daysBeforePayment = 7,
                lastNotificationDate = null,
            )

        val reminder = reminderEntry.toReminder()

        assertEquals(2, reminder.id)
        assertEquals(7, reminder.daysBeforePayment)
        assertNull(reminder.lastNotificationDate)
    }

    @Test
    fun `toReminderEntry maps Reminder to ReminderEntry correctly`() {
        val reminder =
            Reminder(
                id = 5,
                daysBeforePayment = 14,
                lastNotificationDate = Instant.fromEpochMilliseconds(9876543210000L),
            )
        val expenseId = 300

        val reminderEntry = reminder.toReminderEntry(expenseId)

        assertEquals(5, reminderEntry.id)
        assertEquals(300, reminderEntry.expenseId)
        assertEquals(14, reminderEntry.daysBeforePayment)
        assertEquals(9876543210000L, reminderEntry.lastNotificationDate)
    }

    @Test
    fun `toReminderEntry maps Reminder with null lastNotificationDate`() {
        val reminder =
            Reminder(
                id = 0,
                daysBeforePayment = 1,
                lastNotificationDate = null,
            )
        val expenseId = 400

        val reminderEntry = reminder.toReminderEntry(expenseId)

        assertEquals(0, reminderEntry.id)
        assertEquals(400, reminderEntry.expenseId)
        assertEquals(1, reminderEntry.daysBeforePayment)
        assertNull(reminderEntry.lastNotificationDate)
    }

    @Test
    fun `toReminders maps list of ReminderEntry to list of Reminder`() {
        val reminderEntries =
            listOf(
                ReminderEntry(1, 100, 3, 1000000L),
                ReminderEntry(2, 100, 7, null),
                ReminderEntry(3, 100, 14, 2000000L),
            )

        val reminders = reminderEntries.toReminders()

        assertEquals(3, reminders.size)
        assertEquals(1, reminders[0].id)
        assertEquals(3, reminders[0].daysBeforePayment)
        assertEquals(2, reminders[1].id)
        assertEquals(7, reminders[1].daysBeforePayment)
        assertNull(reminders[1].lastNotificationDate)
        assertEquals(3, reminders[2].id)
        assertEquals(14, reminders[2].daysBeforePayment)
    }

    @Test
    fun `toReminders handles empty list`() {
        val reminderEntries = emptyList<ReminderEntry>()

        val reminders = reminderEntries.toReminders()

        assertEquals(0, reminders.size)
    }
}
