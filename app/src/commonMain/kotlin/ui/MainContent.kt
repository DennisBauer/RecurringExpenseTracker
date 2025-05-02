package ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import data.EditExpensePane
import data.EditExpensePane.Companion.getArgExpenseId
import data.HomePane
import data.SettingsPane
import data.UpcomingPane
import model.database.UserPreferencesRepository
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_button_add
import recurringexpensetracker.app.generated.resources.home_title
import recurringexpensetracker.app.generated.resources.upcoming_title
import toCurrencyString
import ui.editexpense.EditRecurringExpenseScreen
import ui.settings.SettingsScreen
import ui.upcomingexpenses.UpcomingPaymentsScreen
import viewmodel.MainNavigationViewModel
import viewmodel.RecurringExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    isGridMode: Boolean,
    biometricSecurity: Boolean,
    canUseBiometric: Boolean,
    canUseNotifications: Boolean,
    hasNotificationPermission: Boolean,
    toggleGridMode: () -> Unit,
    onBiometricSecurityChange: (Boolean) -> Unit,
    requestNotificationPermission: () -> Unit,
    navigateToPermissionsSettings: () -> Unit,
    onClickBackup: () -> Unit,
    onClickRestore: () -> Unit,
    updateWidget: () -> Unit,
    modifier: Modifier = Modifier,
    startRoute: String = HomePane.ROUTE,
    mainNavigationViewModel: MainNavigationViewModel = koinViewModel<MainNavigationViewModel>(),
    recurringExpenseViewModel: RecurringExpenseViewModel = koinViewModel<RecurringExpenseViewModel>(),
    userPreferencesRepository: UserPreferencesRepository = koinInject(),
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currencyCode by userPreferencesRepository.defaultCurrency.collectAsState()

    KoinContext {
        Scaffold(
            modifier = modifier,
            topBar = {
                mainNavigationViewModel.topAppBar()
            },
            bottomBar = {
                if (backStackEntry.value?.destination?.route in
                    listOf(HomePane.ROUTE, UpcomingPane.ROUTE, SettingsPane.ROUTE)
                ) {
                    BottomNavBar(navController = navController)
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
                                stringResource(Res.string.edit_expense_button_add),
                        )
                    }
                }
            },
            content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = startRoute,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    composable(HomePane.ROUTE) {
                        mainNavigationViewModel.topAppBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(Res.string.home_title),
                                    )
                                },
                                actions = {
                                    ToggleGridModeButton(
                                        onToggleGridMode = toggleGridMode,
                                        isGridMode = isGridMode,
                                    )
                                },
                            )
                        }

                        val weeklyExpense =
                            recurringExpenseViewModel.currencyPrefix +
                                recurringExpenseViewModel.weeklyExpense.toCurrencyString(currencyCode)
                        val monthlyExpense =
                            recurringExpenseViewModel.currencyPrefix +
                                recurringExpenseViewModel.monthlyExpense.toCurrencyString(currencyCode)
                        val yearlyExpense =
                            recurringExpenseViewModel.currencyPrefix +
                                recurringExpenseViewModel.yearlyExpense.toCurrencyString(currencyCode)

                        RecurringExpenseOverview(
                            weeklyExpense = weeklyExpense,
                            monthlyExpense = monthlyExpense,
                            yearlyExpense = yearlyExpense,
                            recurringExpenseData = recurringExpenseViewModel.recurringExpenseData,
                            isGridMode = isGridMode,
                            navController = navController,
                            contentPadding =
                                PaddingValues(
                                    top = 8.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ),
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                    composable(UpcomingPane.ROUTE) {
                        mainNavigationViewModel.topAppBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(Res.string.upcoming_title),
                                    )
                                },
                                actions = {
                                    ToggleGridModeButton(
                                        onToggleGridMode = toggleGridMode,
                                        isGridMode = isGridMode,
                                    )
                                },
                            )
                        }

                        UpcomingPaymentsScreen(
                            isGridMode = isGridMode,
                            navController = navController,
                            contentPadding =
                                PaddingValues(
                                    top = 8.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ),
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                    composable(SettingsPane.ROUTE) {
                        var topAppBar by remember { mutableStateOf<@Composable () -> Unit>({}) }
                        SettingsScreen(
                            biometricsChecked = biometricSecurity,
                            onClickBackup = onClickBackup,
                            onClickRestore = onClickRestore,
                            onBiometricCheckedChange = onBiometricSecurityChange,
                            canUseBiometric = canUseBiometric,
                            canUseNotifications = canUseNotifications,
                            hasNotificationPermission = hasNotificationPermission,
                            requestNotificationPermission = requestNotificationPermission,
                            navigateToPermissionsSettings = navigateToPermissionsSettings,
                            setTopAppBar = {
                                mainNavigationViewModel.topAppBar = it
                                topAppBar = it
                            },
                            modifier = Modifier.padding(paddingValues),
                        )
                        mainNavigationViewModel.topAppBar = topAppBar
                    }
                    composable(
                        route = EditExpensePane.ROUTE,
                        arguments = EditExpensePane.navArguments,
                    ) { backStackEntry ->
                        var topAppBar by remember { mutableStateOf<@Composable () -> Unit>({}) }
                        EditRecurringExpenseScreen(
                            expenseId = backStackEntry.getArgExpenseId(),
                            canUseNotifications = canUseNotifications,
                            onDismiss = {
                                navController.navigateUp()
                                updateWidget()
                            },
                            setTopAppBar = {
                                mainNavigationViewModel.topAppBar = it
                                topAppBar = it
                            },
                            modifier = Modifier.padding(paddingValues),
                        )
                        mainNavigationViewModel.topAppBar = topAppBar
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
