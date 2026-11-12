package de.dbauer.expensetracker.web

internal const val STARTUP_ERROR_MESSAGE = "Failed to start the app. Please reload the page."

/**
 * Logs [details] to the browser console and replaces the "Loading…" text of index.html with
 * [message]. Uses plain DOM instead of Compose, as Compose itself may be what failed to start.
 */
internal expect fun showStartupError(
    message: String,
    details: String,
)
