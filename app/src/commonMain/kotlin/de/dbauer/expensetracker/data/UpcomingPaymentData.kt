package de.dbauer.expensetracker.data

data class UpcomingPaymentData(
    val id: Int,
    val name: String,
    val price: CurrencyValue,
    val nextPaymentRemainingDays: Int,
    val nextPaymentDate: String,
)
