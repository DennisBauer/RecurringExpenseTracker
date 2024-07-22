package model

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeCalculatorTest {
    @Test
    fun checkDayCalculationWorksFromAnyTimeOfDay() {
        val expected = 1

        for (hour in 0..23) {
            val actual =
                DateTimeCalculator.getDaysFromUntil(
                    from = LocalDateTime(2024, 7, 21, hour, 0, 7, 10).toInstant(TimeZone.currentSystemDefault()),
                    until = LocalDate(2024, 7, 22),
                )

            assertEquals(expected, actual, "Failed the data calculation didn't work using hour $hour")
        }
    }

    @Test
    fun checkDayCalculationFromDifferentTimeZones() {
        val expected = 34

        listOf(
            TimeZone.of("America/New_York"),
            TimeZone.of("Pacific/Midway"),
            TimeZone.of("Asia/Tokyo"),
        ).forEach { timeZone ->
            var actual =
                DateTimeCalculator.getDaysFromUntil(
                    from = LocalDateTime(2024, 6, 18, 0, 0, 7, 10).toInstant(timeZone),
                    until = LocalDate(2024, 7, 22),
                    fromTimeZone = timeZone,
                )
            assertEquals(expected, actual)

            actual =
                DateTimeCalculator.getDaysFromUntil(
                    from = LocalDateTime(2024, 6, 18, 23, 0, 7, 10).toInstant(timeZone),
                    until = LocalDate(2024, 7, 22),
                    fromTimeZone = timeZone,
                )

            assertEquals(expected, actual, "Failed the data calculation didn't work using time zone: $timeZone")
        }
    }

    @Test
    fun checkDayOfNextOccurrenceFromAnyTimeOfDay() {
        val expected = LocalDate(2024, 7, 12)

        for (hour in 0..23) {
            val actual =
                DateTimeCalculator.getDayOfNextOccurrence(
                    atPointInTime =
                        LocalDateTime(2024, 7, 1, hour, 0, 7, 10)
                            .toInstant(TimeZone.currentSystemDefault()),
                    first =
                        LocalDateTime(2024, 5, 12, 0, 0, 0, 0)
                            .toInstant(TimeZone.UTC),
                    everyXRecurrence = 1,
                    recurrence = DateTimeUnit.MONTH,
                )

            assertEquals(expected, actual, "Failed the data calculation didn't work using hour $hour")
        }
    }

    @Test
    fun checkDayOfNextOccurrenceFromDifferentTimeZones() {
        val expected = LocalDate(2024, 6, 1)

        listOf(
            TimeZone.of("America/New_York"),
            TimeZone.of("Pacific/Midway"),
            TimeZone.of("Asia/Tokyo"),
        ).forEach { timeZone ->
            var actual =
                DateTimeCalculator.getDayOfNextOccurrence(
                    atPointInTime =
                        LocalDateTime(2024, 5, 29, 0, 0, 7, 10)
                            .toInstant(timeZone),
                    first =
                        LocalDateTime(2023, 11, 1, 0, 0, 0, 0)
                            .toInstant(TimeZone.UTC),
                    everyXRecurrence = 1,
                    recurrence = DateTimeUnit.MONTH,
                    atPointInTimeZone = timeZone,
                )

            assertEquals(expected, actual, "Failed the data calculation didn't work using time zone: $timeZone")

            actual =
                DateTimeCalculator.getDayOfNextOccurrence(
                    atPointInTime =
                        LocalDateTime(2024, 5, 29, 0, 0, 7, 10)
                            .toInstant(timeZone),
                    first =
                        LocalDateTime(2023, 11, 1, 0, 0, 0, 0)
                            .toInstant(TimeZone.UTC),
                    everyXRecurrence = 1,
                    recurrence = DateTimeUnit.MONTH,
                    atPointInTimeZone = timeZone,
                )

            assertEquals(expected, actual, "Failed the data calculation didn't work using time zone: $timeZone")
        }
    }

    @Test
    fun checkDayOfNextOccurrenceWithDailyRecurrence() {
        var expected = LocalDate(2024, 5, 25)
        var actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 5, 25, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2024, 4, 30, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 1,
                recurrence = DateTimeUnit.DAY,
            )
        assertEquals(expected, actual)

        expected = LocalDate(2024, 5, 25)
        actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 5, 24, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2024, 4, 30, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 5,
                recurrence = DateTimeUnit.DAY,
            )
        assertEquals(expected, actual)
    }

    @Test
    fun checkDayOfNextOccurrenceWithWeeklyRecurrence() {
        var expected = LocalDate(2024, 6, 20)
        var actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 6, 14, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2024, 5, 2, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 1,
                recurrence = DateTimeUnit.WEEK,
            )
        assertEquals(expected, actual)

        expected = LocalDate(2024, 6, 27)
        actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 6, 14, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2024, 5, 2, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 2,
                recurrence = DateTimeUnit.WEEK,
            )
        assertEquals(expected, actual)
    }

    @Test
    fun checkDayOfNextOccurrenceWithMonthlyRecurrence() {
        // Verify monthly edge case e.g. with February when defining a day which isn't available in the next month
        var expected = LocalDate(2024, 2, 29)
        var actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 2, 28, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2024, 1, 30, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 1,
                recurrence = DateTimeUnit.MONTH,
            )
        assertEquals(expected, actual)

        expected = LocalDate(2024, 6, 15)
        actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 6, 15, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2023, 12, 15, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 3,
                recurrence = DateTimeUnit.MONTH,
            )
        assertEquals(expected, actual)

        expected = LocalDate(2024, 6, 15)
        actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 5, 16, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2023, 12, 15, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 3,
                recurrence = DateTimeUnit.MONTH,
            )
        assertEquals(expected, actual)
    }

    @Test
    fun checkDayOfNextOccurrenceWithYearlyRecurrence() {
        var expected = LocalDate(2024, 6, 1)
        var actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 5, 29, 23, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2023, 6, 1, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 1,
                recurrence = DateTimeUnit.YEAR,
            )
        assertEquals(expected, actual)

        expected = LocalDate(2026, 6, 1)
        actual =
            DateTimeCalculator.getDayOfNextOccurrence(
                atPointInTime =
                    LocalDateTime(2024, 6, 2, 1, 0, 7, 10)
                        .toInstant(TimeZone.currentSystemDefault()),
                first =
                    LocalDateTime(2022, 6, 1, 0, 0, 0, 0)
                        .toInstant(TimeZone.UTC),
                everyXRecurrence = 2,
                recurrence = DateTimeUnit.YEAR,
            )
        assertEquals(expected, actual)
    }
}
