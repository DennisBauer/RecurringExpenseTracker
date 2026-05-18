package de.dbauer.expensetracker.shared.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class RecurringExpenseDataTest {
    @Test
    fun `getNextPaymentDayAfter returns date on the same day as endDate inclusive`() {
        val expense =
            buildExpense(
                firstPayment = LocalDateTime(2025, 1, 15, 0, 0).toInstant(TimeZone.UTC),
                endDate = LocalDateTime(2025, 3, 15, 0, 0).toInstant(TimeZone.UTC),
            )

        val next = expense.getNextPaymentDayAfter(LocalDate(2025, 3, 1))
        assertEquals(LocalDate(2025, 3, 15), next)
    }

    @Test
    fun `getNextPaymentDayAfter returns null when next occurrence is past endDate`() {
        val expense =
            buildExpense(
                firstPayment = LocalDateTime(2025, 1, 15, 0, 0).toInstant(TimeZone.UTC),
                endDate = LocalDateTime(2025, 3, 15, 0, 0).toInstant(TimeZone.UTC),
            )

        val next = expense.getNextPaymentDayAfter(LocalDate(2025, 3, 16))
        assertNull(next)
    }

    @Test
    fun `getNextPaymentDayAfter returns null for every iterator past endDate`() {
        val expense =
            buildExpense(
                firstPayment = LocalDateTime(2025, 1, 1, 0, 0).toInstant(TimeZone.UTC),
                endDate = LocalDateTime(2025, 3, 31, 0, 0).toInstant(TimeZone.UTC),
            )

        assertNull(expense.getNextPaymentDayAfter(LocalDate(2025, 5, 1)))
        assertNull(expense.getNextPaymentDayAfter(LocalDate(2025, 6, 1)))
        assertNull(expense.getNextPaymentDayAfter(LocalDate(2026, 1, 1)))
    }

    @Test
    fun `getNextPaymentDayAfter is unchanged when endDate is null`() {
        val expense =
            buildExpense(
                firstPayment = LocalDateTime(2025, 1, 15, 0, 0).toInstant(TimeZone.UTC),
                endDate = null,
            )

        val next = expense.getNextPaymentDayAfter(LocalDate(2025, 5, 16))
        assertEquals(LocalDate(2025, 6, 15), next)
    }

    private fun buildExpense(
        firstPayment: Instant?,
        endDate: Instant?,
    ): RecurringExpenseData {
        return RecurringExpenseData(
            id = 1,
            name = "test",
            description = "",
            price = CurrencyValue(10f, "EUR", false),
            monthlyPrice = CurrencyValue(10f, "EUR", false),
            everyXRecurrence = 1,
            recurrence = Recurrence.Monthly,
            tags = emptyList(),
            firstPayment = firstPayment,
            notifyForExpense = false,
            endDate = endDate,
        )
    }
}
