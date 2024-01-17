package de.dbauer.expensetracker.data

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import de.dbauer.expensetracker.R

sealed class BottomNavigation(
    val route: String,
    @StringRes val name: Int,
    val icon: ImageVector,
) {
    data object Home : BottomNavigation("home", R.string.bottom_nav_home, Icons.Rounded.Home)

    data object Upcoming : BottomNavigation("upcoming", R.string.bottom_nav_upcoming, Icons.Rounded.Payment)

    data object Settings :
        BottomNavigation("settings", R.string.bottom_nav_settings, Icons.Rounded.Settings)
}
