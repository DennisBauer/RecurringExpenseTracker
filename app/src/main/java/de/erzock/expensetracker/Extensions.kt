package de.erzock.expensetracker

fun Float.toValueString(): String {
    return "%.2f".format(this)
}

fun String.toFloatIgnoreSeparator(): Float {
    val converted = replace(",", ".")
    return converted.toFloat()
}