package de.erzock.expensetracker

import android.app.Application
import de.erzock.expensetracker.viewmodel.database.ExpenseRepository
import de.erzock.expensetracker.viewmodel.database.RecurringExpenseDatabase

class ExpenseTrackerApplication : Application() {
    private val database by lazy { RecurringExpenseDatabase.getDatabase(this) }
    val repository by lazy { ExpenseRepository(database.recurringExpenseDao()) }
}