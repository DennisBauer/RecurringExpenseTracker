package de.dbauer.expensetracker.shared.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.bottom_nav_home
import recurringexpensetracker.shared.generated.resources.bottom_nav_settings
import recurringexpensetracker.shared.generated.resources.bottom_nav_upcoming
import recurringexpensetracker.shared.generated.resources.edit_expense_title
import recurringexpensetracker.shared.generated.resources.home_title
import recurringexpensetracker.shared.generated.resources.settings_title
import recurringexpensetracker.shared.generated.resources.tags_screen_title
import recurringexpensetracker.shared.generated.resources.tags_title
import recurringexpensetracker.shared.generated.resources.upcoming_title
import recurringexpensetracker.shared.generated.resources.whats_new_title

sealed class NavRoute(val title: StringResource, val showBackAction: Boolean) {
    @OptIn(ExperimentalMaterial3Api::class)
    data object HomePane : NavRoute(title = Res.string.home_title, showBackAction = false)

    data object UpcomingPane : NavRoute(title = Res.string.upcoming_title, showBackAction = false)

    data object SettingsPane : NavRoute(title = Res.string.settings_title, showBackAction = false)

    data class EditExpensePane(val expenseId: Int? = null) : NavRoute(
        title = Res.string.edit_expense_title,
        showBackAction = true,
    )

    data object WhatsNew : NavRoute(title = Res.string.whats_new_title, showBackAction = true)

    data object TagsPane : NavRoute(title = Res.string.tags_screen_title, showBackAction = true)
}

data class BottomNavigation(
    val route: NavRoute,
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

@Serializable
object TagsPane

@Serializable
object WhatsNew

inline fun <reified T : Any> NavDestination?.isInRoute(vararg routes: T): Boolean {
    return routes.any { route -> this?.hasRoute(route::class) == true }
}
