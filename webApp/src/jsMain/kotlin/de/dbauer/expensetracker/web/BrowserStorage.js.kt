package de.dbauer.expensetracker.web

internal actual fun requestPersistentStorage() {
    js(
        "if (navigator.storage && navigator.storage.persist) { navigator.storage.persist().catch(function () {}); }",
    )
}

internal actual fun acquireSingleTabLock(onResult: (acquired: Boolean) -> Unit) {
    val lockName = SINGLE_TAB_LOCK_NAME
    js(
        """
        if (!navigator.locks) {
            onResult(true);
        } else {
            var reported = false;
            navigator.locks.request(lockName, { ifAvailable: true }, function (lock) {
                reported = true;
                onResult(lock !== null);
                // Never resolves: the lock stays held until the tab is closed or reloaded.
                return lock !== null ? new Promise(function () {}) : undefined;
            }).catch(function () {
                if (!reported) onResult(true);
            });
        }
        """,
    )
}
