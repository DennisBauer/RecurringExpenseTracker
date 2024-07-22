package model

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

object DateTimeCalculator {
    fun getDaysFromNowUntil(until: LocalDate): Int {
        return getDaysFromUntil(Clock.System.now(), until)
    }

    fun getDaysFromUntil(
        from: Instant,
        until: LocalDate,
        fromTimeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): Int {
        val fromStartOfDay = from.toLocalDateTime(fromTimeZone).date.atStartOfDayIn(TimeZone.UTC)
        return fromStartOfDay.daysUntil(until.atStartOfDayIn(TimeZone.UTC), TimeZone.UTC)
    }

    fun getDayOfNextOccurrenceFromNow(
        from: Instant,
        everyXRecurrence: Int,
        recurrence: DateTimeUnit.DateBased,
    ): LocalDate {
        return getDayOfNextOccurrence(Clock.System.now(), from, everyXRecurrence, recurrence)
    }

    fun getDayOfNextOccurrence(
        atPointInTime: Instant,
        first: Instant,
        everyXRecurrence: Int,
        recurrence: DateTimeUnit.DateBased,
        atPointInTimeZone: TimeZone = TimeZone.currentSystemDefault(),
    ): LocalDate {
        val pointInTime = atPointInTime.toLocalDateTime(atPointInTimeZone).date
        var nextOccurrence = first.toLocalDateTime(TimeZone.UTC).date

        while (pointInTime.isInDaysAfter(nextOccurrence) && !pointInTime.isSameDay(nextOccurrence)) {
            nextOccurrence = nextOccurrence.plus(everyXRecurrence, recurrence)
        }
        return nextOccurrence
    }

    private fun LocalDate.isSameDay(other: LocalDate): Boolean {
        return this.year == other.year &&
            this.month == other.month &&
            this.dayOfMonth == other.dayOfMonth
    }

    private fun LocalDate.isInDaysAfter(other: LocalDate): Boolean {
        if (this.year > other.year) {
            return true
        } else if (this.year == other.year &&
            this.month > other.month
        ) {
            return true
        } else if (this.year == other.year &&
            this.month == other.month &&
            this.dayOfMonth > other.dayOfMonth
        ) {
            return true
        }
        return false
    }
}
