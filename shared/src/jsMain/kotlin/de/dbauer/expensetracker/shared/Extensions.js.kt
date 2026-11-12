package de.dbauer.expensetracker.shared

// Note: js() code wraps `new X(...)` in parentheses before calling members on it; the Kotlin/JS
// compiler otherwise parses `new X(...).m()` as `new (X(...).m)()`.

internal actual fun formatAsCurrency(
    value: Double,
    currencyCode: String,
): String {
    val code = currencyCode
    return js("(new Intl.NumberFormat(undefined, { style: 'currency', currency: code })).format(value)") as String
}

internal actual fun formatAsDecimal(value: Double): String {
    return js("(new Intl.NumberFormat(undefined, { minimumFractionDigits: 2 })).format(value)") as String
}

internal actual fun formatEpochMillisAsUtcDate(epochMillis: Double): String {
    return js(
        "(new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeZone: 'UTC' }))" +
            ".format(new Date(epochMillis))",
    ) as String
}

internal actual fun formatAsMonthYearUtc(
    year: Int,
    month: Int,
): String {
    return js(
        "(new Intl.DateTimeFormat(undefined, { month: 'long', year: 'numeric', timeZone: 'UTC' }))" +
            ".format(new Date(Date.UTC(year, month - 1, 1)))",
    ) as String
}

internal actual fun localeNumberSeparators(): String {
    return js(
        """
        (function () {
            var decimal = '.';
            var group = '';
            (new Intl.NumberFormat()).formatToParts(1234567.5).forEach(function (part) {
                if (part.type === 'decimal') decimal = part.value;
                if (part.type === 'group') group = part.value;
            });
            return decimal + group;
        })()
        """,
    ) as String
}

internal actual fun browserRegion(): String? {
    return js(
        """
        (function () {
            try {
                return (new Intl.Locale(navigator.language)).maximize().region || null;
            } catch (e) {
                return null;
            }
        })()
        """,
    ) as String?
}
