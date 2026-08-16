@file:OptIn(ExperimentalWasmJsInterop::class)

package de.dbauer.expensetracker.websqlite

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker
import kotlin.js.ExperimentalWasmJsInterop

actual fun createWebSQLiteDriver(): SQLiteDriver = WebWorkerSQLiteDriver(jsWorker())

// The whole Worker construction must live in a single js() block: wasmJs interop
// cannot pass a raw JS URL object into the Kotlin Worker constructor.
private fun jsWorker(): Worker = js("""new Worker(new URL("sqlite-wasm-worker/worker.js", import.meta.url))""")
