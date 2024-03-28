package de.dbauer.expensetracker

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Button
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.dbauer.expensetracker.data.BottomNavigation
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.security.BiometricPromptManager
import de.dbauer.expensetracker.security.BiometricPromptManager.BiometricResult
import de.dbauer.expensetracker.ui.RecurringExpenseOverview
import de.dbauer.expensetracker.ui.SettingsScreen
import de.dbauer.expensetracker.ui.customizations.ExpenseColor
import de.dbauer.expensetracker.ui.editexpense.EditRecurringExpense
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import de.dbauer.expensetracker.ui.upcomingexpenses.UpcomingPaymentsScreen
import de.dbauer.expensetracker.viewmodel.MainActivityViewModel
import de.dbauer.expensetracker.viewmodel.RecurringExpenseViewModel
import de.dbauer.expensetracker.viewmodel.SettingsViewModel
import de.dbauer.expensetracker.viewmodel.UpcomingPaymentsViewModel
import de.dbauer.expensetracker.viewmodel.database.UserPreferencesRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val recurringExpenseViewModel: RecurringExpenseViewModel by viewModels {
        RecurringExpenseViewModel.create((application as ExpenseTrackerApplication).repository)
    }
    private val upcomingPaymentsViewModel: UpcomingPaymentsViewModel by viewModels {
        UpcomingPaymentsViewModel.create((application as ExpenseTrackerApplication).repository)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.create(getDatabasePath(Constants.DATABASE_NAME).path)
    }
    private val userPreferencesRepository: UserPreferencesRepository by lazy {
        (application as ExpenseTrackerApplication).userPreferencesRepository
    }
    private val mainActivityViewModel: MainActivityViewModel by viewModels()

    private val biometricPromptManager: BiometricPromptManager by lazy { BiometricPromptManager(this) }

    private val biometricSetup =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == FINISH_TASK_WITH_ACTIVITY) {
                triggerAuthPrompt()
            } else if (it.resultCode == Activity.RESULT_CANCELED) {
                lifecycleScope.launch {
                    userPreferencesRepository.saveBiometricSecurity(false)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            biometricPromptManager.promptResult.collectLatest {
                when (it) {
                    is BiometricResult.AuthenticationError -> {
                        Log.e(TAG, it.error)
                    }
                    BiometricResult.AuthenticationFailed -> {
                        Log.e(TAG, "Authentication failed")
                    }
                    BiometricResult.AuthenticationNotSet -> {
                        // open directly the setup settings for biometrics
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            biometricSetup.launch(
                                Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(
                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        biometricPromptManager.authenticators,
                                    )
                                },
                            )
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            // open old setup settings dialog
                            @Suppress("DEPRECATION")
                            biometricSetup.launch(Intent(Settings.ACTION_FINGERPRINT_ENROLL))
                        } else {
                            // open security settings
                            try {
                                startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                            } catch (_: ActivityNotFoundException) {
                            } finally {
                                launch {
                                    userPreferencesRepository.saveBiometricSecurity(false)
                                }
                            }
                        }
                    }
                    BiometricResult.AuthenticationSuccess -> {
                        Log.i(TAG, "Authentication Success")
                        mainActivityViewModel.isUnlocked = true
                    }
                    BiometricResult.FeatureUnavailable -> {
                        Log.i(TAG, "Authentication unavailable")
                    }
                    BiometricResult.HardwareUnavailable -> {
                        Log.i(TAG, "Hardware not available")
                    }
                }
            }
        }

        val canUseBiometric = biometricPromptManager.canUseAuthenticator()

        setContent {
            val isGridMode by userPreferencesRepository.getIsGridMode().collectAsState(initial = false)
            val biometricSecurity by userPreferencesRepository.getBiometricSecurity().collectAsState(
                initial = false,
            )
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    if (biometricSecurity && !mainActivityViewModel.isUnlocked) {
                        triggerAuthPrompt()
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Button(
                                onClick = {
                                    triggerAuthPrompt()
                                },
                            ) {
                                Text(text = stringResource(id = R.string.biometric_prompt_manager_unlock))
                            }
                        }
                    } else {
                        MainActivityContent(
                            weeklyExpense = recurringExpenseViewModel.weeklyExpense,
                            monthlyExpense = recurringExpenseViewModel.monthlyExpense,
                            yearlyExpense = recurringExpenseViewModel.yearlyExpense,
                            recurringExpenseData = recurringExpenseViewModel.recurringExpenseData,
                            onRecurringExpenseAdded = {
                                recurringExpenseViewModel.addRecurringExpense(it)
                            },
                            onRecurringExpenseEdited = {
                                recurringExpenseViewModel.editRecurringExpense(it)
                            },
                            onRecurringExpenseDeleted = {
                                recurringExpenseViewModel.deleteRecurringExpense(it)
                            },
                            onSelectBackupPath = {
                                val takeFlags: Int =
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                applicationContext.contentResolver.takePersistableUriPermission(it, takeFlags)

                                lifecycleScope.launch {
                                    val backupSuccessful = settingsViewModel.backupDatabase(it, applicationContext)
                                    val toastStringRes =
                                        if (backupSuccessful) {
                                            R.string.settings_backup_created_toast
                                        } else {
                                            R.string.settings_backup_not_created_toast
                                        }
                                    Toast.makeText(this@MainActivity, toastStringRes, Toast.LENGTH_LONG).show()
                                }
                            },
                            onSelectImportFile = {
                                lifecycleScope.launch {
                                    val backupRestored = settingsViewModel.restoreDatabase(it, applicationContext)
                                    val toastStringRes =
                                        if (backupRestored) {
                                            recurringExpenseViewModel.onDatabaseRestored()
                                            upcomingPaymentsViewModel.onDatabaseRestored()
                                            R.string.settings_backup_restored_toast
                                        } else {
                                            R.string.settings_backup_not_restored_toast
                                        }
                                    Toast.makeText(this@MainActivity, toastStringRes, Toast.LENGTH_LONG).show()
                                }
                            },
                            upcomingPaymentsViewModel = upcomingPaymentsViewModel,
                            isGridMode = isGridMode,
                            biometricSecurity = biometricSecurity,
                            onBiometricSecurityChanged = {
                                lifecycleScope.launch {
                                    userPreferencesRepository.saveBiometricSecurity(it)
                                }
                            },
                            toggleGridMode = {
                                lifecycleScope.launch {
                                    userPreferencesRepository.saveIsGridMode(!isGridMode)
                                }
                            },
                            canUseBiometric = canUseBiometric,
                        )
                    }
                }
            }
        }
    }

    private fun triggerAuthPrompt() {
        biometricPromptManager.showBiometricPrompt(
            title = getString(R.string.biometric_prompt_manager_title),
            cancel = getString(R.string.cancel),
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricPromptManager.onDestroy()
    }

    private companion object {
        private const val TAG = "MainActivity"
        private const val FINISH_TASK_WITH_ACTIVITY = 2
    }
}

@Suppress("ktlint:compose:vm-forwarding-check")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    weeklyExpense: String,
    monthlyExpense: String,
    yearlyExpense: String,
    recurringExpenseData: ImmutableList<RecurringExpenseData>,
    isGridMode: Boolean,
    biometricSecurity: Boolean,
    canUseBiometric: Boolean,
    toggleGridMode: () -> Unit,
    onBiometricSecurityChanged: (Boolean) -> Unit,
    onRecurringExpenseAdded: (RecurringExpenseData) -> Unit,
    onRecurringExpenseEdited: (RecurringExpenseData) -> Unit,
    onRecurringExpenseDeleted: (RecurringExpenseData) -> Unit,
    onSelectBackupPath: (backupPath: Uri) -> Unit,
    onSelectImportFile: (importPath: Uri) -> Unit,
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()

    val titleRes by remember {
        derivedStateOf {
            when (backStackEntry.value?.destination?.route) {
                BottomNavigation.Home.route -> R.string.home_title
                BottomNavigation.Upcoming.route -> R.string.upcoming_title
                BottomNavigation.Settings.route -> R.string.settings_title
                else -> R.string.home_title
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

    val backupPathLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(Constants.BACKUP_MIME_TYPE),
        ) {
            if (it == null) return@rememberLauncherForActivityResult
            onSelectBackupPath(it)
        }
    val importPathLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) {
            if (it == null) return@rememberLauncherForActivityResult
            onSelectImportFile(it)
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(id = titleRes),
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
                                            R.string.top_app_bar_icon_button_grid_close_content_desc,
                                        )
                                    } else {
                                        stringResource(
                                            R.string.top_app_bar_icon_button_grid_open_content_desc,
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
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
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
                            Text(text = stringResource(id = item.name))
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
                            stringResource(R.string.home_add_expense_fab_content_description),
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
                        onItemClicked = {
                            selectedRecurringExpense = it
                        },
                        isGridMode = isGridMode,
                        modifier =
                            Modifier
                                .nestedScroll(homeScrollBehavior.nestedScrollConnection),
                        contentPadding =
                            PaddingValues(
                                top = dimensionResource(id = R.dimen.overview_list_grid_padding_top),
                                bottom = dimensionResource(id = R.dimen.overview_list_grid_padding_bottom),
                                start =
                                    dimensionResource(
                                        id = R.dimen.overview_list_grid_padding_start_end,
                                    ),
                                end =
                                    dimensionResource(
                                        id = R.dimen.overview_list_grid_padding_start_end,
                                    ),
                            ),
                    )
                }
                composable(BottomNavigation.Upcoming.route) {
                    UpcomingPaymentsScreen(
                        upcomingPaymentsViewModel = upcomingPaymentsViewModel,
                        onItemClicked = {
                            selectedRecurringExpense = it
                        },
                        isGridMode = isGridMode,
                        modifier =
                            Modifier
                                .nestedScroll(upcomingScrollBehavior.nestedScrollConnection),
                        contentPadding =
                            PaddingValues(
                                top = dimensionResource(id = R.dimen.overview_list_grid_padding_top),
                                bottom = dimensionResource(id = R.dimen.overview_list_grid_padding_bottom),
                                start =
                                    dimensionResource(
                                        id = R.dimen.overview_list_grid_padding_start_end,
                                    ),
                                end =
                                    dimensionResource(
                                        id = R.dimen.overview_list_grid_padding_start_end,
                                    ),
                            ),
                    )
                }
                composable(BottomNavigation.Settings.route) {
                    SettingsScreen(
                        checked = biometricSecurity,
                        onBackupClicked = {
                            backupPathLauncher.launch(Constants.DEFAULT_BACKUP_NAME)
                        },
                        onRestoreClicked = {
                            importPathLauncher.launch(arrayOf(Constants.BACKUP_MIME_TYPE))
                        },
                        onCheckChanged = onBiometricSecurityChanged,
                        canUseBiometric = canUseBiometric,
                        modifier = Modifier.nestedScroll(settingsScrollBehavior.nestedScrollConnection),
                    )
                }
            }
            if (addRecurringExpenseVisible) {
                EditRecurringExpense(
                    onUpdateExpense = {
                        onRecurringExpenseAdded(it)
                        addRecurringExpenseVisible = false
                    },
                    onDismissRequest = { addRecurringExpenseVisible = false },
                )
            }
            if (selectedRecurringExpense != null) {
                EditRecurringExpense(
                    onUpdateExpense = {
                        onRecurringExpenseEdited(it)
                        selectedRecurringExpense = null
                    },
                    onDismissRequest = { selectedRecurringExpense = null },
                    currentData = selectedRecurringExpense,
                    onDeleteExpense = {
                        onRecurringExpenseDeleted(it)
                        selectedRecurringExpense = null
                    },
                )
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun MainActivityContentPreview() {
    var isGridMode by remember { mutableStateOf(false) }
    var biometricSecurity by remember { mutableStateOf(false) }
    ExpenseTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            MainActivityContent(
                weeklyExpense = "4,00 €",
                monthlyExpense = "16,00 €",
                yearlyExpense = "192,00 €",
                recurringExpenseData =
                    persistentListOf(
                        RecurringExpenseData(
                            id = 0,
                            name = "Netflix",
                            description = "My Netflix description",
                            price = 9.99f,
                            monthlyPrice = 9.99f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            0L,
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
                            1L,
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
                            2L,
                            ExpenseColor.Blue,
                        ),
                    ),
                onRecurringExpenseAdded = {},
                onRecurringExpenseEdited = {},
                onRecurringExpenseDeleted = {},
                onSelectBackupPath = { },
                onSelectImportFile = { },
                upcomingPaymentsViewModel = UpcomingPaymentsViewModel(null),
                isGridMode = isGridMode,
                toggleGridMode = { isGridMode = !isGridMode },
                biometricSecurity = biometricSecurity,
                onBiometricSecurityChanged = { biometricSecurity = it },
                canUseBiometric = true,
            )
        }
    }
}
