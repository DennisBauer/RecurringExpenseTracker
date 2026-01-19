package de.dbauer.expensetracker.shared.model.database

import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.data.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExpenseRepository : IExpenseRepository {
    private val expenses = mutableMapOf<Int, RecurringExpenseData>()
    private val expensesFlow = MutableStateFlow<List<RecurringExpenseData>>(emptyList())

    private val tags = mutableMapOf<Int, Tag>()
    private val tagsFlow = MutableStateFlow<List<Tag>>(emptyList())

    override val allRecurringExpenses: Flow<List<RecurringExpenseData>>
        get() = expensesFlow
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpenseData>>
        get() = expensesFlow
    override val allTags: Flow<List<Tag>>
        get() = tagsFlow

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData? {
        return expenses[id]
    }

    override suspend fun insert(recurringExpense: RecurringExpenseData) {
        expenses[recurringExpense.id] = recurringExpense
        expensesFlow.value = expenses.values.toList()
    }

    override suspend fun update(recurringExpense: RecurringExpenseData) {
        expenses[recurringExpense.id] = recurringExpense
        expensesFlow.value = expenses.values.toList()
    }

    override suspend fun delete(recurringExpense: RecurringExpenseData) {
        expenses.remove(recurringExpense.id)
        expensesFlow.value = expenses.values.toList()
    }

    override suspend fun insert(tag: Tag) {
        tags[tag.id] = tag
        tagsFlow.value = tags.values.toList()
    }

    override suspend fun update(tag: Tag) {
        tags[tag.id] = tag
        tagsFlow.value = tags.values.toList()
    }

    override suspend fun delete(tag: Tag) {
        tags.remove(tag.id)
        tagsFlow.value = tags.values.toList()
    }

    fun clearExpenses() {
        expenses.clear()
        expensesFlow.value = emptyList()
    }

    fun clearTags() {
        tags.clear()
        tagsFlow.value = emptyList()
    }

    fun clearAll() {
        clearExpenses()
        clearTags()
    }
}
