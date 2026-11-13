package de.dbauer.expensetracker.model.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

const val ACTION_START_ALARM_LOOPER = "alarm.loopers.START"

fun Context.startAlarmLooper(loopReceiverClass: Class<out AlarmLoopReceiver>) {
    sendBroadcast(
        Intent(this, loopReceiverClass).apply {
            action = ACTION_START_ALARM_LOOPER
        },
    )
}

abstract class AlarmLoopReceiver : BroadcastReceiver() {
    abstract val loopAction: String

    abstract val alarmManager: AlarmManager

    /**
     * Schedules the next loop alarm at [triggerAtMillis] (wall clock time), replacing any pending one.
     */
    protected fun loop(
        context: Context,
        triggerAtMillis: Long,
    ) {
        val intent = getLoopIntent(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            loopInternalApi31(intent, triggerAtMillis)
        } else {
            loopInternal(intent, triggerAtMillis)
        }
    }

    protected fun stop(context: Context) {
        alarmManager.cancel(getLoopIntent(context))
    }

    @SuppressLint("MissingPermission")
    private fun loopInternal(
        intent: PendingIntent,
        triggerAtMillis: Long,
    ) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun loopInternalApi31(
        intent: PendingIntent,
        triggerAtMillis: Long,
    ) {
        if (alarmManager.canScheduleExactAlarms()) {
            loopInternal(intent, triggerAtMillis)
        } else {
            // Inexact one-shot alarms are delivered at most one hour after the trigger time and, unlike
            // setWindow(), still fire while the device is in Doze.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, intent)
        }
    }

    private fun getLoopIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, this::class.java).apply { action = loopAction },
            PendingIntent.FLAG_IMMUTABLE,
        )
}
