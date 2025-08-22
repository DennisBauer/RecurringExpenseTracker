package de.dbauer.expensetracker.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.navigation.toRoute
import de.dbauer.expensetracker.data.EditExpensePane
import de.dbauer.expensetracker.data.HomePane
import de.dbauer.expensetracker.data.MainNavRoute
import de.dbauer.expensetracker.data.SettingsPane
import de.dbauer.expensetracker.data.TagsPane
import de.dbauer.expensetracker.data.UpcomingPane
import de.dbauer.expensetracker.data.isInRoute
import de.dbauer.expensetracker.ui.editexpense.EditRecurringExpenseScreen
import de.dbauer.expensetracker.ui.settings.SettingsScreen
import de.dbauer.expensetracker.ui.tags.TagsScreen
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.ui.upcomingexpenses.UpcomingPaymentsScreen
import de.dbauer.expensetracker.viewmodel.MainNavigationViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_button_add
import recurringexpensetracker.app.generated.resources.home_title
import recurringexpensetracker.app.generated.resources.upcoming_title

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
    startRoute: MainNavRoute = HomePane,
    mainNavigationViewModel: MainNavigationViewModel = koinViewModel<MainNavigationViewModel>(),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        topBar = {
            mainNavigationViewModel.topAppBar()
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(it)
            }
        },
        bottomBar = {
            if (backStackEntry?.destination?.isInRoute(HomePane, UpcomingPane, SettingsPane) == true) {
                BottomNavBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (backStackEntry?.destination?.isInRoute(HomePane, UpcomingPane) == true) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(EditExpensePane())
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
                composable<HomePane> {
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

                    RecurringExpenseOverview(
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
                composable<UpcomingPane> {
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
                composable<SettingsPane> {
                    var topAppBar by remember { mutableStateOf<@Composable () -> Unit>({}) }
                    SettingsScreen(
                        onClickTags = { navController.navigate(TagsPane) },
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
                composable<EditExpensePane> { backStackEntry ->
                    var topAppBar by remember { mutableStateOf<@Composable () -> Unit>({}) }
                    val editExpensePane = backStackEntry.toRoute<EditExpensePane>()
                    EditRecurringExpenseScreen(
                        expenseId = editExpensePane.expenseId,
                        canUseNotifications = canUseNotifications,
                        onDismiss = {
                            navController.navigateUp()
                            updateWidget()
                        },
                        onEditTagsClick = {
                            navController.navigate(TagsPane)
                        },
                        setTopAppBar = {
                            mainNavigationViewModel.topAppBar = it
                            topAppBar = it
                        },
                        modifier = Modifier.padding(paddingValues),
                    )
                    mainNavigationViewModel.topAppBar = topAppBar
                }
                composable<TagsPane> {
                    var topAppBar by remember { mutableStateOf<@Composable () -> Unit>({}) }
                    TagsScreen(
                        onNavigateBack = {
                            navController.navigateUp()
                        },
                        setTopAppBar = {
                            mainNavigationViewModel.topAppBar = it
                            topAppBar = it
                        },
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(paddingValues),
                    )
                    mainNavigationViewModel.topAppBar = topAppBar
                }
            }
        },
    )
}

@Preview
@Composable
private fun MainActivityContentPreview() {
    var isGridMode by remember { mutableStateOf(false) }
    var biometricSecurity by remember { mutableStateOf(false) }
    ExpenseTrackerThemePreview {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MainContent(
                isGridMode = isGridMode,
                toggleGridMode = { isGridMode = !isGridMode },
                biometricSecurity = biometricSecurity,
                canUseBiometric = true,
                canUseNotifications = true,
                hasNotificationPermission = true,
                onBiometricSecurityChange = { biometricSecurity = it },
                requestNotificationPermission = {},
                navigateToPermissionsSettings = {},
                onClickBackup = {},
                onClickRestore = {},
                updateWidget = {},
            )
        }
    }
}
