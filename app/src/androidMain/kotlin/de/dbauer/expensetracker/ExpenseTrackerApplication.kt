package de.dbauer.expensetracker

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import di.platformModule
import di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import viewmodel.database.UserPreferencesRepository

class ExpenseTrackerApplication : Application() {
    private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_DATA_STORE)
    val userPreferencesRepository by lazy { UserPreferencesRepository(dataStore = dataStore) }

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

    private companion object {
        private const val USER_PREFERENCES_DATA_STORE = "USER_PREFERENCES_DATA_STORE"
    }
}
