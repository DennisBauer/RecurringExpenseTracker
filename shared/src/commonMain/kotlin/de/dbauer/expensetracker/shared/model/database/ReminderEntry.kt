package de.dbauer.expensetracker.shared.model.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = RecurringExpenseEntry::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["expenseId"])],
)
data class ReminderEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "expenseId") val expenseId: Int,
    @ColumnInfo(name = "daysBeforePayment") val daysBeforePayment: Int,
    @ColumnInfo(name = "lastNotificationDate") val lastNotificationDate: Long?,
)
