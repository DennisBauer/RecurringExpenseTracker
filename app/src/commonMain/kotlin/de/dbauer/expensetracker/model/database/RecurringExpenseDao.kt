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
    fun getAllExpenses(): Flow<List<RecurringExpense>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses ORDER BY price DESC")
    fun getAllExpensesByPrice(): Flow<List<RecurringExpense>>

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): RecurringExpense?

    @Transaction
    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    suspend fun getExpenseWithTagsById(id: Int): RecurringExpenseWithTags?

    @Transaction
    @Query("SELECT * FROM tags")
    fun getAllTags(): Flow<List<Tag>>

    @Transaction
    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Int): Tag

    @Insert
    suspend fun insert(recurringExpense: RecurringExpense)

    @Update
    suspend fun update(recurringExpense: RecurringExpense)

    @Delete
    suspend fun delete(recurringExpense: RecurringExpense)

    @Insert
    suspend fun insert(tag: Tag)

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)
}
