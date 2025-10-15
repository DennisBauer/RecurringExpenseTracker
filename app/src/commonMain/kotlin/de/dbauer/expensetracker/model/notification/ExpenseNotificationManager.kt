package de.dbauer.expensetracker.model.notification

import de.dbauer.expensetracker.data.Reminder
import de.dbauer.expensetracker.model.DateTimeCalculator
import de.dbauer.expensetracker.model.database.IExpenseRepository
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.getString
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.notification_expense_reminder_message_days
import recurringexpensetracker.app.generated.resources.notification_expense_reminder_message_today
import recurringexpensetracker.app.generated.resources.notification_expense_reminder_message_tomorrow
import kotlin.time.Clock
import kotlin.time.Instant

open class ExpenseNotificationManager(
    private val expenseRepository: IExpenseRepository,
    private val userPreferencesRepository: IUserPreferencesRepository,
    private val currentTimeProvider: () -> Instant = { Clock.System.now() },
) {
    suspend fun getExpenseNotifications(): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        val expenses = expenseRepository.allRecurringExpenses.first()
        val defaultDaysAdvance = userPreferencesRepository.upcomingPaymentNotificationDaysAdvance.get().first()
        val currentDate = DateTimeCalculator.getCurrentLocalDate(currentTimeProvider())

        expenses.forEach { expense ->
            if (expense.notifyForExpense) {
                expense.getNextPaymentDay()?.let { nextPaymentDay ->
                    val daysToNextPayment = DateTimeCalculator.getDaysFromUntil(currentDate, nextPaymentDay)

                    // If expense has custom reminders, use them
                    if (expense.reminders.isNotEmpty()) {
                        expense.reminders.forEach { reminder ->
                            val shouldNotify =
                                reminder.lastNotificationDate
                                    ?.let { lastDateNotifiedForInstant ->
                                        val nextPaymentInstant = nextPaymentDay.atStartOfDayIn(TimeZone.UTC)
                                        lastDateNotifiedForInstant.daysUntil(nextPaymentInstant, TimeZone.UTC) > 0
                                    } ?: true

                            if (shouldNotify && daysToNextPayment <= reminder.daysBeforePayment) {
                                // Only add one notification per expense (first matching reminder prevents duplicates)
                                if (notifications.none { it.id == expense.id }) {
                                    notifications.add(
                                        NotificationData(
                                            id = expense.id,
                                            title = expense.name,
                                            description = getNotificationDescription(daysToNextPayment),
                                            channel = NotificationChannel.ExpenseReminder,
                                        ),
                                    )
                                }
                            }
                        }
                    } else {
                        if (daysToNextPayment <= defaultDaysAdvance) {
                            notifications.add(
                                NotificationData(
                                    id = expense.id,
                                    title = expense.name,
                                    description = getNotificationDescription(daysToNextPayment),
                                    channel = NotificationChannel.ExpenseReminder,
                                ),
                            )
                        }
                    }
                }
            }
        }
        return notifications
    }

    suspend fun markNotificationAsShown(id: Int) {
        expenseRepository.getRecurringExpenseById(id)?.let { expense ->
            expense.getNextPaymentDay()?.let { nextPaymentDay ->
                val nextPaymentInstant = nextPaymentDay.atStartOfDayIn(TimeZone.UTC)
                val currentDate = DateTimeCalculator.getCurrentLocalDate(currentTimeProvider())
                val daysToNextPayment = DateTimeCalculator.getDaysFromUntil(currentDate, nextPaymentDay)

                if (expense.reminders.isNotEmpty()) {
                    // Update ALL reminders that should have already triggered or are triggering now
                    val updatedReminders =
                        expense.reminders.map { reminder ->
                            // Mark as shown if the reminder's trigger point has passed or is now
                            if (reminder.daysBeforePayment >= daysToNextPayment) {
                                reminder.copy(lastNotificationDate = nextPaymentInstant)
                            } else {
                                reminder
                            }
                        }

                    expenseRepository.update(
                        expense.copy(reminders = updatedReminders),
                    )
                } else {
                    // No custom reminders - create a synthetic default reminder to track notification state
                    val defaultDaysAdvance =
                        userPreferencesRepository.upcomingPaymentNotificationDaysAdvance.get().first()
                    val defaultReminder =
                        Reminder(
                            id = 0,
                            daysBeforePayment = defaultDaysAdvance,
                            lastNotificationDate = nextPaymentInstant,
                        )
                    expenseRepository.update(
                        expense.copy(reminders = listOf(defaultReminder)),
                    )
                }
            }
        }
    }

    protected open suspend fun getNotificationDescription(daysToNextPayment: Int): String {
        return when (daysToNextPayment) {
            0 -> getString(Res.string.notification_expense_reminder_message_today)
            1 -> getString(Res.string.notification_expense_reminder_message_tomorrow)
            else -> getString(Res.string.notification_expense_reminder_message_days, daysToNextPayment)
        }
    }
}
