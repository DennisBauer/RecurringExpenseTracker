package model.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.CATEGORY_ALARM
import androidx.core.app.NotificationCompat.PRIORITY_MAX
import de.dbauer.expensetracker.MainActivity
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.StartRoute
import org.jetbrains.compose.resources.getString

class SystemNotificationBuilder(
    private val context: Context,
    private val notificationManager: NotificationManager,
) {
    private val dismissExpenseNotificationReceiver = DismissExpenseNotificationReceiver()

    init {
        dismissExpenseNotificationReceiver.registerReceiver(context)
    }

    suspend fun buildSystemNotification(data: NotificationData): Notification {
        val channel = createChannel(data.channel)
        notificationManager.createNotificationChannel(channel)

        val launchIntent =
            PendingIntent.getActivity(
                context,
                data.id,
                MainActivity.newInstance(context, data.id, StartRoute.Upcoming),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
            )
        val dismissIntent =
            PendingIntent.getBroadcast(
                context,
                data.id,
                DismissExpenseNotificationReceiver.createIntent(context, data.id),
                PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat
            .Builder(context, data.channel.id)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setAutoCancel(true)
            .setContentText(data.description)
            .setContentTitle(data.title)
            .setPriority(PRIORITY_MAX)
            .setCategory(CATEGORY_ALARM)
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .setContentIntent(launchIntent)
            .setDeleteIntent(dismissIntent)
            .build()
    }

    private suspend fun createChannel(channel: model.notification.NotificationChannel): NotificationChannel {
        return NotificationChannel(
            channel.id,
            getString(channel.displayNameRes),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
    }
}
