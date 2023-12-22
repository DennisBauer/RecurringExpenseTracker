package de.dbauer.expensetracker.viewmodel.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringExpenseDao {
    @Query("SELECT * FROM recurring_expenses")
    fun getAll(): Flow<List<RecurringExpense>>

    @Query("SELECT * FROM recurring_expenses ORDER BY price DESC")
    fun getAllByPrice(): Flow<List<RecurringExpense>>

    @Query("SELECT * FROM recurring_expenses WHERE id = :id")
    fun getById(id: Int): RecurringExpense?

    @Insert
    fun insert(recurringExpense: RecurringExpense)

    @Update
    fun update(recurringExpense: RecurringExpense)

    @Delete
    fun delete(recurringExpense: RecurringExpense)
}
