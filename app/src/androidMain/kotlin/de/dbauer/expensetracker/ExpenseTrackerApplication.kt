package de.dbauer.expensetracker

import android.app.Application
import di.platformModule
import di.sharedModule
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
    }
}
