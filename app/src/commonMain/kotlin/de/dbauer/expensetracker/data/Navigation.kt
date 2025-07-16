package de.dbauer.expensetracker.data

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource

data class BottomNavigation(
    val route: Any,
    val name: StringResource,
    val icon: ImageVector,
)

@Serializable
sealed interface MainNavRoute

@Serializable
object HomePane : MainNavRoute

@Serializable
object UpcomingPane : MainNavRoute

@Serializable
object SettingsPane : MainNavRoute

@Serializable
object SettingsPaneAbout

@Serializable
object SettingsPaneLibraries

@Serializable
object SettingsPaneDefaultCurrency

@Serializable
class EditExpensePane(val expenseId: Int? = null)

inline fun <reified T : Any> NavDestination?.isInRoute(vararg routes: T): Boolean {
    return routes.any { route -> this?.hasRoute(route::class) == true }
}
