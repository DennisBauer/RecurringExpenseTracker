package de.dbauer.expensetracker

import android.content.Context
import android.os.Build
import org.koin.java.KoinJavaComponent.get

actual fun getAppVersion(): String {
    return BuildConfig.VERSION_NAME
}

actual fun getAppVersionCode(): Int {
    return try {
        val context: Context = get(Context::class.java)
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }
    } catch (_: Exception) {
        -1
    }
}
