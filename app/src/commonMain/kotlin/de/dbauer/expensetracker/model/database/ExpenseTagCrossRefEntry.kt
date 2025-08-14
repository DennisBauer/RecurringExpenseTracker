package de.dbauer.expensetracker.model.database

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "ExpenseTagCrossRef",
    primaryKeys = ["expenseId", "tagId"],
    indices = [
        Index(value = ["tagId"]),
        Index(value = ["expenseId"]),
    ],
)
data class ExpenseTagCrossRefEntry(
    val expenseId: Int,
    val tagId: Int,
)
