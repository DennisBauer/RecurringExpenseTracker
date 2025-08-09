package de.dbauer.expensetracker.model.database

import kotlinx.coroutines.flow.Flow

interface IExpenseRepository {
    val allRecurringExpenses: Flow<List<EntryRecurringExpense>>
    val allRecurringExpensesByPrice: Flow<List<EntryRecurringExpense>>
    val allTags: Flow<List<EntryTag>>

    suspend fun getRecurringExpenseById(id: Int): EntryRecurringExpense?

    suspend fun getRecurringExpenseWithTagsById(id: Int): RecurringExpenseWithTags?

    suspend fun insert(recurringExpense: EntryRecurringExpense)

    suspend fun update(recurringExpense: EntryRecurringExpense)

    suspend fun delete(recurringExpense: EntryRecurringExpense)

    suspend fun insert(tag: EntryTag)

    suspend fun update(tag: EntryTag)

    suspend fun delete(tag: EntryTag)
}
