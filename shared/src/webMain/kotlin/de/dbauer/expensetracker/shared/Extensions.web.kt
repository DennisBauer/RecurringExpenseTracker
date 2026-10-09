package de.dbauer.expensetracker.shared

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlin.time.Instant

// Implemented per web target because JS interop differs between Kotlin/JS and Kotlin/Wasm.
internal expect fun formatAsCurrency(
    value: Double,
    currencyCode: String,
): String

internal expect fun formatAsDecimal(value: Double): String

internal expect fun formatEpochMillisAsUtcDate(epochMillis: Double): String

internal expect fun formatAsMonthYearUtc(
    year: Int,
    month: Int,
): String

actual fun Float.toCurrencyString(currencyCode: String): String {
    return try {
        formatAsCurrency(toDouble(), currencyCode)
    } catch (_: Throwable) {
        // Unknown currency code, mirror the desktop fallback.
        "${toLocalString()} $currencyCode"
    }
}

actual fun Float.toLocalString(): String = formatAsDecimal(toDouble())

actual fun String.toFloatLocaleAware(): Float? {
    // Intl has no parsing API; accept both '.' and ',' decimal separators.
    val normalized = trim().replace(',', '.')
    return normalized.toFloatOrNull()
}

actual fun Instant.toLocaleString(): String = formatEpochMillisAsUtcDate(toEpochMilliseconds().toDouble())

actual fun LocalDate.toMonthYearStringUTC(): String = formatAsMonthYearUtc(year, month.number)
