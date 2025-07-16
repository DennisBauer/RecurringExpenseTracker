package de.dbauer.expensetracker

actual fun getAppVersion(): String {
    return BuildConfig.VERSION_NAME
}
