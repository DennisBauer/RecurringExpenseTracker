package de.dbauer.expensetracker.model.database

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class RecurringExpenseWithTags(
    @Embedded val expense: RecurringExpense,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy =
            Junction(
                value = ExpenseTagCrossRef::class,
                parentColumn = "expenseId",
                entityColumn = "tagId",
            ),
    )
    val tags: List<Tag>,
)
