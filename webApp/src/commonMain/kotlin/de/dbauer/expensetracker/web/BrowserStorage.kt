package de.dbauer.expensetracker.web

/** Name of the Web Lock held by the tab that owns the database. */
internal const val SINGLE_TAB_LOCK_NAME = "expensetracker-db"

/**
 * Asks the browser to mark the origin's storage as persistent, so the OPFS database is not
 * evicted under storage pressure. Fire-and-forget; browsers may silently decline.
 */
internal expect fun requestPersistentStorage()

/**
 * Tries to acquire the [SINGLE_TAB_LOCK_NAME] Web Lock and holds it for the lifetime of the tab.
 * [onResult] receives `false` if another tab already holds it. Browsers without the Web Locks
 * API report `true`; opening the database then fails with an error in a second tab.
 */
internal expect fun acquireSingleTabLock(onResult: (acquired: Boolean) -> Unit)
