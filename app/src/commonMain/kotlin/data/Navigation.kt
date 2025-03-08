package data

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.navArgument
import org.jetbrains.compose.resources.StringResource

data class BottomNavigation(
    val route: String,
    val name: StringResource,
    val icon: ImageVector,
)

object HomePane {
    const val ROUTE: String = "Home"
}

object UpcomingPane {
    const val ROUTE: String = "Upcoming"
}

object SettingsPane {
    const val ROUTE: String = "Settings"
}

object AboutPane {
    const val ROUTE: String = "About"
}

object AboutLibsPane {
    const val ROUTE: String = "AboutLibs"
}

class EditExpensePane(expenseId: Int? = null) {
    val destination: String = ROUTE.replace("{$ARG_EXPENSE_ID}", expenseId.toString())

    companion object {
        const val ARG_EXPENSE_ID = "expenseId"
        const val ROUTE: String = "EditExpense?$ARG_EXPENSE_ID={$ARG_EXPENSE_ID}"
        val navArguments = listOf(navArgument(ARG_EXPENSE_ID) { nullable = true })

        fun NavBackStackEntry.getArgExpenseId(): Int? {
            return arguments?.getString(ARG_EXPENSE_ID)?.toInt()
        }
    }
}
