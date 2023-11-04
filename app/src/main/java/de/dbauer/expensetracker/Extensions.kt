package de.dbauer.expensetracker

import java.text.NumberFormat
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

fun String.toFloatIgnoreSeparator(): Float {
    val converted = replace(",", ".")
    return converted.toFloat()
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
