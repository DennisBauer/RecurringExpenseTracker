@file:OptIn(ExperimentalWasmJsInterop::class)

package de.dbauer.expensetracker.web

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun requestPersistentStorage() = jsRequestPersistentStorage()

internal actual fun acquireSingleTabLock(onResult: (acquired: Boolean) -> Unit) =
    jsAcquireSingleTabLock(SINGLE_TAB_LOCK_NAME, onResult)

private fun jsRequestPersistentStorage(): Unit =
    js("{ if (navigator.storage && navigator.storage.persist) { navigator.storage.persist().catch(() => {}); } }")

private fun jsAcquireSingleTabLock(
    lockName: String,
    onResult: (Boolean) -> Unit,
): Unit =
    js(
        """{
        if (!navigator.locks) {
            onResult(true);
        } else {
            let reported = false;
            navigator.locks.request(lockName, { ifAvailable: true }, (lock) => {
                reported = true;
                onResult(lock !== null);
                // Never resolves: the lock stays held until the tab is closed or reloaded.
                return lock !== null ? new Promise(() => {}) : undefined;
            }).catch(() => {
                if (!reported) onResult(true);
            });
        }
    }""",
    )
