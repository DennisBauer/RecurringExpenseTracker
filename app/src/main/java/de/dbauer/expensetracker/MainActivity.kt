package de.dbauer.expensetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.dbauer.expensetracker.data.BottomNavigation
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.ui.RecurringExpenseOverview
import de.dbauer.expensetracker.ui.SettingsScreen
import de.dbauer.expensetracker.ui.editexpense.EditRecurringExpense
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import de.dbauer.expensetracker.ui.upcomingexpenses.UpcomingPaymentsScreen
import de.dbauer.expensetracker.viewmodel.MainActivityViewModel
import de.dbauer.expensetracker.viewmodel.SettingsViewModel
import de.dbauer.expensetracker.viewmodel.UpcomingPaymentsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val mainActivityViewModel: MainActivityViewModel by viewModels {
        MainActivityViewModel.create((application as ExpenseTrackerApplication).repository)
    }
    private val upcomingPaymentsViewModel: UpcomingPaymentsViewModel by viewModels {
        UpcomingPaymentsViewModel.create((application as ExpenseTrackerApplication).repository)
    }
    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.create(getDatabasePath(Constants.DATABASE_NAME).path)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MainActivityContent(
                weeklyExpense = mainActivityViewModel.weeklyExpense,
                monthlyExpense = mainActivityViewModel.monthlyExpense,
                yearlyExpense = mainActivityViewModel.yearlyExpense,
                recurringExpenseData = mainActivityViewModel.recurringExpenseData,
                onRecurringExpenseAdded = {
                    mainActivityViewModel.addRecurringExpense(it)
                },
                onRecurringExpenseEdited = {
                    mainActivityViewModel.editRecurringExpense(it)
                },
                onRecurringExpenseDeleted = {
                    mainActivityViewModel.deleteRecurringExpense(it)
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
                                mainActivityViewModel.onDatabaseRestored()
                                upcomingPaymentsViewModel.onDatabaseRestored()
                                R.string.settings_backup_restored_toast
                            } else {
                                R.string.settings_backup_not_restored_toast
                            }
                        Toast.makeText(this@MainActivity, toastStringRes, Toast.LENGTH_LONG).show()
                    }
                },
                upcomingPaymentsViewModel = upcomingPaymentsViewModel,
            )
        }
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

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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

    ExpenseTrackerTheme {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(id = titleRes),
                            )
                        },
                        scrollBehavior = scrollBehavior,
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
                    if (BottomNavigation.Home.route == backStackEntry.value?.destination?.route) {
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
                                contentPadding =
                                    PaddingValues(
                                        top = 8.dp,
                                        bottom = 88.dp,
                                        start = 16.dp,
                                        end = 16.dp,
                                    ),
                                modifier =
                                    Modifier
                                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                            )
                        }
                        composable(BottomNavigation.Upcoming.route) {
                            UpcomingPaymentsScreen(
                                upcomingPaymentsViewModel = upcomingPaymentsViewModel,
                                onItemClicked = {
                                    selectedRecurringExpense = it
                                },
                                modifier =
                                    Modifier
                                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        composable(BottomNavigation.Settings.route) {
                            SettingsScreen(
                                onBackupClicked = {
                                    backupPathLauncher.launch(Constants.DEFAULT_BACKUP_NAME)
                                },
                                onRestoreClicked = {
                                    importPathLauncher.launch(arrayOf(Constants.BACKUP_MIME_TYPE))
                                },
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
    }
}

@Preview(showBackground = true)
@Composable
private fun MainActivityContentPreview() {
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
                ),
            ),
        onRecurringExpenseAdded = {},
        onRecurringExpenseEdited = {},
        onRecurringExpenseDeleted = {},
        onSelectBackupPath = { },
        onSelectImportFile = { },
        upcomingPaymentsViewModel = UpcomingPaymentsViewModel(null),
    )
}
