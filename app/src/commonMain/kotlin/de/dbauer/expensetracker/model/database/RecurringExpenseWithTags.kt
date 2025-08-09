package de.dbauer.expensetracker.model.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class RecurringExpenseWithTags(
    @Embedded val expense: EntryRecurringExpense,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy =
            Junction(
                value = EntryExpenseTagCrossRef::class,
                parentColumn = "expenseId",
                entityColumn = "tagId",
            ),
    )
    val tags: List<EntryTag>,
)
