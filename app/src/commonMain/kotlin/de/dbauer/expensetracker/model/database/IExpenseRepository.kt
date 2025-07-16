package de.dbauer.expensetracker.model.database

import kotlinx.coroutines.flow.Flow

interface IExpenseRepository {
    val allRecurringExpenses: Flow<List<RecurringExpense>>
    val allRecurringExpensesByPrice: Flow<List<RecurringExpense>>

    suspend fun getRecurringExpenseById(id: Int): RecurringExpense?

    suspend fun insert(recurringExpense: RecurringExpense)

    suspend fun update(recurringExpense: RecurringExpense)

    suspend fun delete(recurringExpense: RecurringExpense)
}
