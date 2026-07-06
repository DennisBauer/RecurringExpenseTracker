package de.dbauer.expensetracker.shared.model.database

import androidx.room3.Entity
import androidx.room3.Index

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
