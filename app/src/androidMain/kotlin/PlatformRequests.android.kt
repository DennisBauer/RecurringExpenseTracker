import de.dbauer.expensetracker.BuildConfig

actual fun getAppVersion(): String {
    return BuildConfig.VERSION_NAME
}
