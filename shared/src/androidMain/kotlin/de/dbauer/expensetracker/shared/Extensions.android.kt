package de.dbauer.expensetracker.shared

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException
import java.time.Month
import java.time.format.TextStyle
import java.util.Currency
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.time.Instant

actual fun Float.toCurrencyString(currencyCode: String): String {
    val currencyInstance = NumberFormat.getCurrencyInstance()
    if (currencyCode.isNotEmpty()) {
        try {
            currencyInstance.currency = Currency.getInstance(currencyCode)
        } catch (_: IllegalArgumentException) {
            return "${String.format(Locale.getDefault(), "%.2f", this)} $currencyCode"
        }
    }
    return currencyInstance.format(this)
}

actual fun Float.toLocalString(): String {
    return NumberFormat.getInstance().let {
        it.minimumFractionDigits = 2
        it.format(this)
    }
}

actual fun String.toFloatLocaleAware(): Float? {
    return try {
        NumberFormat.getInstance().parse(this)?.toFloat()
    } catch (e: ParseException) {
        null
    }
}

fun ZipInputStream.forEachEntry(block: (entry: ZipEntry) -> Unit) {
    var entry: ZipEntry?
    while (run {
            entry = nextEntry
            entry
        } != null
    ) {
        try {
            block(entry as ZipEntry)
        } finally {
            this.closeEntry()
        }
    }
}

actual fun Instant.toLocaleString(): String {
    return DateFormat
        .getDateInstance()
        .apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date(this.toEpochMilliseconds()))
}

actual fun LocalDate.toMonthYearStringUTC(): String {
    val locale = Locale.getDefault()
    val month =
        Month
            .of(month.number)
            .getDisplayName(TextStyle.FULL, locale)
    return "$month $year"
}
