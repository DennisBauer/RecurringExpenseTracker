package de.dbauer.expensetracker.data

import de.dbauer.expensetracker.toValueString

data class RecurringExpenseData(
    val id: Int,
    val name: String,
    val description: String,
    val priceValue: Float,
) {
    val priceString = "${priceValue.toValueString()} €" // TODO: Make currency dynamic
}