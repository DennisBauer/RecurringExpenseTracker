package model

import androidx.annotation.VisibleForTesting
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
        return getDaysFromUntil(getCurrentLocalDate(), until)
    }

    fun getDaysFromUntil(
        from: LocalDate,
        until: LocalDate,
    ): Int {
        val fromStartOfDay = from.atStartOfDayIn(TimeZone.UTC)
        return fromStartOfDay.daysUntil(until.atStartOfDayIn(TimeZone.UTC), TimeZone.UTC)
    }

    fun getDayOfNextOccurrenceFromNow(
        from: Instant,
        everyXRecurrence: Int,
        recurrence: DateTimeUnit.DateBased,
    ): LocalDate {
        return getDayOfNextOccurrence(getCurrentLocalDate(), from, everyXRecurrence, recurrence)
    }

    fun getDayOfNextOccurrence(
        afterDay: LocalDate,
        first: Instant,
        everyXRecurrence: Int,
        recurrence: DateTimeUnit.DateBased,
    ): LocalDate {
        var nextOccurrence = first.toLocalDateTime(TimeZone.UTC).date

        while (afterDay.isInDaysAfter(nextOccurrence) && !afterDay.isSameDay(nextOccurrence)) {
            nextOccurrence = nextOccurrence.plus(everyXRecurrence, recurrence)
        }
        return nextOccurrence
    }

    fun getCurrentLocalDate(
        @VisibleForTesting
        now: Instant = Clock.System.now(),
    ): LocalDate {
        return now.toLocalDateTime(TimeZone.currentSystemDefault()).date
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
