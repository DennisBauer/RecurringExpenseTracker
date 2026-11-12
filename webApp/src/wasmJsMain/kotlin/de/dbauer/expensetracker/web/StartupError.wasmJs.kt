@file:OptIn(ExperimentalWasmJsInterop::class)

package de.dbauer.expensetracker.web

import kotlin.js.ExperimentalWasmJsInterop

internal actual fun showStartupError(
    message: String,
    details: String,
) = jsShowStartupError(message, details)

private fun jsShowStartupError(
    message: String,
    details: String,
): Unit =
    js(
        """{
        console.error(details);
        const loading = document.getElementById("loading");
        if (loading) {
            loading.textContent = message;
            loading.style.zIndex = "1";
        }
    }""",
    )
