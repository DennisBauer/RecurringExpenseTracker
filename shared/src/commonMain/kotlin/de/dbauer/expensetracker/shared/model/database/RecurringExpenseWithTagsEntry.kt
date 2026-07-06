package de.dbauer.expensetracker.shared.model.database

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation

data class RecurringExpenseWithTagsEntry(
    @Embedded val expense: RecurringExpenseEntry,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy =
            Junction(
                value = ExpenseTagCrossRefEntry::class,
                parentColumns = ["expenseId"],
                entityColumns = ["tagId"],
            ),
    )
    val tags: List<TagEntry>,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["expenseId"],
    )
    val reminders: List<ReminderEntry> = emptyList(),
)
