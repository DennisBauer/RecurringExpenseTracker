package ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import data.BottomNavigation
import data.HomePane
import data.SettingsPane
import data.UpcomingPane
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.bottom_nav_home
import recurringexpensetracker.app.generated.resources.bottom_nav_settings
import recurringexpensetracker.app.generated.resources.bottom_nav_upcoming

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val bottomNavigationItems =
        listOf(
            BottomNavigation(HomePane.ROUTE, Res.string.bottom_nav_home, Icons.Rounded.Home),
            BottomNavigation(UpcomingPane.ROUTE, Res.string.bottom_nav_upcoming, Icons.Rounded.Payment),
            BottomNavigation(SettingsPane.ROUTE, Res.string.bottom_nav_settings, Icons.Rounded.Settings),
        )

    NavigationBar(modifier = modifier) {
        bottomNavigationItems.forEach { item ->
            val selected = item.route == backStackEntry.value?.destination?.route

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        navController.graph.findStartDestination().route?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
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
