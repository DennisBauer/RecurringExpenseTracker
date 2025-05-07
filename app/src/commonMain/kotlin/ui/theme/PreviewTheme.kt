package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import di.previewModule
import org.koin.compose.KoinApplication

@Composable
fun ExpenseTrackerThemePreview(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    KoinApplication(application = {
        // your preview config here
        modules(previewModule)
    }) {
        ExpenseTrackerTheme(
            darkTheme = darkTheme,
            dynamicColor = dynamicColor,
            content = content,
        )
    }
}
