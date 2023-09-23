package de.dbauer.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.dbauer.expensetracker.data.BottomNavItem
import de.dbauer.expensetracker.data.NavigationRoute
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.ui.EditRecurringExpense
import de.dbauer.expensetracker.ui.RecurringExpenseOverview
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import de.dbauer.expensetracker.viewmodel.MainActivityViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels {
        MainActivityViewModel.create((application as ExpenseTrackerApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            MainActivityContent(
                weeklyExpense = viewModel.weeklyExpense,
                monthlyExpense = viewModel.monthlyExpense,
                yearlyExpense = viewModel.yearlyExpense,
                recurringExpenseData = viewModel.recurringExpenseData,
                onRecurringExpenseAdded = {
                    viewModel.addRecurringExpense(it)
                },
                onRecurringExpenseEdited = {
                    viewModel.editRecurringExpense(it)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainActivityContent(
    weeklyExpense: String,
    monthlyExpense: String,
    yearlyExpense: String,
    recurringExpenseData: ImmutableList<RecurringExpenseData>,
    onRecurringExpenseAdded: (RecurringExpenseData) -> Unit,
    onRecurringExpenseEdited: (RecurringExpenseData) -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry = navController.currentBackStackEntryAsState()

    var addRecurringExpenseVisible by rememberSaveable { mutableStateOf(false) }

    var selectedRecurringExpense by rememberSaveable { mutableStateOf<RecurringExpenseData?>(null) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val bottomNavItems = listOf(
        BottomNavItem(
            name = "Home",
            route = NavigationRoute.Home,
            icon = Icons.Rounded.Home,
        ),
        BottomNavItem(
            name = "Settings",
            route = NavigationRoute.Settings,
            icon = Icons.Rounded.Settings,
        ),
    )

    ExpenseTrackerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Recurring Expense Tracker",
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            }, bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = item.route.value == backStackEntry.value?.destination?.route

                        NavigationBarItem(selected = selected,
                            onClick = { navController.navigate(item.route.value) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = "${item.name} Icon"
                                )
                            },
                            label = {
                                Text(text = item.name)
                            })
                    }
                }
            }, floatingActionButton = {
                FloatingActionButton(onClick = {
                    addRecurringExpenseVisible = true
                }) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
                }
            }, content = { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = NavigationRoute.Home.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    composable(NavigationRoute.Home.value) {
                        RecurringExpenseOverview(
                            weeklyExpense = weeklyExpense,
                            monthlyExpense = monthlyExpense,
                            yearlyExpense = yearlyExpense,
                            recurringExpenseData = recurringExpenseData,
                            onItemClicked = {
                                selectedRecurringExpense = it
                            },
                            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .nestedScroll(scrollBehavior.nestedScrollConnection),
                        )
                    }
                    composable(NavigationRoute.Settings.value) {

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
                    )
                }
            })
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
        recurringExpenseData = persistentListOf(
            RecurringExpenseData(
                id = 0,
                name = "Netflix",
                description = "My Netflix description",
                priceValue = 9.99f,
            ),
            RecurringExpenseData(
                id = 1,
                name = "Disney Plus",
                description = "My Disney Plus description",
                priceValue = 5f,
            ),
            RecurringExpenseData(
                id = 2,
                name = "Amazon Prime",
                description = "My Disney Plus description",
                priceValue = 7.95f,
            ),
        ),
        onRecurringExpenseAdded = {},
        onRecurringExpenseEdited = {},
    )
}