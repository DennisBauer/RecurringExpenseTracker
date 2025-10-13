package de.dbauer.expensetracker.data

import kotlin.time.Instant

data class Reminder(
    val id: Int = 0,
    val daysBeforePayment: Int,
    val lastNotificationDate: Instant? = null,
)
