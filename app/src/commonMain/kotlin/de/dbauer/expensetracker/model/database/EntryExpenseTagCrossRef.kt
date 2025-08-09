package de.dbauer.expensetracker.model.database

import androidx.room.Entity

@Entity(tableName = "ExpenseTagCrossRef", primaryKeys = ["expenseId", "tagId"])
internal data class EntryExpenseTagCrossRef(
    val expenseId: Int,
    val tagId: Int,
)
