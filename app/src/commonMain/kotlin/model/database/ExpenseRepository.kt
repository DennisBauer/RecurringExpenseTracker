package model.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val recurringExpenseDao: RecurringExpenseDao,
) {
    val allRecurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAll()
    val allRecurringExpensesByPrice: Flow<List<RecurringExpense>> =
        recurringExpenseDao.getAllByPrice()

    suspend fun getRecurringExpenseById(id: Int): RecurringExpense? =
        withContext(Dispatchers.IO) {
            return@withContext recurringExpenseDao.getById(id)
        }

    suspend fun insert(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.insert(recurringExpense)
        }

    suspend fun update(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(recurringExpense)
        }

    suspend fun delete(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(recurringExpense)
        }
}
