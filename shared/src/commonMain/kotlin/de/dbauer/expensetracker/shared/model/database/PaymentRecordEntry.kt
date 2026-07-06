package de.dbauer.expensetracker.shared.model.database

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "payment_records",
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
data class PaymentRecordEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "expenseId") val expenseId: Int,
    @ColumnInfo(name = "paymentDate") val paymentDate: Long,
)
