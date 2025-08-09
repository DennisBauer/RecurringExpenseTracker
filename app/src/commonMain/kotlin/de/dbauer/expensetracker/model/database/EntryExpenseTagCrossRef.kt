package de.dbauer.expensetracker.model.database

import androidx.room.Entity

@Entity(primaryKeys = ["expenseId", "tagId"])
data class EntryExpenseTagCrossRef(
    val expenseId: Int,
    val tagId: Int,
)
