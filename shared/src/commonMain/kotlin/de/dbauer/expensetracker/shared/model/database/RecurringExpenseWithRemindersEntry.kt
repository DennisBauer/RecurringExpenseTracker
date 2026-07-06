package de.dbauer.expensetracker.shared.model.database

import androidx.room3.Embedded
import androidx.room3.Relation

data class RecurringExpenseWithRemindersEntry(
    @Embedded val expense: RecurringExpenseEntry,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["expenseId"],
    )
    val reminders: List<ReminderEntry>,
)
