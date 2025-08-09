package de.dbauer.expensetracker.model.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Transaction
    @Query("SELECT * FROM recurring_expenses")
    fun getAllExpenses(): Flow<List<EntryRecurringExpenseWithTags>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses ORDER BY price DESC")
    fun getAllExpensesByPrice(): Flow<List<EntryRecurringExpenseWithTags>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): EntryRecurringExpenseWithTags?

    @Transaction
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<EntryTag>>

    @Transaction
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Int): EntryTag

    @Insert
    suspend fun insert(recurringExpense: EntryRecurringExpense)

    @Update
    suspend fun update(recurringExpense: EntryRecurringExpense)

    @Delete
    suspend fun delete(recurringExpense: EntryRecurringExpense)

    @Insert
    suspend fun insert(tag: EntryTag)

    @Update
    suspend fun update(tag: EntryTag)

    @Delete
    suspend fun delete(tag: EntryTag)
}
