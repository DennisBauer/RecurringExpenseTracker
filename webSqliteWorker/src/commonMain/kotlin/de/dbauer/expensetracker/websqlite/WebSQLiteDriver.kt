package de.dbauer.expensetracker.websqlite

import androidx.sqlite.SQLiteDriver

/**
 * Creates a [SQLiteDriver] backed by a web worker running SQLite WASM with OPFS persistence.
 *
 * The worker is served from the local npm package `sqlite-wasm-worker` (see `worker/`).
 * OPFS requires the page to be cross-origin isolated (COOP/COEP headers).
 */
expect fun createWebSQLiteDriver(): SQLiteDriver
