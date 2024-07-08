package ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.bottom_nav_home
import recurringexpensetracker.app.generated.resources.bottom_nav_settings
import recurringexpensetracker.app.generated.resources.bottom_nav_upcoming
import recurringexpensetracker.app.generated.resources.edit_expense_title
import recurringexpensetracker.app.generated.resources.home_add_expense_fab_content_description
import recurringexpensetracker.app.generated.resources.home_title
import recurringexpensetracker.app.generated.resources.settings_title
import recurringexpensetracker.app.generated.resources.top_app_bar_icon_button_grid_close_content_desc
import recurringexpensetracker.app.generated.resources.top_app_bar_icon_button_grid_open_content_desc
import recurringexpensetracker.app.generated.resources.upcoming_title
import ui.editexpense.EditRecurringExpenseScreen
import ui.upcomingexpenses.UpcomingPaymentsScreen
import viewmodel.EditRecurringExpenseViewModel
import viewmodel.RecurringExpenseViewModel
import viewmodel.UpcomingPaymentsViewModel
import viewmodel.database.ExpenseRepository

@Suppress("ktlint:compose:vm-forwarding-check")
@OptIn(ExperimentalMaterial3Api::class, KoinExperimentalAPI::class)
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

    val titleRes by remember {
        derivedStateOf {
            when (backStackEntry.value?.destination?.route) {
                HomePane.ROUTE -> Res.string.home_title
                UpcomingPane.ROUTE -> Res.string.upcoming_title
                SettingsPane.ROUTE -> Res.string.settings_title
                EditExpensePane.ROUTE -> Res.string.edit_expense_title
                else -> Res.string.home_title
            }
        }
    }

    val homeScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val upcomingScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val settingsScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val editExpenseScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val topAppBarScrollBehavior by remember {
        derivedStateOf {
            when (backStackEntry.value?.destination?.route) {
                HomePane.ROUTE -> homeScrollBehavior
                UpcomingPane.ROUTE -> upcomingScrollBehavior
                SettingsPane.ROUTE -> settingsScrollBehavior
                EditExpensePane.ROUTE -> editExpenseScrollBehavior
                else -> homeScrollBehavior
            }
        }
    }

    val bottomNavigationItems =
        listOf(
            BottomNavigation(HomePane.ROUTE, Res.string.bottom_nav_home, Icons.Rounded.Home),
            BottomNavigation(UpcomingPane.ROUTE, Res.string.bottom_nav_upcoming, Icons.Rounded.Payment),
            BottomNavigation(SettingsPane.ROUTE, Res.string.bottom_nav_settings, Icons.Rounded.Settings),
        )

    KoinContext {
        Scaffold(
            modifier = modifier,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(titleRes),
                        )
                    },
                    navigationIcon = {
                        if (backStackEntry.value?.destination?.route == EditExpensePane.ROUTE) {
                            IconButton(
                                onClick = {
                                    navController.navigateUp()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                )
                            }
                        }
                    },
                    actions = {
                        if (backStackEntry.value?.destination?.route in
                            listOf(
                                HomePane.ROUTE,
                                UpcomingPane.ROUTE,
                            )
                        ) {
                            IconButton(
                                onClick = {
                                    toggleGridMode()
                                    // Because of the [AnimatedContent] in [RecurringExpenseOverview] the list is
                                    // reset and scrolled back to the top. To make sure the scroll state matches
                                    // that we need to reset it here. It make the TopAppBar use the surface
                                    // color again. This is a workaround which can hopefully removed in the near
                                    // future.
                                    homeScrollBehavior.state.contentOffset = 0f
                                },
                            ) {
                                Icon(
                                    imageVector =
                                        if (isGridMode) Icons.Filled.TableRows else Icons.Filled.GridView,
                                    contentDescription =
                                        if (isGridMode) {
                                            stringResource(
                                                Res.string.top_app_bar_icon_button_grid_close_content_desc,
                                            )
                                        } else {
                                            stringResource(
                                                Res.string.top_app_bar_icon_button_grid_open_content_desc,
                                            )
                                        },
                                )
                            }
                        }
                    },
                    scrollBehavior = topAppBarScrollBehavior,
                )
            },
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
            floatingActionButton = {
                if (backStackEntry.value?.destination?.route in listOf(HomePane.ROUTE, UpcomingPane.ROUTE)) {
                    FloatingActionButton(
                        onClick = {
                            navController.navigate(EditExpensePane().destination)
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription =
                                stringResource(Res.string.home_add_expense_fab_content_description),
                        )
                    }
                }
            },
            content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = HomePane.ROUTE,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                ) {
                    composable(HomePane.ROUTE) {
                        RecurringExpenseOverview(
                            weeklyExpense = recurringExpenseViewModel.weeklyExpense,
                            monthlyExpense = recurringExpenseViewModel.monthlyExpense,
                            yearlyExpense = recurringExpenseViewModel.yearlyExpense,
                            recurringExpenseData = recurringExpenseViewModel.recurringExpenseData,
                            onClickItem = {
                                navController.navigate(EditExpensePane(it.id).destination)
                            },
                            isGridMode = isGridMode,
                            modifier =
                                Modifier
                                    .nestedScroll(homeScrollBehavior.nestedScrollConnection),
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
                            modifier =
                                Modifier
                                    .nestedScroll(upcomingScrollBehavior.nestedScrollConnection),
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
                            modifier = Modifier.nestedScroll(settingsScrollBehavior.nestedScrollConnection),
                        )
                    }
                    composable(
                        route = EditExpensePane.ROUTE,
                        arguments = EditExpensePane.navArguments,
                    ) { backStackEntry ->
                        val expenseId = backStackEntry.getArgExpenseId()
                        val expenseRepository = koinInject<ExpenseRepository>()
                        val viewModel = viewModel { EditRecurringExpenseViewModel(expenseId, expenseRepository) }
                        EditRecurringExpenseScreen(
                            viewModel = viewModel,
                            onDismiss = {
                                navController.navigateUp()
                            },
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
