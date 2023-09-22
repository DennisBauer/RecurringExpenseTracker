package de.erzock.expensetracker

fun Float.toValueString(): String {
    return "%.2f".format(this)
}