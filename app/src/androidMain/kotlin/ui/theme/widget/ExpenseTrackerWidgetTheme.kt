package ui.theme.widget

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.glance.GlanceTheme
import androidx.glance.material3.ColorProviders
import ui.theme.darkColorScheme
import ui.theme.lightColorScheme

@Composable
fun ExpenseTrackerWidgetTheme(
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    GlanceTheme(
        colors =
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GlanceTheme.colors
            } else {
                ColorProviders(
                    light = lightColorScheme,
                    dark = darkColorScheme,
                )
            },
        content = content,
    )
}
