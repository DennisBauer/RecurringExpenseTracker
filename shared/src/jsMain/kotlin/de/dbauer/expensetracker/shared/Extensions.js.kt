package de.dbauer.expensetracker.shared

internal actual fun formatAsCurrency(
    value: Double,
    currencyCode: String,
): String {
    val code = currencyCode.ifEmpty { "USD" }
    return js("new Intl.NumberFormat(undefined, { style: 'currency', currency: code }).format(value)") as String
}

internal actual fun formatAsDecimal(value: Double): String {
    return js("new Intl.NumberFormat(undefined, { minimumFractionDigits: 2 }).format(value)") as String
}

internal actual fun formatEpochMillisAsUtcDate(epochMillis: Double): String {
    return js(
        "new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeZone: 'UTC' })" +
            ".format(new Date(epochMillis))",
    ) as String
}

internal actual fun formatAsMonthYearUtc(
    year: Int,
    month: Int,
): String {
    return js(
        "new Intl.DateTimeFormat(undefined, { month: 'long', year: 'numeric', timeZone: 'UTC' })" +
            ".format(new Date(Date.UTC(year, month - 1, 1)))",
    ) as String
}
