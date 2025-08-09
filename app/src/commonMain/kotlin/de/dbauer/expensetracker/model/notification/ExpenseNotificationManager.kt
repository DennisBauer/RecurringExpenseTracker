package de.dbauer.expensetracker.model.notification

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

class ExpenseNotificationManager(
    private val expenseRepository: IExpenseRepository,
    private val userPreferencesRepository: IUserPreferencesRepository,
) {
    suspend fun getExpenseNotifications(): List<NotificationData> {
        val notifications = mutableListOf<NotificationData>()
        val expenses = expenseRepository.allRecurringExpenses.first()
        expenses.forEach { expense ->
            if (expense.notifyForExpense) {
                expense.getNextPaymentDay()?.let { nextPaymentDay ->
                    val daysToNextPayment = DateTimeCalculator.getDaysFromNowUntil(nextPaymentDay)
                    val notifyXDaysBefore =
                        expense.notifyXDaysBefore
                            ?: userPreferencesRepository.upcomingPaymentNotificationDaysAdvance.get().first()
                    val notifyForExpense =
                        expense.lastNotificationDate
                            ?.let { lastDateNotifiedForInstant ->
                                val nextPaymentInstant = nextPaymentDay.atStartOfDayIn(TimeZone.UTC)
                                lastDateNotifiedForInstant.daysUntil(nextPaymentInstant, TimeZone.UTC) > 0
                            } ?: true
                    if (notifyForExpense && daysToNextPayment <= notifyXDaysBefore) {
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
        return notifications
    }

    suspend fun markNotificationAsShown(id: Int) {
        expenseRepository.getRecurringExpenseById(id)?.let { expense ->
            expense.getNextPaymentDay()?.let { test ->
                expenseRepository.update(
                    expense.copy(
                        lastNotificationDate = expense.getNextPaymentDay()?.atStartOfDayIn(TimeZone.UTC),
                    ),
                )
            }
        }
    }

    private suspend fun getNotificationDescription(daysToNextPayment: Int): String {
        return when (daysToNextPayment) {
            0 -> getString(Res.string.notification_expense_reminder_message_today)
            1 -> getString(Res.string.notification_expense_reminder_message_tomorrow)
            else -> getString(Res.string.notification_expense_reminder_message_days, daysToNextPayment)
        }
    }
}
