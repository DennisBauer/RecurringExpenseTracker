package de.dbauer.expensetracker.data

data class RecurringExpenseData(
    val id: Int,
    val name: String,
    val description: String,
    val price: Float,
)
