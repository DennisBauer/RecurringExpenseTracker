package de.dbauer.expensetracker.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import de.dbauer.expensetracker.data.EditExpensePane
import de.dbauer.expensetracker.data.HomePane
import de.dbauer.expensetracker.data.MainNavRoute
import de.dbauer.expensetracker.data.NavRoute
import de.dbauer.expensetracker.data.SettingsPane
import de.dbauer.expensetracker.data.TagsPane
import de.dbauer.expensetracker.data.UpcomingPane
import de.dbauer.expensetracker.data.WhatsNew
import de.dbauer.expensetracker.data.isInRoute
import de.dbauer.expensetracker.ui.editexpense.EditRecurringExpenseScreen
import de.dbauer.expensetracker.ui.settings.SettingsScreen
import de.dbauer.expensetracker.ui.tags.TagsScreen
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.ui.upcomingexpenses.UpcomingPaymentsScreen
import de.dbauer.expensetracker.ui.whatsnew.IWhatsNew
import de.dbauer.expensetracker.viewmodel.MainNavigationViewModel
import de.dbauer.expensetracker.viewmodel.TopAppBarViewModel
import kotlinx.coroutines.flow.last
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.delete
import recurringexpensetracker.app.generated.resources.edit_expense_button_add
import recurringexpensetracker.app.generated.resources.edit_expense_title
import recurringexpensetracker.app.generated.resources.home_title
import recurringexpensetracker.app.generated.resources.upcoming_title
import recurringexpensetracker.app.generated.resources.whats_new_title

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
    startRoute: NavRoute = NavRoute.HomePane,
    mainNavigationViewModel: MainNavigationViewModel = koinViewModel<MainNavigationViewModel>(),
    topAppBarViewModel: TopAppBarViewModel = koinViewModel<TopAppBarViewModel>(),
    whatsNew: IWhatsNew = koinInject<IWhatsNew>(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentRoute by mainNavigationViewModel.currentRoute.collectAsState()
    val topAppBarTitle by topAppBarViewModel.title.collectAsState()
    val topAppBarShowBackAction by topAppBarViewModel.showBackAction.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(topAppBarTitle),
                    )
                },
                navigationIcon = {
                    if (topAppBarShowBackAction) {
                        IconButton(
                            onClick = {
                                mainNavigationViewModel.navigateUp()
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
                    topAppBarViewModel.actions.forEach { (icon, contentDescription, onClick) ->
                        IconButton(
                            onClick = onClick,
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = stringResource(contentDescription),
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(it)
            }
        },
        bottomBar = {
            if (currentRoute::class in
                listOf(NavRoute.HomePane::class, NavRoute.UpcomingPane::class, NavRoute.SettingsPane::class)
            ) {
                BottomNavBar(
                    backStackTop = currentRoute,
                    onClick = mainNavigationViewModel::onBottomNavClick,
                )
            }
        },
        floatingActionButton = {
            if (currentRoute::class in listOf(NavRoute.HomePane::class, NavRoute.UpcomingPane::class)) {
                FloatingActionButton(
                    onClick = {
                        mainNavigationViewModel.navigate(NavRoute.EditExpensePane())
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
            NavDisplay(
                backStack = mainNavigationViewModel.backStack,
                onBack = { mainNavigationViewModel.navigateUp() },
                entryProvider =
                    entryProvider {
                        entry<NavRoute.HomePane> { backStackEntry ->
                            RecurringExpenseOverview(
                                isGridMode = isGridMode,
                                navigateTo = { mainNavigationViewModel.navigate(it) },
                                contentPadding =
                                    PaddingValues(
                                        top = 8.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                    ),
                                modifier = Modifier.padding(paddingValues),
                            )

                            // TODO: Move into the VM itself
                            LaunchedEffect(mainNavigationViewModel.shouldShowWhatsNew) {
                                if (mainNavigationViewModel.shouldShowWhatsNew &&
                                    currentRoute::class != NavRoute.WhatsNew
                                ) {
                                    mainNavigationViewModel.navigate(NavRoute.WhatsNew)
                                }
                            }
                        }
                        entry<NavRoute.UpcomingPane> {
                            UpcomingPaymentsScreen(
                                isGridMode = isGridMode,
                                navigateTo = { mainNavigationViewModel.navigate(it) },
                                contentPadding =
                                    PaddingValues(
                                        top = 8.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                    ),
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                        entry<NavRoute.SettingsPane> {
                            var topAppBar by remember { mutableStateOf<@Composable () -> Unit>({}) }
                            SettingsScreen(
                                onClickTags = { mainNavigationViewModel.navigate(NavRoute.TagsPane) },
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
                        entry<NavRoute.EditExpensePane> { backStackEntry ->
                            EditRecurringExpenseScreen(
                                expenseId = backStackEntry.expenseId,
                                canUseNotifications = canUseNotifications,
                                onDismiss = {
                                    mainNavigationViewModel.navigateUp()
                                    updateWidget()
                                },
                                onEditTagsClick = {
                                    mainNavigationViewModel.navigate(NavRoute.TagsPane)
                                },
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                        entry<NavRoute.TagsPane> {
                            TagsScreen(
                                snackbarHostState = snackbarHostState,
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                        entry<de.dbauer.expensetracker.ui.whatsnew.WhatsNew> {
                            mainNavigationViewModel.topAppBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = stringResource(Res.string.whats_new_title),
                                        )
                                    },
                                )
                            }
                            whatsNew.WhatsNewUI(
                                onDismissRequest = {
                                    mainNavigationViewModel.onWhatsNewShown()
                                },
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                    },
            )
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
