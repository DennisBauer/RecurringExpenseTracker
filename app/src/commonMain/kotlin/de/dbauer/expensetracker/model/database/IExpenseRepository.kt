package de.dbauer.expensetracker.model.database

import kotlinx.coroutines.flow.Flow

interface IExpenseRepository {
    val allRecurringExpenses: Flow<List<RecurringExpense>>
    val allRecurringExpensesByPrice: Flow<List<RecurringExpense>>
    val allTags: Flow<List<Tag>>

    suspend fun getRecurringExpenseById(id: Int): RecurringExpense?

    suspend fun getRecurringExpenseWithTagsById(id: Int): RecurringExpenseWithTags?

    suspend fun insert(recurringExpense: RecurringExpense)

    suspend fun update(recurringExpense: RecurringExpense)

    suspend fun delete(recurringExpense: RecurringExpense)

    suspend fun insert(tag: Tag)

    suspend fun update(tag: Tag)

    suspend fun delete(tag: Tag)
}
