package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.Tag
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
            recurringExpense.tags.forEach {
                recurringExpenseDao.upsert(ExpenseTagCrossRefEntry(recurringExpense.id, it.id))
            }
            recurringExpense.reminders.forEach { reminder ->
                recurringExpenseDao.insertReminder(reminder.toReminderEntry(recurringExpense.id))
            }
        }

    override suspend fun update(recurringExpense: RecurringExpenseData) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(recurringExpense.toEntryRecurringExpense(getDefaultCurrencyCode()))

            // Get current tags for the expense
            val currentTags = recurringExpenseDao.getExpenseById(recurringExpense.id)?.tags ?: emptyList()
            val currentTagIds = currentTags.map { it.id }.toSet()
            val newTagIds = recurringExpense.tags.map { it.id }.toSet()

            // Tags to add
            recurringExpense.tags.filterNot { it.id in currentTagIds }.forEach { newTag ->
                recurringExpenseDao.upsert(ExpenseTagCrossRefEntry(recurringExpense.id, newTag.id))
            }
            // Tags to remove
            currentTags.filterNot { it.id in newTagIds }.forEach { oldTag ->
                recurringExpenseDao.delete(ExpenseTagCrossRefEntry(recurringExpense.id, oldTag.id))
            }

            // Handle reminders
            val currentReminders = recurringExpenseDao.getRemindersForExpense(recurringExpense.id)
            val currentReminderIds = currentReminders.map { it.id }.toSet()
            val newReminderIds = recurringExpense.reminders.map { it.id }.toSet()

            // Reminders to add
            recurringExpense.reminders.filter { it.id == 0 }.forEach { newReminder ->
                recurringExpenseDao.insertReminder(newReminder.toReminderEntry(recurringExpense.id))
            }
            // Reminders to update
            recurringExpense.reminders.filter { it.id != 0 && it.id in currentReminderIds }.forEach { reminder ->
                recurringExpenseDao.updateReminder(reminder.toReminderEntry(recurringExpense.id))
            }
            // Reminders to remove
            currentReminders.filterNot { it.id in newReminderIds }.forEach { oldReminder ->
                recurringExpenseDao.deleteReminder(oldReminder)
            }
        }

    override suspend fun delete(recurringExpense: RecurringExpenseData) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(recurringExpense.toEntryRecurringExpense(getDefaultCurrencyCode()))
            recurringExpenseDao.deleteAllCrossRefForExpenseId(recurringExpense.id)
            recurringExpenseDao.deleteAllRemindersForExpenseId(recurringExpense.id)
        }

    override suspend fun insert(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.insert(tag.toTagEntry())
        }

    override suspend fun update(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.update(tag.toTagEntry())
        }

    override suspend fun delete(tag: Tag) =
        withContext(Dispatchers.IO) {
            recurringExpenseDao.delete(tag.toTagEntry())
            recurringExpenseDao.deleteAllCrossRefForTagId(tag.id)
        }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
