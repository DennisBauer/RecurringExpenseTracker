package ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import data.BottomNavigation
import data.Recurrence
import data.RecurringExpenseData
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.home_add_expense_fab_content_description
import recurringexpensetracker.app.generated.resources.home_title
import recurringexpensetracker.app.generated.resources.settings_title
import recurringexpensetracker.app.generated.resources.top_app_bar_icon_button_grid_close_content_desc
import recurringexpensetracker.app.generated.resources.top_app_bar_icon_button_grid_open_content_desc
import recurringexpensetracker.app.generated.resources.upcoming_title
import ui.customizations.ExpenseColor
import ui.editexpense.EditRecurringExpense
import ui.theme.ExpenseTrackerTheme
import ui.upcomingexpenses.UpcomingPaymentsScreen
import viewmodel.UpcomingPaymentsViewModel

@Suppress("ktlint:compose:vm-forwarding-check")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    weeklyExpense: String,
    monthlyExpense: String,
    yearlyExpense: String,
    recurringExpenseData: List<RecurringExpenseData>,
    isGridMode: Boolean,
    biometricSecurity: Boolean,
    canUseBiometric: Boolean,
    toggleGridMode: () -> Unit,
    onBiometricSecurityChange: (Boolean) -> Unit,
    onRecurringExpenseAdd: (RecurringExpenseData) -> Unit,
    onRecurringExpenseEdit: (RecurringExpenseData) -> Unit,
    onRecurringExpenseDelete: (RecurringExpenseData) -> Unit,
    onClickBackup: () -> Unit,
    onClickRestore: () -> Unit,
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()

    val titleRes by remember {
        derivedStateOf {
            when (backStackEntry.value?.destination?.route) {
                BottomNavigation.Home.route -> Res.string.home_title
                BottomNavigation.Upcoming.route -> Res.string.upcoming_title
                BottomNavigation.Settings.route -> Res.string.settings_title
                else -> Res.string.home_title
            }
        }
    }

    var addRecurringExpenseVisible by rememberSaveable { mutableStateOf(false) }

    var selectedRecurringExpense by rememberSaveable { mutableStateOf<RecurringExpenseData?>(null) }

    val homeScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val upcomingScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val settingsScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val topAppBarScrollBehavior by remember {
        derivedStateOf {
            when (backStackEntry.value?.destination?.route) {
                BottomNavigation.Home.route -> homeScrollBehavior
                BottomNavigation.Upcoming.route -> upcomingScrollBehavior
                BottomNavigation.Settings.route -> settingsScrollBehavior
                else -> homeScrollBehavior
            }
        }
    }

    val bottomNavigationItems =
        listOf(
            BottomNavigation.Home,
            BottomNavigation.Upcoming,
            BottomNavigation.Settings,
        )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(titleRes),
                    )
                },
                actions = {
                    // only creates toggling button if navigation is at home or upcoming payments
                    if (backStackEntry.value?.destination?.route == BottomNavigation.Home.route ||
                        backStackEntry.value?.destination?.route == BottomNavigation.Upcoming.route
                    ) {
                        IconButton(onClick = {
                            toggleGridMode()
                            // Because of the [AnimatedContent] in [RecurringExpenseOverview] the list is
                            // reset and scrolled back to the top. To make sure the scroll state matches
                            // that we need to reset it here. It make the TopAppBar use the surface
                            // color again. This is a workaround which can hopefully removed in the near
                            // future.
                            homeScrollBehavior.state.contentOffset = 0f
                        }) {
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
            NavigationBar {
                bottomNavigationItems.forEach { item ->
                    val selected =
                        item.route == backStackEntry.value?.destination?.route

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
        },
        floatingActionButton = {
            if (BottomNavigation.Home.route == backStackEntry.value?.destination?.route ||
                BottomNavigation.Upcoming.route == backStackEntry.value?.destination?.route
            ) {
                FloatingActionButton(onClick = {
                    addRecurringExpenseVisible = true
                }) {
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
                startDestination = BottomNavigation.Home.route,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
            ) {
                composable(BottomNavigation.Home.route) {
                    RecurringExpenseOverview(
                        weeklyExpense = weeklyExpense,
                        monthlyExpense = monthlyExpense,
                        yearlyExpense = yearlyExpense,
                        recurringExpenseData = recurringExpenseData,
                        onClickItem = {
                            selectedRecurringExpense = it
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
                composable(BottomNavigation.Upcoming.route) {
                    UpcomingPaymentsScreen(
                        upcomingPaymentsViewModel = upcomingPaymentsViewModel,
                        onClickItem = {
                            selectedRecurringExpense = it
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
                composable(BottomNavigation.Settings.route) {
                    SettingsScreen(
                        checked = biometricSecurity,
                        onClickBackup = onClickBackup,
                        onClickRestore = onClickRestore,
                        onCheckedChange = onBiometricSecurityChange,
                        canUseBiometric = canUseBiometric,
                        modifier = Modifier.nestedScroll(settingsScrollBehavior.nestedScrollConnection),
                    )
                }
            }
            if (addRecurringExpenseVisible) {
                EditRecurringExpense(
                    onUpdateExpense = {
                        onRecurringExpenseAdd(it)
                        addRecurringExpenseVisible = false
                    },
                    onDismissRequest = { addRecurringExpenseVisible = false },
                )
            }
            if (selectedRecurringExpense != null) {
                EditRecurringExpense(
                    onUpdateExpense = {
                        onRecurringExpenseEdit(it)
                        selectedRecurringExpense = null
                    },
                    onDismissRequest = { selectedRecurringExpense = null },
                    currentData = selectedRecurringExpense,
                    onDeleteExpense = {
                        onRecurringExpenseDelete(it)
                        selectedRecurringExpense = null
                    },
                )
            }
        },
    )
}

@Preview
@Composable
private fun MainActivityContentPreview() {
    var isGridMode by remember { mutableStateOf(false) }
    var biometricSecurity by remember { mutableStateOf(false) }
    ExpenseTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MainContent(
                weeklyExpense = "4,00 €",
                monthlyExpense = "16,00 €",
                yearlyExpense = "192,00 €",
                recurringExpenseData =
                    listOf(
                        RecurringExpenseData(
                            id = 0,
                            name = "Netflix",
                            description = "My Netflix description",
                            price = 9.99f,
                            monthlyPrice = 9.99f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            Clock.System.now(),
                            ExpenseColor.Dynamic,
                        ),
                        RecurringExpenseData(
                            id = 1,
                            name = "Disney Plus",
                            description = "My Disney Plus description",
                            price = 5f,
                            monthlyPrice = 5f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            Clock.System.now(),
                            ExpenseColor.Red,
                        ),
                        RecurringExpenseData(
                            id = 2,
                            name = "Amazon Prime",
                            description = "My Disney Plus description",
                            price = 7.95f,
                            monthlyPrice = 7.95f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            Clock.System.now(),
                            ExpenseColor.Blue,
                        ),
                    ),
                onRecurringExpenseAdd = {},
                onRecurringExpenseEdit = {},
                onRecurringExpenseDelete = {},
                onClickBackup = { },
                onClickRestore = { },
                upcomingPaymentsViewModel = UpcomingPaymentsViewModel(null),
                isGridMode = isGridMode,
                toggleGridMode = { isGridMode = !isGridMode },
                biometricSecurity = biometricSecurity,
                onBiometricSecurityChange = { biometricSecurity = it },
                canUseBiometric = true,
            )
        }
    }
}
