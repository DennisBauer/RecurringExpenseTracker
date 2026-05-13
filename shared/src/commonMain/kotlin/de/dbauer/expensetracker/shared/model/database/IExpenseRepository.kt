package de.dbauer.expensetracker.shared.model.database

import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Tag
import kotlinx.coroutines.flow.Flow

interface IExpenseRepository {
    val allRecurringExpenses: Flow<List<RecurringExpenseData>>
    val allRecurringExpensesByPrice: Flow<List<RecurringExpenseData>>
    val allArchivedRecurringExpenses: Flow<List<RecurringExpenseData>>
    val allArchivedRecurringExpensesByPrice: Flow<List<RecurringExpenseData>>
    val allTags: Flow<List<Tag>>

    suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData?

    suspend fun insert(recurringExpense: RecurringExpenseData)

    suspend fun update(recurringExpense: RecurringExpenseData)

    suspend fun delete(recurringExpense: RecurringExpenseData)

    suspend fun archive(
        expenseId: Int,
        archivedDateEpoch: Long,
    )

    suspend fun unarchive(expenseId: Int)

    suspend fun clearEndDateIfOverdue(
        expenseId: Int,
        nowEpoch: Long,
    )

    suspend fun autoArchiveExpired(nowEpoch: Long): List<AutoArchiveCandidate>

    suspend fun insert(tag: Tag)

    suspend fun update(tag: Tag)

    suspend fun delete(tag: Tag)

    suspend fun markAsPaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    )

    suspend fun markAsUnpaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    )

    suspend fun getPaymentRecordsForExpense(expenseId: Int): List<Long>
}
