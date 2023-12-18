package de.dbauer.expensetracker.data

data class UpcomingPaymentData(
    val name: String,
    val price: Float,
    val nextPaymentRemainingDays: Int,
    val nextPaymentDate: String,
)
