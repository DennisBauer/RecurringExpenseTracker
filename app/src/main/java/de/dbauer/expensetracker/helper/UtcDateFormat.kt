package de.dbauer.expensetracker.helper

import java.text.DateFormat
import java.util.TimeZone

object UtcDateFormat {
    fun getDateInstance(): DateFormat {
        return DateFormat.getDateInstance().apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
}
