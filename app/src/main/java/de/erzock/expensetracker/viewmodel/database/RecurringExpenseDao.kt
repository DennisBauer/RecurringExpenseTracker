package de.erzock.expensetracker.viewmodel.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses")
    fun getAll(): Flow<List<RecurringExpense>>

    @Query("SELECT * FROM recurring_expenses ORDER BY price DESC")
    fun getAllByPrice(): Flow<List<RecurringExpense>>

    @Insert
    fun insert(recurringExpense: RecurringExpense)

    @Delete
    fun delete(recurringExpense: RecurringExpense)
}