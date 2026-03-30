package de.dbauer.expensetracker.shared.data

data class UpcomingPaymentData(
    val id: Int,
    val name: String,
    val price: CurrencyValue,
    val nextPaymentRemainingDays: Int,
    val nextPaymentDate: String,
    val tags: List<Tag>,
    val requiresConfirmation: Boolean = false,
    val isPaid: Boolean = false,
    val paymentDateEpoch: Long = 0L,
) {
    val isOverdue: Boolean
        get() = nextPaymentRemainingDays < 0 && !isPaid && requiresConfirmation
}
