package de.dbauer.expensetracker.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import de.dbauer.expensetracker.data.BottomNavigation
import de.dbauer.expensetracker.data.HomePane
import de.dbauer.expensetracker.data.MainNavRoute
import de.dbauer.expensetracker.data.NavRoute
import de.dbauer.expensetracker.data.SettingsPane
import de.dbauer.expensetracker.data.UpcomingPane
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.bottom_nav_home
import recurringexpensetracker.app.generated.resources.bottom_nav_settings
import recurringexpensetracker.app.generated.resources.bottom_nav_upcoming

@Composable
fun BottomNavBar(
    backStackTop: NavRoute,
    onClick: (NavRoute) -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomNavigationItems =
        listOf(
            BottomNavigation(NavRoute.HomePane, Res.string.bottom_nav_home, Icons.Rounded.Home),
            BottomNavigation(NavRoute.UpcomingPane, Res.string.bottom_nav_upcoming, Icons.Rounded.Payment),
            BottomNavigation(NavRoute.SettingsPane, Res.string.bottom_nav_settings, Icons.Rounded.Settings),
        )

    NavigationBar(modifier = modifier) {
        bottomNavigationItems.forEach { item ->
            val selected = backStackTop == item.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    onClick(item.route)
//                    navController.navigate(item.route) {
//                        // Pop up to the start destination of the graph to
//                        // avoid building up a large stack of destinations
//                        // on the back stack as users select items
//                        navController.graph.findStartDestination().route?.let { route ->
//                            popUpTo(route) {
//                                saveState = true
//                            }
//                        }
//                        // Avoid multiple copies of the same destination when
//                        // reselecting the same item
//                        launchSingleTop = true
//                        // Restore state when reselecting a previously selected item
//                        restoreState = true
//                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                    )
                },
                label = {
                    Text(text = stringResource(item.name))
                },
            )
        }
    }
}
