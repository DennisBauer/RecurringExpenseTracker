package de.dbauer.expensetracker

import androidx.compose.ui.Modifier
import java.text.NumberFormat
import java.text.ParseException
import java.util.Calendar
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun Float.toCurrencyString(): String {
    return NumberFormat.getCurrencyInstance().format(this)
}

fun Float.toLocalString(): String {
    return NumberFormat.getInstance().let {
        it.minimumFractionDigits = 2
        it.format(this)
    }
}

fun String.toFloatLocaleAware(): Float? {
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

fun Calendar.isSameDay(other: Calendar): Boolean {
    return this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
        this.get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
        this.get(Calendar.DAY_OF_MONTH) == other.get(Calendar.DAY_OF_MONTH)
}

fun Calendar.isInDaysAfter(other: Calendar): Boolean {
    if (this.get(Calendar.YEAR) > other.get(Calendar.YEAR)) {
        return true
    } else if (this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
        this.get(Calendar.MONTH) > other.get(Calendar.MONTH)
    ) {
        return true
    } else if (this.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
        this.get(Calendar.MONTH) == other.get(Calendar.MONTH) &&
        this.get(Calendar.DAY_OF_MONTH) > other.get(Calendar.DAY_OF_MONTH)
    ) {
        return true
    }
    return false
}

fun Modifier.conditional(
    condition: Boolean,
    modifier: Modifier.() -> Modifier,
): Modifier {
    return if (condition) {
        then(modifier(Modifier))
    } else {
        this
    }
}
