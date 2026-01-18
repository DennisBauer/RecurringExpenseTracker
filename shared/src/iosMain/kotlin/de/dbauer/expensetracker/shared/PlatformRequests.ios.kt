package de.dbauer.expensetracker.shared

import platform.Foundation.NSBundle

actual fun getAppVersion(): String {
    val infoDictionary = NSBundle.mainBundle.infoDictionary
    val version = infoDictionary?.get("CFBundleShortVersionString") as? String
    return version ?: "Unknown"
}

actual fun getAppVersionCode(): Int {
    val versionString = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion") as? String
    return versionString?.toIntOrNull() ?: -1
}
