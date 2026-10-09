package de.dbauer.expensetracker.shared.model.database

import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Tag
import de.dbauer.expensetracker.shared.ioDispatcher
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.getSystemCurrencyCode
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
    override val allArchivedRecurringExpenses: Flow<List<RecurringExpenseData>> =
        recurringExpenseDao.getAllArchivedExpenses().map { expenses ->
            expenses.map { it.toRecurringExpenseData(getDefaultCurrencyCode()) }
        }
    override val allArchivedRecurringExpensesByPrice: Flow<List<RecurringExpenseData>> =
        recurringExpenseDao.getAllArchivedExpensesByPrice().map { expenses ->
            expenses.map { it.toRecurringExpenseData(getDefaultCurrencyCode()) }
        }
    override val allTags: Flow<List<Tag>> =
        recurringExpenseDao.getAllTags().map { tags -> tags.toTags() }

    private val defaultCurrency = userPreferencesRepository.defaultCurrency.get()

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData? =
        withContext(ioDispatcher) {
            return@withContext recurringExpenseDao
                .getExpenseById(id)
                ?.toRecurringExpenseData(getDefaultCurrencyCode())
        }

    override suspend fun insert(recurringExpense: RecurringExpenseData) =
        withContext(ioDispatcher) {
            val expenseId =
                recurringExpenseDao.insert(
                    recurringExpense.toEntryRecurringExpense(getDefaultCurrencyCode()),
                )
            recurringExpense.tags.forEach {
                recurringExpenseDao.upsert(ExpenseTagCrossRefEntry(expenseId.toInt(), it.id))
            }
            recurringExpense.reminders.forEach { reminder ->
                recurringExpenseDao.insertReminder(reminder.toReminderEntry(expenseId.toInt()))
            }
        }

    override suspend fun update(recurringExpense: RecurringExpenseData) =
        withContext(ioDispatcher) {
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
        withContext(ioDispatcher) {
            recurringExpenseDao.delete(recurringExpense.toEntryRecurringExpense(getDefaultCurrencyCode()))
            recurringExpenseDao.deleteAllCrossRefForExpenseId(recurringExpense.id)
            recurringExpenseDao.deleteAllRemindersForExpenseId(recurringExpense.id)
            recurringExpenseDao.deleteAllPaymentRecordsForExpenseId(recurringExpense.id)
        }

    override suspend fun archive(
        expenseId: Int,
        archivedDateEpoch: Long,
    ) = withContext(ioDispatcher) {
        recurringExpenseDao.setArchivedDate(expenseId, archivedDateEpoch)
    }

    override suspend fun unarchive(expenseId: Int) =
        withContext(ioDispatcher) {
            recurringExpenseDao.setArchivedDate(expenseId, null)
        }

    override suspend fun clearEndDateIfOverdue(
        expenseId: Int,
        nowEpoch: Long,
    ) = withContext(ioDispatcher) {
        recurringExpenseDao.clearEndDateIfOverdue(expenseId, nowEpoch)
    }

    override suspend fun autoArchiveExpired(nowEpoch: Long): List<AutoArchiveCandidate> =
        withContext(ioDispatcher) {
            val candidates = recurringExpenseDao.getExpensesToAutoArchive(nowEpoch)
            candidates.forEach { candidate ->
                recurringExpenseDao.setArchivedDate(candidate.id, nowEpoch)
            }
            candidates.filter { !it.name.isNullOrBlank() }
        }

    override suspend fun insert(tag: Tag) =
        withContext(ioDispatcher) {
            recurringExpenseDao.insert(tag.toTagEntry())
        }

    override suspend fun update(tag: Tag) =
        withContext(ioDispatcher) {
            recurringExpenseDao.update(tag.toTagEntry())
        }

    override suspend fun delete(tag: Tag) =
        withContext(ioDispatcher) {
            recurringExpenseDao.delete(tag.toTagEntry())
            recurringExpenseDao.deleteAllCrossRefForTagId(tag.id)
        }

    override suspend fun markAsPaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    ) = withContext(ioDispatcher) {
        val existing = recurringExpenseDao.getPaymentRecord(expenseId, paymentDateEpoch)
        if (existing == null) {
            recurringExpenseDao.insertPaymentRecord(
                PaymentRecordEntry(expenseId = expenseId, paymentDate = paymentDateEpoch),
            )
        }
    }

    override suspend fun markAsUnpaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    ) = withContext(ioDispatcher) {
        val record = recurringExpenseDao.getPaymentRecord(expenseId, paymentDateEpoch)
        if (record != null) {
            recurringExpenseDao.deletePaymentRecord(record)
        }
    }

    override suspend fun getPaymentRecordsForExpense(expenseId: Int): List<Long> =
        withContext(ioDispatcher) {
            recurringExpenseDao.getPaymentRecordsForExpense(expenseId).map { it.paymentDate }
        }

    private suspend fun getDefaultCurrencyCode(): String {
        return defaultCurrency.first().ifBlank { getSystemCurrencyCode() }
    }
}
