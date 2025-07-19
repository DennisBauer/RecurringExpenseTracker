package de.dbauer.expensetracker.model.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val recurringExpenseDao: RecurringExpenseDao,
) : IExpenseRepository {
    override val allRecurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAll()
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpense>> =
        recurringExpenseDao.getAllByPrice()

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpense? =
        withContext(Dispatchers.IO) {
            return@withContext recurringExpenseDao.getById(id)
        }

    override suspend fun insert(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.insert(recurringExpense)
        }

    override suspend fun update(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(recurringExpense)
        }

    override suspend fun delete(recurringExpense: RecurringExpense) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(recurringExpense)
        }
}
