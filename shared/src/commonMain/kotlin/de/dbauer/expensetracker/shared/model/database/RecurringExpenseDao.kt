package de.dbauer.expensetracker.shared.model.database

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE archivedDate IS NULL")
    fun getAllExpenses(): Flow<List<RecurringExpenseWithTagsEntry>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE archivedDate IS NULL ORDER BY price DESC")
    fun getAllExpensesByPrice(): Flow<List<RecurringExpenseWithTagsEntry>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE archivedDate IS NOT NULL ORDER BY archivedDate DESC")
    fun getAllArchivedExpenses(): Flow<List<RecurringExpenseWithTagsEntry>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE archivedDate IS NOT NULL ORDER BY price DESC")
    fun getAllArchivedExpensesByPrice(): Flow<List<RecurringExpenseWithTagsEntry>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): RecurringExpenseWithTagsEntry?

    @Query("UPDATE recurring_expenses SET archivedDate = :archivedDate WHERE id = :id")
    suspend fun setArchivedDate(
        id: Int,
        archivedDate: Long?,
    )

    @Query(
        "SELECT id, name FROM recurring_expenses WHERE endDate IS NOT NULL AND endDate <= :now AND archivedDate IS NULL",
    )
    suspend fun getExpensesToAutoArchive(now: Long): List<AutoArchiveCandidate>

    @Query(
        "UPDATE recurring_expenses SET endDate = NULL WHERE id = :id AND endDate IS NOT NULL AND endDate <= :now",
    )
    suspend fun clearEndDateIfOverdue(
        id: Int,
        now: Long,
    )

    @Transaction
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<TagEntry>>

    @Transaction
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Int): TagEntry

    @Insert
    suspend fun insert(recurringExpense: RecurringExpenseEntry): Long

    @Update
    suspend fun update(recurringExpense: RecurringExpenseEntry)

    @Delete
    suspend fun delete(recurringExpense: RecurringExpenseEntry)

    @Insert
    suspend fun insert(tag: TagEntry)

    @Update
    suspend fun update(tag: TagEntry)

    @Delete
    suspend fun delete(tag: TagEntry)

    @Upsert()
    suspend fun upsert(expenseTagCrossRef: ExpenseTagCrossRefEntry)

    @Delete
    suspend fun delete(expenseTagCrossRef: ExpenseTagCrossRefEntry)

    @Query("DELETE FROM ExpenseTagCrossRef WHERE tagId = :tagId")
    suspend fun deleteAllCrossRefForTagId(tagId: Int)

    @Query("DELETE FROM ExpenseTagCrossRef WHERE expenseId = :expenseId")
    suspend fun deleteAllCrossRefForExpenseId(expenseId: Int)

    @Insert
    suspend fun insertReminder(reminder: ReminderEntry): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntry)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntry)

    @Query("DELETE FROM reminders WHERE expenseId = :expenseId")
    suspend fun deleteAllRemindersForExpenseId(expenseId: Int)

    @Query("SELECT * FROM reminders WHERE expenseId = :expenseId")
    suspend fun getRemindersForExpense(expenseId: Int): List<ReminderEntry>

    @Insert
    suspend fun insertPaymentRecord(paymentRecord: PaymentRecordEntry): Long

    @Delete
    suspend fun deletePaymentRecord(paymentRecord: PaymentRecordEntry)

    @Query("SELECT * FROM payment_records WHERE expenseId = :expenseId")
    suspend fun getPaymentRecordsForExpense(expenseId: Int): List<PaymentRecordEntry>

    @Query("SELECT * FROM payment_records WHERE expenseId = :expenseId AND paymentDate = :paymentDate LIMIT 1")
    suspend fun getPaymentRecord(
        expenseId: Int,
        paymentDate: Long,
    ): PaymentRecordEntry?

    @Query("DELETE FROM payment_records WHERE expenseId = :expenseId")
    suspend fun deleteAllPaymentRecordsForExpenseId(expenseId: Int)
}
