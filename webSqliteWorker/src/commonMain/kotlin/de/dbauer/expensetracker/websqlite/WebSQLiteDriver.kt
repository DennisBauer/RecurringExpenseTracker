package de.dbauer.expensetracker.websqlite

import androidx.sqlite.SQLiteDriver

/**
 * Creates a [SQLiteDriver] backed by a web worker running SQLite WASM with OPFS persistence.
 *
 * The worker is served from the local npm package `sqlite-wasm-worker` (see `worker/`).
 * Storage uses the `opfs-sahpool` VFS, which needs no COOP/COEP headers but allows only one
 * open instance (tab) per origin.
 */
expect fun createWebSQLiteDriver(): SQLiteDriver
