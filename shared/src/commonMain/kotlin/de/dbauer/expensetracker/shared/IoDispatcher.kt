package de.dbauer.expensetracker.shared

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Dispatcher for IO-bound work. Maps to [kotlinx.coroutines.Dispatchers.IO] on platforms that
 * have it and to the default dispatcher on web targets, which have no IO dispatcher.
 */
expect val ioDispatcher: CoroutineDispatcher
