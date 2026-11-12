package de.dbauer.expensetracker.shared.data

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
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

    @Test
    fun `getPaymentDaysBetween returns the same days as repeated getNextPaymentDayAfter`() {
        val firstPayments =
            listOf(
                // Month end: repeated addition clamps to the 28th and stays there
                LocalDateTime(2020, 1, 31, 0, 0),
                // Leap day
                LocalDateTime(2024, 2, 29, 0, 0),
                LocalDateTime(2019, 6, 15, 0, 0),
                // Inside the range
                LocalDateTime(2027, 4, 10, 0, 0),
                // After the range
                LocalDateTime(2030, 1, 1, 0, 0),
            ).map { it.toInstant(TimeZone.UTC) }
        val endDates =
            listOf(
                null,
                // Before the range
                LocalDateTime(2025, 1, 1, 0, 0),
                // Inside the range
                LocalDateTime(2027, 9, 15, 0, 0),
            ).map { it?.toInstant(TimeZone.UTC) }

        val shortStepRecurrences = listOf(Recurrence.Daily, Recurrence.Weekly)
        for (recurrence in Recurrence.entries - shortStepRecurrences) {
            assertSameDaysAsRepeatedNextPaymentDay(
                recurrence = recurrence,
                rangeStart = LocalDate(2026, 8, 1),
                rangeEndExclusive = LocalDate(2029, 3, 1),
                firstPayments = firstPayments,
                endDates = endDates,
            )
        }

        // The reference walks from the first payment once per payment, which is quadratic. Daily
        // and weekly payments therefore use a short range close to the first payments, as years of
        // such steps are too slow on Kotlin/JS. Month end clamping and leap days do not apply to them.
        for (recurrence in shortStepRecurrences) {
            assertSameDaysAsRepeatedNextPaymentDay(
                recurrence = recurrence,
                rangeStart = LocalDate(2026, 8, 1),
                rangeEndExclusive = LocalDate(2026, 11, 1),
                firstPayments =
                    listOf(
                        // Before the range
                        LocalDateTime(2026, 6, 20, 0, 0),
                        // Inside the range
                        LocalDateTime(2026, 9, 10, 0, 0),
                        // After the range
                        LocalDateTime(2027, 1, 1, 0, 0),
                    ).map { it.toInstant(TimeZone.UTC) },
                endDates =
                    listOf(
                        null,
                        // Before the range
                        LocalDateTime(2026, 7, 1, 0, 0),
                        // Inside the range
                        LocalDateTime(2026, 10, 5, 0, 0),
                    ).map { it?.toInstant(TimeZone.UTC) },
            )
        }
    }

    private fun assertSameDaysAsRepeatedNextPaymentDay(
        recurrence: Recurrence,
        rangeStart: LocalDate,
        rangeEndExclusive: LocalDate,
        firstPayments: List<Instant>,
        endDates: List<Instant?>,
    ) {
        for (everyXRecurrence in listOf(1, 2, 5)) {
            for (firstPayment in firstPayments) {
                for (endDate in endDates) {
                    val expense =
                        buildExpense(
                            firstPayment = firstPayment,
                            endDate = endDate,
                            everyXRecurrence = everyXRecurrence,
                            recurrence = recurrence,
                        )

                    val expected = mutableListOf<LocalDate>()
                    var next = expense.getNextPaymentDayAfter(rangeStart)
                    while (next != null && next < rangeEndExclusive) {
                        expected.add(next)
                        next = expense.getNextPaymentDayAfter(next.plus(1, DateTimeUnit.DAY))
                    }

                    assertEquals(
                        expected,
                        expense.getPaymentDaysBetween(rangeStart, rangeEndExclusive),
                        "$recurrence x$everyXRecurrence first=$firstPayment end=$endDate",
                    )
                }
            }
        }
    }

    @Test
    fun `getPaymentDaysBetween includes range start and excludes range end`() {
        val expense =
            buildExpense(
                firstPayment = LocalDateTime(2025, 1, 1, 0, 0).toInstant(TimeZone.UTC),
                endDate = null,
            )

        val days = expense.getPaymentDaysBetween(LocalDate(2025, 3, 1), LocalDate(2025, 5, 1))
        assertEquals(listOf(LocalDate(2025, 3, 1), LocalDate(2025, 4, 1)), days)
    }

    @Test
    fun `getPaymentDaysBetween returns nothing without a first payment`() {
        val expense = buildExpense(firstPayment = null, endDate = null)

        val days = expense.getPaymentDaysBetween(LocalDate(2025, 1, 1), LocalDate(2030, 1, 1))
        assertEquals(emptyList(), days)
    }

    private fun buildExpense(
        firstPayment: Instant?,
        endDate: Instant?,
        everyXRecurrence: Int = 1,
        recurrence: Recurrence = Recurrence.Monthly,
    ): RecurringExpenseData {
        return RecurringExpenseData(
            id = 1,
            name = "test",
            description = "",
            price = CurrencyValue(10f, "EUR", false),
            monthlyPrice = CurrencyValue(10f, "EUR", false),
            everyXRecurrence = everyXRecurrence,
            recurrence = recurrence,
            tags = emptyList(),
            firstPayment = firstPayment,
            notifyForExpense = false,
            endDate = endDate,
        )
    }
}
