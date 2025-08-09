package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.Tag
import kotlinx.coroutines.flow.Flow

interface IExpenseRepository {
    val allRecurringExpenses: Flow<List<RecurringExpenseData>>
    val allRecurringExpensesByPrice: Flow<List<RecurringExpenseData>>
    val allTags: Flow<List<Tag>>

    suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData?

    suspend fun getRecurringExpenseWithTagsById(id: Int): RecurringExpenseWithTags?

    suspend fun insert(recurringExpense: RecurringExpenseData)

    suspend fun update(recurringExpense: RecurringExpenseData)

    suspend fun delete(recurringExpense: RecurringExpenseData)

    suspend fun insert(tag: Tag)

    suspend fun update(tag: Tag)

    suspend fun delete(tag: Tag)
}
