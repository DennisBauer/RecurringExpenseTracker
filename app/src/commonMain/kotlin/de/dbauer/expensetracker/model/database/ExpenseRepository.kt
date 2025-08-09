package de.dbauer.expensetracker.model.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val recurringExpenseDao: RecurringExpenseDao,
) : IExpenseRepository {
    override val allRecurringExpenses: Flow<List<RecurringExpense>> = recurringExpenseDao.getAllExpenses()
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpense>> =
        recurringExpenseDao.getAllExpensesByPrice()
    override val allTags: Flow<List<Tag>> = recurringExpenseDao.getAllTags()

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpense? =
        withContext(Dispatchers.IO) {
            return@withContext recurringExpenseDao.getExpenseById(id)
        }

    override suspend fun getRecurringExpenseWithTagsById(id: Int): RecurringExpenseWithTags? =
        withContext(Dispatchers.IO) {
            return@withContext recurringExpenseDao.getExpenseWithTagsById(id)
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

    override suspend fun insert(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.insert(tag)
        }

    override suspend fun update(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(tag)
        }

    override suspend fun delete(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(tag)
        }
}
