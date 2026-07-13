package de.dbauer.expensetracker.shared.model

actual fun getSystemCurrencyCode(): String {
    // Browsers expose no reliable system currency; same fallback as iOS.
    return "USD"
}
