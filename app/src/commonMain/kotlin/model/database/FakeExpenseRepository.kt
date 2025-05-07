package model.database

import data.Recurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import ui.customizations.ExpenseColor

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
            color = ExpenseColor.Dynamic.ordinal,
            currencyCode = "EUR",
            notifyForExpense = true,
            notifyXDaysBefore = null,
            lastNotificationDate = null,
        )

    override val allRecurringExpenses: Flow<List<RecurringExpense>> = flowOf(listOf(fakeExpense))
    override val allRecurringExpensesByPrice: Flow<List<RecurringExpense>> = flowOf(listOf(fakeExpense))

    override suspend fun getRecurringExpenseById(id: Int): RecurringExpense? {
        return fakeExpense
    }

    override suspend fun insert(recurringExpense: RecurringExpense) {}

    override suspend fun update(recurringExpense: RecurringExpense) {}

    override suspend fun delete(recurringExpense: RecurringExpense) {}
}
