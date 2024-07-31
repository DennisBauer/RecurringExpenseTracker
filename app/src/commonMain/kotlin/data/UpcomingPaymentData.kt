package data

import ui.customizations.ExpenseColor

data class UpcomingPaymentData(
    val id: Int,
    val name: String,
    val price: CurrencyValue,
    val nextPaymentRemainingDays: Int,
    val nextPaymentDate: String,
    val color: ExpenseColor,
)
