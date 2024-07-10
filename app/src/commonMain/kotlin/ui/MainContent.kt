package ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import data.BottomNavigation
import data.EditExpensePane
import data.EditExpensePane.Companion.getArgExpenseId
import data.HomePane
import data.SettingsPane
import data.UpcomingPane
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.bottom_nav_home
import recurringexpensetracker.app.generated.resources.bottom_nav_settings
import recurringexpensetracker.app.generated.resources.bottom_nav_upcoming
import ui.editexpense.EditRecurringExpenseScreen
import ui.upcomingexpenses.UpcomingPaymentsScreen
import viewmodel.RecurringExpenseViewModel
import viewmodel.UpcomingPaymentsViewModel

@Suppress("ktlint:compose:vm-forwarding-check")
@OptIn(KoinExperimentalAPI::class)
@Composable
fun MainContent(
    isGridMode: Boolean,
    biometricSecurity: Boolean,
    canUseBiometric: Boolean,
    toggleGridMode: () -> Unit,
    onBiometricSecurityChange: (Boolean) -> Unit,
    onClickBackup: () -> Unit,
    onClickRestore: () -> Unit,
    modifier: Modifier = Modifier,
    recurringExpenseViewModel: RecurringExpenseViewModel = koinViewModel<RecurringExpenseViewModel>(),
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel = koinViewModel<UpcomingPaymentsViewModel>(),
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()

    val bottomNavigationItems =
        listOf(
            BottomNavigation(HomePane.ROUTE, Res.string.bottom_nav_home, Icons.Rounded.Home),
            BottomNavigation(UpcomingPane.ROUTE, Res.string.bottom_nav_upcoming, Icons.Rounded.Payment),
            BottomNavigation(SettingsPane.ROUTE, Res.string.bottom_nav_settings, Icons.Rounded.Settings),
        )

    KoinContext {
        Scaffold(
            modifier = modifier,
            bottomBar = {
                if (backStackEntry.value?.destination?.route in
                    listOf(HomePane.ROUTE, UpcomingPane.ROUTE, SettingsPane.ROUTE)
                ) {
                    NavigationBar {
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
            },
            content = { _ ->
                NavHost(
                    navController = navController,
                    startDestination = HomePane.ROUTE,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(HomePane.ROUTE) {
                        RecurringExpenseOverview(
                            weeklyExpense = recurringExpenseViewModel.weeklyExpense,
                            monthlyExpense = recurringExpenseViewModel.monthlyExpense,
                            yearlyExpense = recurringExpenseViewModel.yearlyExpense,
                            recurringExpenseData = recurringExpenseViewModel.recurringExpenseData,
                            isGridMode = isGridMode,
                            onToggleGridMode = toggleGridMode,
                            onCreateNewExpense = {
                                navController.navigate(EditExpensePane().destination)
                            },
                            onEditExpense = {
                                navController.navigate(EditExpensePane(it).destination)
                            },
                            contentPadding =
                                PaddingValues(
                                    top = 8.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ),
                        )
                    }
                    composable(UpcomingPane.ROUTE) {
                        UpcomingPaymentsScreen(
                            upcomingPaymentsViewModel = upcomingPaymentsViewModel,
                            onClickItem = {
                                navController.navigate(EditExpensePane(it.id).destination)
                            },
                            isGridMode = isGridMode,
                            onToggleGridMode = toggleGridMode,
                            onCreateNewExpense = {
                                navController.navigate(EditExpensePane().destination)
                            },
                            contentPadding =
                                PaddingValues(
                                    top = 8.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ),
                        )
                    }
                    composable(SettingsPane.ROUTE) {
                        SettingsScreen(
                            checked = biometricSecurity,
                            onClickBackup = onClickBackup,
                            onClickRestore = onClickRestore,
                            onCheckedChange = onBiometricSecurityChange,
                            canUseBiometric = canUseBiometric,
                        )
                    }
                    composable(
                        route = EditExpensePane.ROUTE,
                        arguments = EditExpensePane.navArguments,
                    ) { backStackEntry ->
                        EditRecurringExpenseScreen(
                            expenseId = backStackEntry.getArgExpenseId(),
                            onDismiss = navController::navigateUp,
                        )
                    }
                }
            },
        )
    }
}

// @Preview
// @Composable
// private fun MainActivityContentPreview() {
//    var isGridMode by remember { mutableStateOf(false) }
//    var biometricSecurity by remember { mutableStateOf(false) }
//    ExpenseTrackerTheme {
//        Surface(
//            modifier = Modifier.fillMaxSize(),
//            color = MaterialTheme.colorScheme.background,
//        ) {
//            MainContent(
//                weeklyExpense = "4,00 €",
//                monthlyExpense = "16,00 €",
//                yearlyExpense = "192,00 €",
//                recurringExpenseData =
//                    listOf(
//                        RecurringExpenseData(
//                            id = 0,
//                            name = "Netflix",
//                            description = "My Netflix description",
//                            price = 9.99f,
//                            monthlyPrice = 9.99f,
//                            everyXRecurrence = 1,
//                            recurrence = Recurrence.Monthly,
//                            Clock.System.now(),
//                            ExpenseColor.Dynamic,
//                        ),
//                        RecurringExpenseData(
//                            id = 1,
//                            name = "Disney Plus",
//                            description = "My Disney Plus description",
//                            price = 5f,
//                            monthlyPrice = 5f,
//                            everyXRecurrence = 1,
//                            recurrence = Recurrence.Monthly,
//                            Clock.System.now(),
//                            ExpenseColor.Red,
//                        ),
//                        RecurringExpenseData(
//                            id = 2,
//                            name = "Amazon Prime",
//                            description = "My Disney Plus description",
//                            price = 7.95f,
//                            monthlyPrice = 7.95f,
//                            everyXRecurrence = 1,
//                            recurrence = Recurrence.Monthly,
//                            Clock.System.now(),
//                            ExpenseColor.Blue,
//                        ),
//                    ),
//                onRecurringExpenseAdd = {},
//                onRecurringExpenseEdit = {},
//                onRecurringExpenseDelete = {},
//                onClickBackup = { },
//                onClickRestore = { },
//                upcomingPaymentsViewModel = UpcomingPaymentsViewModel(null),
//                isGridMode = isGridMode,
//                toggleGridMode = { isGridMode = !isGridMode },
//                biometricSecurity = biometricSecurity,
//                onBiometricSecurityChange = { biometricSecurity = it },
//                canUseBiometric = true,
//            )
//        }
//    }
// }
