package de.dbauer.expensetracker.di

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import de.dbauer.expensetracker.model.notification.SystemNotificationBuilder
import de.dbauer.expensetracker.widget.UpcomingPaymentsWidgetModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val appModule = module {
    singleOf(::UpcomingPaymentsWidgetModel)
    factory<AlarmManager> {
        val context = get<Context>()
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }
    factory<SystemNotificationBuilder> {
        SystemNotificationBuilder(get<Context>(), get<NotificationManager>())
    }
    factory<NotificationManager> {
        val context = get<Context>()
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }
}
