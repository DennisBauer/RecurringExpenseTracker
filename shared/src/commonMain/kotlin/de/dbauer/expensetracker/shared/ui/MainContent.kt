package de.dbauer.expensetracker.shared.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import de.dbauer.expensetracker.shared.conditional
import de.dbauer.expensetracker.shared.data.EditExpensePane
import de.dbauer.expensetracker.shared.data.HomePane
import de.dbauer.expensetracker.shared.data.MainNavRoute
import de.dbauer.expensetracker.shared.data.SettingsPane
import de.dbauer.expensetracker.shared.data.TagsPane
import de.dbauer.expensetracker.shared.data.UpcomingPane
import de.dbauer.expensetracker.shared.data.WhatsNew
import de.dbauer.expensetracker.shared.data.isInRoute
import de.dbauer.expensetracker.shared.model.database.IExpenseRepository
import de.dbauer.expensetracker.shared.ui.editexpense.EditRecurringExpenseScreen
import de.dbauer.expensetracker.shared.ui.settings.SettingsScreen
import de.dbauer.expensetracker.shared.ui.tags.TagsScreen
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.shared.ui.upcomingexpenses.UpcomingPaymentsScreen
import de.dbauer.expensetracker.shared.ui.whatsnew.IWhatsNew
import de.dbauer.expensetracker.shared.viewmodel.MainNavigationViewModel
import de.dbauer.expensetracker.shared.viewmodel.RecurringExpenseViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.edit_expense_button_add
import recurringexpensetracker.shared.generated.resources.home_filter_chip_archived
import recurringexpensetracker.shared.generated.resources.home_filter_content_desc
import recurringexpensetracker.shared.generated.resources.home_search_clear_content_desc
import recurringexpensetracker.shared.generated.resources.home_search_close_content_desc
import recurringexpensetracker.shared.generated.resources.home_search_content_desc
import recurringexpensetracker.shared.generated.resources.home_search_placeholder
import recurringexpensetracker.shared.generated.resources.home_title
import recurringexpensetracker.shared.generated.resources.snackbar_expense_archived
import recurringexpensetracker.shared.generated.resources.snackbar_expense_unarchived
import recurringexpensetracker.shared.generated.resources.tags_delete_undo
import recurringexpensetracker.shared.generated.resources.upcoming_title
import recurringexpensetracker.shared.generated.resources.whats_new_title
import kotlin.time.Clock

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
    startRoute: MainNavRoute = HomePane(),
    mainNavigationViewModel: MainNavigationViewModel = koinViewModel<MainNavigationViewModel>(),
    whatsNew: IWhatsNew = koinInject<IWhatsNew>(),
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
            if (backStackEntry
                    ?.destination
                    ?.isInRoute<MainNavRoute>(HomePane(), UpcomingPane, SettingsPane) == true
            ) {
                BottomNavBar(navController = navController)
            }
        },
        floatingActionButton = {
            if (backStackEntry?.destination?.isInRoute<MainNavRoute>(HomePane(), UpcomingPane) == true) {
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
                composable<HomePane> { backStackEntry ->
                    val homeRoute = backStackEntry.toRoute<HomePane>()
                    val recurringExpenseViewModel = koinViewModel<RecurringExpenseViewModel>()
                    var isSearchActive by rememberSaveable { mutableStateOf(false) }
                    val isFilterRowVisible = recurringExpenseViewModel.isFilterRowVisible
                    val showArchived by recurringExpenseViewModel.showArchived.collectAsState()

                    LaunchedEffect(homeRoute.showArchived) {
                        if (homeRoute.showArchived) {
                            recurringExpenseViewModel.onFilterRowVisibilityChange(true)
                            recurringExpenseViewModel.setShowArchived(true)
                        }
                    }

                    val backState =
                        rememberNavigationEventState(
                            currentInfo = NavigationEventInfo.None,
                        )
                    NavigationBackHandler(
                        isBackEnabled = isSearchActive,
                        state = backState,
                        onBackCompleted = {
                            recurringExpenseViewModel.onSearchQueryChanged("")
                            isSearchActive = false
                        },
                    )
                    val focusRequester = remember { FocusRequester() }

                    if (isSearchActive) {
                        mainNavigationViewModel.topAppBar = {
                            SearchBar(
                                inputField = {
                                    SearchBarDefaults.InputField(
                                        query = recurringExpenseViewModel.searchQuery,
                                        onQueryChange = { recurringExpenseViewModel.onSearchQueryChanged(it) },
                                        onSearch = { },
                                        expanded = false,
                                        onExpandedChange = { },
                                        modifier = Modifier.focusRequester(focusRequester),
                                        placeholder = {
                                            Text(text = stringResource(Res.string.home_search_placeholder))
                                        },
                                        leadingIcon = {
                                            IconButton(
                                                onClick = {
                                                    recurringExpenseViewModel.onSearchQueryChanged("")
                                                    isSearchActive = false
                                                },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription =
                                                        stringResource(Res.string.home_search_close_content_desc),
                                                )
                                            }
                                        },
                                        trailingIcon = {
                                            if (recurringExpenseViewModel.searchQuery.isNotEmpty()) {
                                                IconButton(
                                                    onClick = {
                                                        recurringExpenseViewModel.onSearchQueryChanged("")
                                                    },
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription =
                                                            stringResource(
                                                                Res.string.home_search_clear_content_desc,
                                                            ),
                                                    )
                                                }
                                            }
                                        },
                                    )
                                },
                                expanded = false,
                                onExpandedChange = { },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                            ) { }
                        }

                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }
                    } else {
                        mainNavigationViewModel.topAppBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = stringResource(Res.string.home_title),
                                    )
                                },
                                actions = {
                                    IconButton(
                                        onClick = { isSearchActive = true },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription =
                                                stringResource(Res.string.home_search_content_desc),
                                        )
                                    }
                                    ToggleGridModeButton(
                                        onToggleGridMode = toggleGridMode,
                                        isGridMode = isGridMode,
                                    )
                                    IconToggleButton(
                                        checked = isFilterRowVisible,
                                        onCheckedChange = { checked ->
                                            recurringExpenseViewModel.onFilterRowVisibilityChange(checked)
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.FilterList,
                                            contentDescription =
                                                stringResource(Res.string.home_filter_content_desc),
                                        )
                                    }
                                },
                            )
                        }
                    }

                    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                    val bottomPadding = if (keyboardOpen) 0.dp else 80.dp
                    val layoutDirection = LocalLayoutDirection.current
                    Column(
                        modifier =
                            Modifier
                                .conditional(keyboardOpen) {
                                    Modifier
                                        .padding(
                                            start = paddingValues.calculateStartPadding(layoutDirection),
                                            end = paddingValues.calculateEndPadding(layoutDirection),
                                            top = paddingValues.calculateTopPadding(),
                                        ).imePadding()
                                }.conditional(!keyboardOpen) {
                                    Modifier.padding(paddingValues)
                                },
                    ) {
                        if (isFilterRowVisible) {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .padding(top = 8.dp),
                            ) {
                                FilterChip(
                                    selected = showArchived,
                                    onClick = {
                                        recurringExpenseViewModel.setShowArchived(!showArchived)
                                    },
                                    label = {
                                        Text(text = stringResource(Res.string.home_filter_chip_archived))
                                    },
                                )
                            }
                        }
                        RecurringExpenseOverview(
                            isGridMode = isGridMode,
                            navController = navController,
                            recurringExpenseViewModel = recurringExpenseViewModel,
                            contentPadding =
                                PaddingValues(
                                    top = if (isFilterRowVisible) 0.dp else 8.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ),
                            bottomSafeArea = bottomPadding,
                        )
                    }

                    LaunchedEffect(mainNavigationViewModel.shouldShowWhatsNew) {
                        if (mainNavigationViewModel.shouldShowWhatsNew &&
                            !backStackEntry.destination.hasRoute(WhatsNew::class)
                        ) {
                            navController.navigate(WhatsNew)
                            mainNavigationViewModel.onWhatsNewShown()
                        }
                    }
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
                    val expenseRepository = koinInject<IExpenseRepository>()
                    val snackbarScope = rememberCoroutineScope()
                    val archivedMessage = stringResource(Res.string.snackbar_expense_archived)
                    val unarchivedMessage = stringResource(Res.string.snackbar_expense_unarchived)
                    val undoLabel = stringResource(Res.string.tags_delete_undo)
                    EditRecurringExpenseScreen(
                        expenseId = editExpensePane.expenseId,
                        canUseNotifications = canUseNotifications,
                        onDismiss = {
                            navController.navigateUp()
                            updateWidget()
                        },
                        onArchived = { archivedExpenseId ->
                            navController.navigateUp()
                            updateWidget()
                            snackbarScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result =
                                    snackbarHostState.showSnackbar(
                                        message = archivedMessage,
                                        actionLabel = undoLabel,
                                        duration = SnackbarDuration.Short,
                                    )
                                if (result == SnackbarResult.ActionPerformed) {
                                    expenseRepository.unarchive(archivedExpenseId)
                                    updateWidget()
                                }
                            }
                        },
                        onUnarchived = { unarchivedExpenseId, previousArchivedDateMillis ->
                            navController.navigateUp()
                            updateWidget()
                            snackbarScope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val result =
                                    snackbarHostState.showSnackbar(
                                        message = unarchivedMessage,
                                        actionLabel = undoLabel,
                                        duration = SnackbarDuration.Short,
                                    )
                                if (result == SnackbarResult.ActionPerformed) {
                                    val restoreMillis =
                                        previousArchivedDateMillis
                                            ?: Clock.System.now().toEpochMilliseconds()
                                    expenseRepository.archive(unarchivedExpenseId, restoreMillis)
                                    updateWidget()
                                }
                            }
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
                composable<WhatsNew> {
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
                            navController.navigateUp()
                        },
                        modifier = Modifier.padding(paddingValues),
                    )
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
