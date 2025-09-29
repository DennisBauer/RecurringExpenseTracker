package de.dbauer.expensetracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import de.dbauer.expensetracker.ui.ThemeMode

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
            darkTheme -> darkColorScheme
            else -> lightColorScheme
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
