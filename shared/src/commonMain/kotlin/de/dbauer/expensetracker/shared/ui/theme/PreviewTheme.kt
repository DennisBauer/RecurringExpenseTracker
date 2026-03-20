package de.dbauer.expensetracker.shared.ui.theme

import androidx.compose.runtime.Composable
import de.dbauer.expensetracker.shared.di.previewModule
import de.dbauer.expensetracker.shared.ui.ThemeMode
import org.koin.compose.KoinApplicationPreview

@Composable
fun ExpenseTrackerThemePreview(
    themeMode: ThemeMode = ThemeMode.FollowSystem,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    KoinApplicationPreview(application = {
        modules(previewModule)
    }) {
        ExpenseTrackerTheme(
            themeMode = themeMode,
            dynamicColor = dynamicColor,
            content = content,
        )
    }
}
