package de.erzock.expensetracker.data

import de.erzock.expensetracker.toValueString

data class RecurringExpenseData(
    val name: String,
    val description: String,
    val priceValue: Float,
) {
    val priceString = "${priceValue.toValueString()} €" // TODO: Make currency dynamic
}