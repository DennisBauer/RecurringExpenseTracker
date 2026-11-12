package de.dbauer.expensetracker.web

internal actual fun showStartupError(
    message: String,
    details: String,
) {
    js(
        """
        console.error(details);
        var loading = document.getElementById("loading");
        if (loading) {
            loading.textContent = message;
            loading.style.zIndex = "1";
        }
        """,
    )
}
