package de.dbauer.expensetracker.websqlite

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

actual fun createWebSQLiteDriver(): SQLiteDriver =
    WebWorkerSQLiteDriver(
        Worker(js("""new URL("sqlite-wasm-worker/worker.js", import.meta.url)""")),
    )
