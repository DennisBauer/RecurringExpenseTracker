package data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.bottom_nav_home
import recurringexpensetracker.app.generated.resources.bottom_nav_settings
import recurringexpensetracker.app.generated.resources.bottom_nav_upcoming

sealed class BottomNavigation(
    val route: String,
    val name: StringResource,
    val icon: ImageVector,
) {
    data object Home : BottomNavigation("home", Res.string.bottom_nav_home, Icons.Rounded.Home)

    data object Upcoming : BottomNavigation("upcoming", Res.string.bottom_nav_upcoming, Icons.Rounded.Payment)

    data object Settings :
        BottomNavigation("settings", Res.string.bottom_nav_settings, Icons.Rounded.Settings)
}
