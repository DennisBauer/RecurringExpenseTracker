package model.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import kotlin.apply
import kotlin.time.Duration

const val ACTION_START_ALARM_LOOPER = "alarm.loopers.START"

fun Context.startAlarmLooper(loopReceiverClass: Class<out AlarmLoopReceiver>) {
    sendBroadcast(
        Intent(this, loopReceiverClass).apply {
            action = ACTION_START_ALARM_LOOPER
        },
    )
}

abstract class AlarmLoopReceiver : BroadcastReceiver() {
    abstract val loopPeriod: Duration

    abstract val loopAction: String

    abstract val alarmManager: AlarmManager

    @CallSuper
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val isStartAction =
            intent.action == ACTION_START_ALARM_LOOPER || intent.action == Intent.ACTION_BOOT_COMPLETED
        val isLoopStarted = getLoopIntent(context, true) != null

        if (isStartAction && !isLoopStarted) {
            loop(context)
        }
    }

    protected fun loop(context: Context) {
        val intent = getLoopIntent(context, false) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            loopInternalApi31(intent)
        } else {
            loopInternal(intent)
        }
    }

    protected fun stop(context: Context) {
        getLoopIntent(context, false)?.let { loopIntent ->
            alarmManager.cancel(loopIntent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loopInternal(intent: PendingIntent) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + loopPeriod.inWholeMilliseconds,
            intent,
        )
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun loopInternalApi31(intent: PendingIntent) {
        if (alarmManager.canScheduleExactAlarms()) {
            loopInternal(intent)
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + loopPeriod.inWholeMilliseconds,
                intent,
            )
        }
    }

    private fun getLoopIntent(
        context: Context,
        noCreate: Boolean,
    ): PendingIntent? =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(context, this::class.java).apply { action = loopAction },
            if (noCreate) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            } else {
                PendingIntent.FLAG_IMMUTABLE
            },
        )
}
