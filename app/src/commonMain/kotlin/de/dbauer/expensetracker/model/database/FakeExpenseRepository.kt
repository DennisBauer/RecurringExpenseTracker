package de.dbauer.expensetracker.model.database

import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeExpenseRepository : IExpenseRepository {
    private val fakeTags =
        listOf(
            Tag(title = "TagTitle1", color = "0xFF00658F"),
            Tag(title = "TagTitle1", color = "0xFF4F616E"),
        )
    private val fakeExpense =
        RecurringExpenseData(
            id = 0,
            name = "name",
            description = "description",
            price = CurrencyValue(10f, "EUR"),
            monthlyPrice = CurrencyValue(10f, "EUR"),
            everyXRecurrence = 1,
            recurrence = Recurrence.Monthly,
            tags = fakeTags,
            firstPayment = null,
            notifyForExpense = true,
            notifyXDaysBefore = null,
            lastNotificationDate = null,
        )

    override val allRecurringExpenses: Flow<List<RecurringExpenseData>> = flowOf(listOf(fakeExpense))
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpenseData>> = flowOf(listOf(fakeExpense))
    override val allTags: Flow<List<Tag>> = flowOf(fakeTags)

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpenseData? {
        return fakeExpense
    }

    override suspend fun insert(recurringExpense: RecurringExpenseData) {}

    override suspend fun update(recurringExpense: RecurringExpenseData) {}

    override suspend fun delete(recurringExpense: RecurringExpenseData) {}

    override suspend fun insert(tag: Tag) {}

    override suspend fun update(tag: Tag) {}

    override suspend fun delete(tag: Tag) {}
}
