package de.dbauer.expensetracker

import android.app.Application
import android.os.Build
import androidx.glance.appwidget.GlanceAppWidgetManager
import de.dbauer.expensetracker.widget.UpcomingPaymentsWidgetReceiver
import di.platformModule
import di.sharedModule
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ExpenseTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ExpenseTrackerApplication)
            modules(
                sharedModule,
                platformModule,
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val mainScope = MainScope()
            mainScope
                .launch {
                    GlanceAppWidgetManager(
                        applicationContext,
                    ).setWidgetPreviews(UpcomingPaymentsWidgetReceiver::class)
                }.invokeOnCompletion {
                    mainScope.cancel()
                }
        }
    }
}
