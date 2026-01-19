package de.dbauer.expensetracker.shared.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import de.dbauer.expensetracker.shared.ui.ThemeMode
import de.dbauer.expensetracker.shared.ui.theme.darkColorScheme
import de.dbauer.expensetracker.shared.ui.theme.lightColorScheme
import de.dbauer.expensetracker.shared.ui.theme.toAmoledColorScheme

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
