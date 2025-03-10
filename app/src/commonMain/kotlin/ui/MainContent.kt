package ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import data.EditExpensePane
import data.EditExpensePane.Companion.getArgExpenseId
import data.HomePane
import data.SettingsPane
import data.UpcomingPane
import model.database.UserPreferencesRepository
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import toCurrencyString
import ui.editexpense.EditRecurringExpenseScreen
import ui.upcomingexpenses.UpcomingPaymentsScreen
import viewmodel.RecurringExpenseViewModel
import viewmodel.UpcomingPaymentsViewModel

@Suppress("ktlint:compose:vm-forwarding-check")
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
    modifier: Modifier = Modifier,
    startRoute: String = HomePane.ROUTE,
    recurringExpenseViewModel: RecurringExpenseViewModel = koinViewModel<RecurringExpenseViewModel>(),
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel = koinViewModel<UpcomingPaymentsViewModel>(),
    userPreferencesRepository: UserPreferencesRepository = koinInject(),
) {
    val navController = rememberNavController()
    val currencyCode by userPreferencesRepository.defaultCurrency.collectAsState()

    KoinContext {
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = modifier.fillMaxSize(),
        ) {
            composable(HomePane.ROUTE) {
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
                    onToggleGridMode = toggleGridMode,
                    navController = navController,
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
                    isGridMode = isGridMode,
                    onToggleGridMode = toggleGridMode,
                    navController = navController,
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
                    biometricsChecked = biometricSecurity,
                    onClickBackup = onClickBackup,
                    onClickRestore = onClickRestore,
                    onBiometricCheckedChange = onBiometricSecurityChange,
                    canUseBiometric = canUseBiometric,
                    canUseNotifications = canUseNotifications,
                    hasNotificationPermission = hasNotificationPermission,
                    requestNotificationPermission = requestNotificationPermission,
                    navigateToPermissionsSettings = navigateToPermissionsSettings,
                    navController = navController,
                )
            }
            composable(
                route = EditExpensePane.ROUTE,
                arguments = EditExpensePane.navArguments,
            ) { backStackEntry ->
                EditRecurringExpenseScreen(
                    expenseId = backStackEntry.getArgExpenseId(),
                    canUseNotifications = canUseNotifications,
                    onDismiss = navController::navigateUp,
                )
            }
        }
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
