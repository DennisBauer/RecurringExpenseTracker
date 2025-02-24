package model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import model.DateTimeCalculator

@Entity(tableName = "recurring_expenses")
data class RecurringExpense(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "price") val price: Float?,
    @ColumnInfo(name = "everyXRecurrence") val everyXRecurrence: Int?,
    @ColumnInfo(name = "recurrence") val recurrence: Int?,
    @ColumnInfo(name = "firstPayment") val firstPayment: Long?,
    @ColumnInfo(name = "color") val color: Int?,
    @ColumnInfo(name = "currencyCode") val currencyCode: String,
    @ColumnInfo(name = "notifyForExpense") val notifyForExpense: Boolean,
    @ColumnInfo(name = "notifyXDaysBefore") val notifyXDaysBefore: Int?,
    @ColumnInfo(name = "lastNotificationDate") val lastNotificationDate: Long?,
) {
    fun getMonthlyPrice(): Float {
        return when (recurrence) {
            RecurrenceDatabase.Daily.value -> {
                (365 / 12f) / everyXRecurrence!! * price!!
            }
            RecurrenceDatabase.Weekly.value -> {
                (52 / 12f) / everyXRecurrence!! * price!!
            }
            RecurrenceDatabase.Monthly.value -> {
                1f / everyXRecurrence!! * price!!
            }
            RecurrenceDatabase.Yearly.value -> {
                price!! / (everyXRecurrence!! * 12f)
            }
            else -> 0f
        }
    }

    fun getNextPaymentDay(): LocalDate? {
        if (firstPayment == null) return null
        if (everyXRecurrence == null) return null

        return DateTimeCalculator.getDayOfNextOccurrenceFromNow(
            from = Instant.fromEpochMilliseconds(firstPayment),
            everyXRecurrence = everyXRecurrence,
            recurrence =
                when (recurrence) {
                    RecurrenceDatabase.Daily.value -> DateTimeUnit.DAY
                    RecurrenceDatabase.Weekly.value -> DateTimeUnit.WEEK
                    RecurrenceDatabase.Monthly.value -> DateTimeUnit.MONTH
                    RecurrenceDatabase.Yearly.value -> DateTimeUnit.YEAR
                    else -> DateTimeUnit.MONTH
                },
        )
    }
}

enum class RecurrenceDatabase(
    val value: Int,
) {
    Daily(1),
    Weekly(2),
    Monthly(3),
    Yearly(4),
}
