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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import org.koin.java.KoinJavaComponent.inject
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.notification_auto_archived_title
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
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

    private var _loopPeriod = 1.days
    override val loopPeriod: Duration
        get() = _loopPeriod

    override val loopAction: String
        get() = ACTION_NOTIFICATION_LOOP_ACTION

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        super.onReceive(context, intent)
        val pendingResult = goAsync()

        coroutineScope.launch {
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
            when (intent.action) {
                ACTION_START_ALARM_LOOPER -> {
                    stop(context)
                    val shouldShowNotifications =
                        userPreferencesRepository.upcomingPaymentNotification.get().first()
                    if (shouldShowNotifications) {
                        val schedulingTimeInt =
                            userPreferencesRepository.upcomingPaymentNotificationTime.get().first()
                        if (schedulingTimeInt >= 0) {
                            val scheduleLocalTime = LocalTime(schedulingTimeInt / 60, schedulingTimeInt % 60)
                            val now = Clock.System.now()
                            var scheduleTime =
                                LocalDateTime(
                                    now.toLocalDateTime(TimeZone.currentSystemDefault()).date,
                                    scheduleLocalTime,
                                ).toInstant(TimeZone.currentSystemDefault())
                            if (scheduleTime.minus(now).isNegative()) {
                                scheduleTime = scheduleTime.plus(1.days)
                            }
                            _loopPeriod = scheduleTime.minus(now)

                            expenseNotificationManager.getExpenseNotifications().forEach {
                                showNotification(it)
                            }
                            loop(context)
                        }
                    }
                }

                ACTION_NOTIFICATION_LOOP_ACTION -> {
                    _loopPeriod = 1.days

                    expenseNotificationManager.getExpenseNotifications().forEach {
                        showNotification(it)
                    }
                    loop(context)
                }
            }

            cancel()
            pendingResult.finish()
        }
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
