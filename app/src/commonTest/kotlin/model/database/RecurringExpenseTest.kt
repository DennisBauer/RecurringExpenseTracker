package model.database

import kotlin.test.Test
import kotlin.test.assertEquals

class RecurringExpenseTest {
    @Test
    fun checkGetMonthlyPrice_usingDailyRecurrence_onceADay() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 5f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Daily.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(152.08333f, expense.getMonthlyPrice())
    }

    @Test
    fun checkGetMonthlyPrice_usingDailyRecurrence_everyFewDays() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 7f,
                everyXRecurrence = 8,
                recurrence = RecurrenceDatabase.Daily.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(26.614582f, expense.getMonthlyPrice())
    }

    @Test
    fun checkGetMonthlyPrice_usingWeeklyRecurrence_onceAWeek() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Weekly.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(43.333336f, expense.getMonthlyPrice())
    }

    @Test
    fun checkGetMonthlyPrice_usingWeeklyRecurrence_everyFewWeeks() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 5,
                recurrence = RecurrenceDatabase.Weekly.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(8.666667f, expense.getMonthlyPrice())
    }

    @Test
    fun checkGetMonthlyPrice_usingMonthlyRecurrence_onceAMonth() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Monthly.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(10f, expense.getMonthlyPrice())
    }

    @Test
    fun checkGetMonthlyPrice_usingMonthlyRecurrence_everyFewMonths() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 5,
                recurrence = RecurrenceDatabase.Monthly.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(2.0f, expense.getMonthlyPrice())
    }

    @Test
    fun checkGetMonthlyPrice_usingYearlyRecurrence_onceAYear() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 1,
                recurrence = RecurrenceDatabase.Yearly.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(0.8333333f, expense.getMonthlyPrice())
    }

    @Test
    fun checkGetMonthlyPrice_usingYearlyRecurrence_everyFewYears() {
        val expense =
            RecurringExpense(
                id = 1,
                name = null,
                description = null,
                price = 10f,
                everyXRecurrence = 5,
                recurrence = RecurrenceDatabase.Yearly.value,
                firstPayment = null,
                color = null,
                currencyCode = "USD",
                notifyForExpense = true,
                notifyXDaysBefore = null,
                lastNotificationDate = null,
            )

        assertEquals(0.16666667f, expense.getMonthlyPrice())
    }
}
