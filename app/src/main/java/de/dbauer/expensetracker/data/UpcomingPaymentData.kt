package de.dbauer.expensetracker.data

import de.dbauer.expensetracker.ui.customizations.ExpenseColor

data class UpcomingPaymentData(
    val id: Int,
    val name: String,
    val price: Float,
    val nextPaymentRemainingDays: Int,
    val nextPaymentDate: String,
    val color: ExpenseColor,
)
