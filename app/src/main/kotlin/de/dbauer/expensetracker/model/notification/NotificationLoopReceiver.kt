package de.dbauer.expensetracker.model.notification

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import de.dbauer.expensetracker.shared.data.HomePane
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.notification.ExpenseNotificationManager
import de.dbauer.expensetracker.shared.model.notification.NotificationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.koin.java.KoinJavaComponent.inject
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.notification_auto_archived_title
import kotlin.time.Clock
import de.dbauer.expensetracker.shared.model.notification.NotificationChannel as SharedNotificationChannel

internal const val ACTION_NOTIFICATION_LOOP_ACTION = "notification_loop_action"

internal class NotificationLoopReceiver : AlarmLoopReceiver() {
    override val alarmManager: AlarmManager by inject(AlarmManager::class.java)

    private val expenseNotificationManager: ExpenseNotificationManager by inject(
        ExpenseNotificationManager::class.java,
    )
    private val systemNotificationBuilder: SystemNotificationBuilder by inject(
        SystemNotificationBuilder::class.java,
    )
    private val notificationManager: NotificationManager by inject(NotificationManager::class.java)
    private val userPreferencesRepository: IUserPreferencesRepository by inject(
        IUserPreferencesRepository::class.java,
    )
    private val expenseRepository: IExpenseRepository by inject(
        IExpenseRepository::class.java,
    )

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val loopAction: String
        get() = ACTION_NOTIFICATION_LOOP_ACTION

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.action
        if (action != ACTION_START_ALARM_LOOPER &&
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != ACTION_NOTIFICATION_LOOP_ACTION
        ) {
            return
        }
        val pendingResult = goAsync()

        coroutineScope.launch {
            try {
                // Re-arm first so a failure further down cannot break the daily loop
                val notificationsEnabled = scheduleNextLoop(context)

                val autoArchivedCandidates =
                    expenseRepository.autoArchiveExpired(Clock.System.now().toEpochMilliseconds())
                autoArchivedCandidates.forEach { candidate ->
                    showNotification(
                        NotificationData(
                            id = -candidate.id - 1,
                            title = getString(Res.string.notification_auto_archived_title),
                            description = candidate.name.orEmpty(),
                            channel = SharedNotificationChannel.ExpenseReminder,
                            startRoute = HomePane(showArchived = true),
                        ),
                    )
                }

                if (notificationsEnabled && action != Intent.ACTION_BOOT_COMPLETED) {
                    expenseNotificationManager.getExpenseNotifications().forEach {
                        showNotification(it)
                    }
                }
            } finally {
                pendingResult.finish()
                coroutineScope.cancel()
            }
        }
    }

    /**
     * Schedules the loop alarm at the next occurrence of the configured notification time, or cancels it if
     * notifications are disabled.
     *
     * @return whether upcoming payment notifications are enabled
     */
    private suspend fun scheduleNextLoop(context: Context): Boolean {
        val shouldShowNotifications = userPreferencesRepository.upcomingPaymentNotification.get().first()
        val schedulingTimeInt = userPreferencesRepository.upcomingPaymentNotificationTime.get().first()
        if (!shouldShowNotifications || schedulingTimeInt < 0) {
            stop(context)
            return false
        }

        val timeZone = TimeZone.currentSystemDefault()
        val scheduleLocalTime = LocalTime(schedulingTimeInt / 60, schedulingTimeInt % 60)
        val now = Clock.System.now()
        val today = now.toLocalDateTime(timeZone).date
        var scheduleTime = LocalDateTime(today, scheduleLocalTime).toInstant(timeZone)
        if (scheduleTime <= now) {
            scheduleTime = LocalDateTime(today.plus(1, DateTimeUnit.DAY), scheduleLocalTime).toInstant(timeZone)
        }
        loop(context, scheduleTime.toEpochMilliseconds())
        return true
    }

    /**
     * Show notification if it does not exist already
     */
    private suspend fun showNotification(data: NotificationData) {
        if (notificationManager.activeNotifications.firstOrNull { it.id == data.id } == null) {
            val notification = systemNotificationBuilder.buildSystemNotification(data)
            notificationManager.notify(data.id, notification)
        }
    }
}
