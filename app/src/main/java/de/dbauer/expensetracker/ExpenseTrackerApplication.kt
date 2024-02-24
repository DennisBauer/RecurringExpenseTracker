package de.dbauer.expensetracker

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import de.dbauer.expensetracker.viewmodel.database.ExpenseRepository
import de.dbauer.expensetracker.viewmodel.database.RecurringExpenseDatabase
import de.dbauer.expensetracker.viewmodel.database.UserPreferencesRepository

class ExpenseTrackerApplication : Application() {
    private val database by lazy { RecurringExpenseDatabase.getDatabase(this) }
    val repository by lazy { ExpenseRepository(database.recurringExpenseDao()) }
    private val Context.dataStore by preferencesDataStore(name = "user_preferences")
    val userPreferencesRepository by lazy { UserPreferencesRepository(dataStore) }
}
