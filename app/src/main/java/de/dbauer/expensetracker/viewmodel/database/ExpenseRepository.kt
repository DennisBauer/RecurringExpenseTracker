package de.dbauer.expensetracker.viewmodel.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val recurringExpenseDao: RecurringExpenseDao,
) {
    val allRecurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAll()
    val allRecurringExpensesByPrice: Flow<List<RecurringExpense>> =
        recurringExpenseDao.getAllByPrice()

    @WorkerThread
    suspend fun getRecurringExpenseById(id: Int): RecurringExpense? =
        withContext(Dispatchers.IO) {
            return@withContext recurringExpenseDao.getById(id)
        }

    @WorkerThread
    suspend fun insert(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.insert(recurringExpense)
        }

    @WorkerThread
    suspend fun update(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(recurringExpense)
        }

    @WorkerThread
    suspend fun delete(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(recurringExpense)
        }
}
