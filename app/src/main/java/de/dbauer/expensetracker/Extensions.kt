package de.dbauer.expensetracker

import java.text.NumberFormat

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
