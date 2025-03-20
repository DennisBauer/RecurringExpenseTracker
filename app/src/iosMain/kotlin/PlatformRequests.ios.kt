import platform.Foundation.NSBundle

actual fun getAppVersion(): String {
    val infoDictionary = NSBundle.mainBundle.infoDictionary
    val version = infoDictionary?.get("CFBundleShortVersionString") as? String
    return version ?: "Unknown"
}
