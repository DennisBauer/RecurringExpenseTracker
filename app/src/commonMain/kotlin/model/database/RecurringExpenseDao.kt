package model.database

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
    suspend fun getById(id: Int): RecurringExpense?

    @Insert
    suspend fun insert(recurringExpense: RecurringExpense)

    @Update
    suspend fun update(recurringExpense: RecurringExpense)

    @Delete
    suspend fun delete(recurringExpense: RecurringExpense)
}
