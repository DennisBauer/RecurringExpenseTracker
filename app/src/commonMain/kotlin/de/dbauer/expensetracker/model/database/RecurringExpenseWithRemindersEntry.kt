package de.dbauer.expensetracker.model.database

import androidx.room.Embedded
import androidx.room.Relation

data class RecurringExpenseWithRemindersEntry(
    @Embedded val expense: RecurringExpenseEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "expenseId",
    )
    val reminders: List<ReminderEntry>,
)
