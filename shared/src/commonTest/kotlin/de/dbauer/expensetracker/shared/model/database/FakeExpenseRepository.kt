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

    private val paymentRecords = mutableMapOf<Int, MutableSet<Long>>()

    private val archivedExpensesFlow = MutableStateFlow<List<RecurringExpenseData>>(emptyList())

    override val allRecurringExpenses: Flow<List<RecurringExpenseData>>
        get() = expensesFlow
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpenseData>>
        get() = expensesFlow
    override val allArchivedRecurringExpenses: Flow<List<RecurringExpenseData>>
        get() = archivedExpensesFlow
    override val allArchivedRecurringExpensesByPrice: Flow<List<RecurringExpenseData>>
        get() = archivedExpensesFlow
    override val allTags: Flow<List<Tag>>
        get() = tagsFlow

    private fun refreshArchivedFlow() {
        archivedExpensesFlow.value = expenses.values.filter { it.archivedDate != null }
    }

    private fun refreshActiveFlow() {
        expensesFlow.value = expenses.values.filter { it.archivedDate == null }
    }

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData? {
        return expenses[id]
    }

    override suspend fun insert(recurringExpense: RecurringExpenseData) {
        expenses[recurringExpense.id] = recurringExpense
        refreshActiveFlow()
        refreshArchivedFlow()
    }

    override suspend fun update(recurringExpense: RecurringExpenseData) {
        expenses[recurringExpense.id] = recurringExpense
        refreshActiveFlow()
        refreshArchivedFlow()
    }

    override suspend fun delete(recurringExpense: RecurringExpenseData) {
        expenses.remove(recurringExpense.id)
        refreshActiveFlow()
        refreshArchivedFlow()
    }

    override suspend fun archive(
        expenseId: Int,
        archivedDateEpoch: Long,
    ) {
        expenses[expenseId]?.let {
            expenses[expenseId] =
                it.copy(archivedDate = kotlin.time.Instant.fromEpochMilliseconds(archivedDateEpoch))
        }
        refreshActiveFlow()
        refreshArchivedFlow()
    }

    override suspend fun unarchive(expenseId: Int) {
        expenses[expenseId]?.let {
            expenses[expenseId] = it.copy(archivedDate = null)
        }
        refreshActiveFlow()
        refreshArchivedFlow()
    }

    override suspend fun clearEndDateIfOverdue(
        expenseId: Int,
        nowEpoch: Long,
    ) {
        val existing = expenses[expenseId] ?: return
        val end = existing.endDate ?: return
        if (end.toEpochMilliseconds() <= nowEpoch) {
            expenses[expenseId] = existing.copy(endDate = null)
            refreshActiveFlow()
            refreshArchivedFlow()
        }
    }

    override suspend fun autoArchiveExpired(nowEpoch: Long): List<AutoArchiveCandidate> {
        val instant = kotlin.time.Instant.fromEpochMilliseconds(nowEpoch)
        val archived = mutableListOf<AutoArchiveCandidate>()
        expenses.keys.toList().forEach { id ->
            val e = expenses[id] ?: return@forEach
            val end = e.endDate ?: return@forEach
            if (e.archivedDate == null && end.toEpochMilliseconds() <= nowEpoch) {
                expenses[id] = e.copy(archivedDate = instant)
                archived.add(AutoArchiveCandidate(id = e.id, name = e.name))
            }
        }
        refreshActiveFlow()
        refreshArchivedFlow()
        return archived
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

    override suspend fun markAsPaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    ) {
        paymentRecords.getOrPut(expenseId) { mutableSetOf() }.add(paymentDateEpoch)
    }

    override suspend fun markAsUnpaid(
        expenseId: Int,
        paymentDateEpoch: Long,
    ) {
        paymentRecords[expenseId]?.remove(paymentDateEpoch)
    }

    override suspend fun getPaymentRecordsForExpense(expenseId: Int): List<Long> {
        return paymentRecords[expenseId]?.toList() ?: emptyList()
    }

    fun clearExpenses() {
        expenses.clear()
        expensesFlow.value = emptyList()
        archivedExpensesFlow.value = emptyList()
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
