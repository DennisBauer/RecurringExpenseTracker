package de.dbauer.expensetracker.shared.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Transaction
    @Query("SELECT * FROM recurring_expenses")
    fun getAllExpenses(): Flow<List<RecurringExpenseWithTagsEntry>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses ORDER BY price DESC")
    fun getAllExpensesByPrice(): Flow<List<RecurringExpenseWithTagsEntry>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): RecurringExpenseWithTagsEntry?

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
}
