package de.dbauer.expensetracker.model

import java.text.NumberFormat

actual fun getSystemCurrencyCode(): String {
    val currencyInstance = NumberFormat.getCurrencyInstance()
    return currencyInstance.currency?.currencyCode ?: "USD"
}
