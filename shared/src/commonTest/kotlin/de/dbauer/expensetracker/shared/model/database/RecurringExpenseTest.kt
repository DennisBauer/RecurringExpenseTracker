package de.dbauer.expensetracker.shared.model.database

import kotlin.test.Test
import kotlin.test.assertEquals

// Kotlin/JS represents Float as a 64-bit JS number, so results are not rounded to 32 bit.
private const val PRICE_TOLERANCE = 0.0001f

class RecurringExpenseTest {
    @Test
    fun checkGetMonthlyPrice_usingDailyRecurrence_onceADay() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 5f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Daily.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(152.08333f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }

    @Test
    fun checkGetMonthlyPrice_usingDailyRecurrence_everyFewDays() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 7f,
                everyXRecurrence = 8,
                recurrence = RecurrenceDatabase.Daily.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(26.614582f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }

    @Test
    fun checkGetMonthlyPrice_usingWeeklyRecurrence_onceAWeek() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Weekly.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(43.333336f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }

    @Test
    fun checkGetMonthlyPrice_usingWeeklyRecurrence_everyFewWeeks() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 5,
                recurrence = RecurrenceDatabase.Weekly.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(8.666667f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }

    @Test
    fun checkGetMonthlyPrice_usingMonthlyRecurrence_onceAMonth() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Monthly.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(10f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }

    @Test
    fun checkGetMonthlyPrice_usingMonthlyRecurrence_everyFewMonths() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 5,
                recurrence = RecurrenceDatabase.Monthly.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(2.0f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }

    @Test
    fun checkGetMonthlyPrice_usingYearlyRecurrence_onceAYear() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Yearly.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(0.8333333f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }

    @Test
    fun checkGetMonthlyPrice_usingYearlyRecurrence_everyFewYears() {
        val expense =
            RecurringExpenseEntry(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 5,
                recurrence = RecurrenceDatabase.Yearly.value,
                firstPayment = null,
                currencyCode = "USD",
                notifyForExpense = true,
            )

        assertEquals(0.16666667f, expense.getMonthlyPrice(), PRICE_TOLERANCE)
    }
}
