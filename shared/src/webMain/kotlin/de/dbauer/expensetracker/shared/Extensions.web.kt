package de.dbauer.expensetracker.shared

import de.dbauer.expensetracker.shared.model.getSystemCurrencyCode
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

/** Region (ISO 3166-1 alpha-2) of the browser locale, or `null` if it cannot be determined. */
internal expect fun browserRegion(): String?

/** Decimal separator followed by the group separator (if any) of the browser locale. */
internal expect fun localeNumberSeparators(): String

actual fun Float.toCurrencyString(currencyCode: String): String {
    // An empty code means "no default currency chosen"; like desktop, use the system currency.
    val code = currencyCode.ifEmpty { getSystemCurrencyCode() }
    return try {
        formatAsCurrency(toDouble(), code)
    } catch (_: Throwable) {
        // Unknown currency code, mirror the desktop fallback.
        "${toLocalString()} $code"
    }
}

actual fun Float.toLocalString(): String = formatAsDecimal(toDouble())

actual fun String.toFloatLocaleAware(): Float? {
    // Intl has no parsing API; use the locale's separators to parse manually.
    val separators = localeNumberSeparators()
    return parseLocalizedFloat(
        this,
        decimalSeparator = separators.first(),
        groupSeparator = separators.getOrNull(1),
    )
}

actual fun Instant.toLocaleString(): String = formatEpochMillisAsUtcDate(toEpochMilliseconds().toDouble())

actual fun LocalDate.toMonthYearStringUTC(): String = formatAsMonthYearUtc(year, month.number)
