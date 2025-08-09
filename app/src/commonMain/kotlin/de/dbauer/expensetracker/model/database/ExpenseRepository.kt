package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.model.database.EntryTag.Companion.toEntryTag
import de.dbauer.expensetracker.model.database.EntryTag.Companion.toTags
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.model.getSystemCurrencyCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val recurringExpenseDao: RecurringExpenseDao,
    userPreferencesRepository: IUserPreferencesRepository,
) : IExpenseRepository {
    override val allRecurringExpenses: Flow<List<RecurringExpenseData>> =
        recurringExpenseDao.getAllExpenses().map { expenses ->
            expenses.map { it.toRecurringExpenseData(getDefaultCurrencyCode()) }
        }
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpenseData>> =
        recurringExpenseDao.getAllExpensesByPrice().map { expenses ->
            expenses.map { it.toRecurringExpenseData(getDefaultCurrencyCode()) }
        }
    override val allTags: Flow<List<Tag>> =
        recurringExpenseDao.getAllTags().map { tags -> tags.toTags() }

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData? =
        withContext(Dispatchers.IO) {
            return@withContext recurringExpenseDao
                .getExpenseById(id)
                ?.toRecurringExpenseData(getDefaultCurrencyCode())
        }

    override suspend fun insert(recurringExpense: RecurringExpenseData) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.insert(recurringExpense.toEntryRecurringExpense(getDefaultCurrencyCode()))
        }

    override suspend fun update(recurringExpense: RecurringExpenseData) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(recurringExpense.toEntryRecurringExpense(getDefaultCurrencyCode()))
        }

    override suspend fun delete(recurringExpense: RecurringExpenseData) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(recurringExpense.toEntryRecurringExpense(getDefaultCurrencyCode()))
        }

    override suspend fun insert(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.insert(tag.toEntryTag())
        }

    override suspend fun update(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(tag.toEntryTag())
        }

    override suspend fun delete(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(tag.toEntryTag())
        }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
