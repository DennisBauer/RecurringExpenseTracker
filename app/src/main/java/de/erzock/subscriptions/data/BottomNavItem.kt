package de.erzock.subscriptions.data

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val name: String,
    val route: NavigationRoute,
    val icon: ImageVector,
)