package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.Recurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeExpenseRepository : IExpenseRepository {
    private val fakeExpense =
        RecurringExpense(
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
            Tag(id = 0, title = "TagTitle1", color = "0xFF00658F"),
            Tag(id = 0, title = "TagTitle1", color = "0xFF4F616E"),
        )

    override val allRecurringExpenses: Flow<List<RecurringExpense>> = flowOf(listOf(fakeExpense))
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpense>> = flowOf(listOf(fakeExpense))
    override val allTags: Flow<List<Tag>> = flowOf(fakeTags)

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpense? {
        return fakeExpense
    }

    override suspend fun getRecurringExpenseWithTagsById(id: Int): RecurringExpenseWithTags? {
        return RecurringExpenseWithTags(fakeExpense, fakeTags)
    }

    override suspend fun insert(recurringExpense: RecurringExpense) {}

    override suspend fun update(recurringExpense: RecurringExpense) {}

    override suspend fun delete(recurringExpense: RecurringExpense) {}

    override suspend fun insert(tag: Tag) {}

    override suspend fun update(tag: Tag) {}

    override suspend fun delete(tag: Tag) {}
}
