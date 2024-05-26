package de.dbauer.expensetracker

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import viewmodel.database.ExpenseRepository
import viewmodel.database.RecurringExpenseDatabase
import viewmodel.database.UserPreferencesRepository
import viewmodel.database.getDatabaseBuilder

class ExpenseTrackerApplication : Application() {
    private val database by lazy { RecurringExpenseDatabase.getRecurringExpenseDatabase(getDatabaseBuilder(this)) }
    val repository by lazy { ExpenseRepository(database.recurringExpenseDao()) }

    private val Context.dataStore by preferencesDataStore(name = USER_PREFERENCES_DATA_STORE)
    val userPreferencesRepository by lazy { UserPreferencesRepository(dataStore = dataStore) }

    private companion object {
        private const val USER_PREFERENCES_DATA_STORE = "USER_PREFERENCES_DATA_STORE"
    }
}
