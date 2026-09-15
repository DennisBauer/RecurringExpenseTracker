package de.dbauer.expensetracker.shared

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Web targets have no IO dispatcher; everything runs on the single-threaded default.
actual val ioDispatcher: CoroutineDispatcher = Dispatchers.Default
