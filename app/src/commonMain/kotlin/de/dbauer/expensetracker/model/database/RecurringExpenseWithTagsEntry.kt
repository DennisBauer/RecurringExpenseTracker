package de.dbauer.expensetracker.model.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class RecurringExpenseWithTagsEntry(
    @Embedded val expense: RecurringExpenseEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy =
            Junction(
                value = ExpenseTagCrossRefEntry::class,
                parentColumn = "expenseId",
                entityColumn = "tagId",
            ),
    )
    val tags: List<TagEntry>,
)
