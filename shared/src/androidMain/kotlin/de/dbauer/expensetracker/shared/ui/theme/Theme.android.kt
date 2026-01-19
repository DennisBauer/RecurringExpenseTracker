package de.dbauer.expensetracker.shared.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import de.dbauer.expensetracker.shared.ui.ThemeMode

@Composable
actual fun ExpenseTrackerTheme(
    themeMode: ThemeMode,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean,
    content: @Composable () -> Unit,
) {
    val darkTheme =
        when (themeMode) {
            ThemeMode.FollowSystem -> isSystemInDarkTheme()
            ThemeMode.Dark, ThemeMode.Amoled -> true
            ThemeMode.Light -> false
        }

    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                darkColorScheme
            }

            else -> {
                lightColorScheme
            }
        }.let { baseColorScheme ->
            if (themeMode == ThemeMode.Amoled) {
                baseColorScheme.toAmoledColorScheme()
            } else {
                baseColorScheme
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
