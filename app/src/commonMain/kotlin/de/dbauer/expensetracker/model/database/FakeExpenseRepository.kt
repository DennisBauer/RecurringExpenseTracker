package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.Recurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeExpenseRepository : IExpenseRepository {
    private val fakeExpense =
        EntryRecurringExpense(
            id = 0,
            name = "name",
            description = "description",
            price = 10f,
            everyXRecurrence = 1,
            recurrence = Recurrence.Monthly.ordinal,
            firstPayment = null,
            currencyCode = "EUR",
            notifyForExpense = true,
            notifyXDaysBefore = null,
            lastNotificationDate = null,
        )
    private val fakeTags =
        listOf(
            EntryTag(id = 0, title = "TagTitle1", color = "0xFF00658F"),
            EntryTag(id = 0, title = "TagTitle1", color = "0xFF4F616E"),
        )

    override val allRecurringExpenses: Flow<List<EntryRecurringExpense>> = flowOf(listOf(fakeExpense))
    override val allRecurringExpensesByPrice: Flow<List<EntryRecurringExpense>> = flowOf(listOf(fakeExpense))
    override val allTags: Flow<List<EntryTag>> = flowOf(fakeTags)

    override suspend fun getRecurringExpenseById(id: Int): EntryRecurringExpense? {
        return fakeExpense
    }

    override suspend fun getRecurringExpenseWithTagsById(id: Int): RecurringExpenseWithTags? {
        return RecurringExpenseWithTags(fakeExpense, fakeTags)
    }

    override suspend fun insert(recurringExpense: EntryRecurringExpense) {}

    override suspend fun update(recurringExpense: EntryRecurringExpense) {}

    override suspend fun delete(recurringExpense: EntryRecurringExpense) {}

    override suspend fun insert(tag: EntryTag) {}

    override suspend fun update(tag: EntryTag) {}

    override suspend fun delete(tag: EntryTag) {}
}
