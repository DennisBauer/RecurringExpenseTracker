@file:OptIn(ExperimentalWasmJsInterop::class)

package de.dbauer.expensetracker.shared

import kotlin.js.ExperimentalWasmJsInterop

private fun jsFormatAsCurrency(
    value: Double,
    code: String,
): String = js("new Intl.NumberFormat(undefined, { style: 'currency', currency: code }).format(value)")

private fun jsFormatAsDecimal(value: Double): String =
    js("new Intl.NumberFormat(undefined, { minimumFractionDigits: 2 }).format(value)")

private fun jsFormatEpochMillisAsUtcDate(epochMillis: Double): String =
    js(
        """new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeZone: 'UTC' })
            .format(new Date(epochMillis))""",
    )

private fun jsFormatAsMonthYearUtc(
    year: Int,
    month: Int,
): String =
    js(
        """new Intl.DateTimeFormat(undefined, { month: 'long', year: 'numeric', timeZone: 'UTC' })
            .format(new Date(Date.UTC(year, month - 1, 1)))""",
    )

internal actual fun formatAsCurrency(
    value: Double,
    currencyCode: String,
): String = jsFormatAsCurrency(value, currencyCode.ifEmpty { "USD" })

internal actual fun formatAsDecimal(value: Double): String = jsFormatAsDecimal(value)

internal actual fun formatEpochMillisAsUtcDate(epochMillis: Double): String =
    jsFormatEpochMillisAsUtcDate(epochMillis)

internal actual fun formatAsMonthYearUtc(
    year: Int,
    month: Int,
): String = jsFormatAsMonthYearUtc(year, month)
