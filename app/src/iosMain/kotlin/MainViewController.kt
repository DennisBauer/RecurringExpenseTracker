import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import ui.MainContent
import ui.theme.ExpenseTrackerTheme
import viewmodel.RecurringExpenseViewModel
import viewmodel.UpcomingPaymentsViewModel
import viewmodel.database.ExpenseRepository
import viewmodel.database.RecurringExpenseDatabase
import viewmodel.database.getDatabaseBuilder

fun MainViewController() =
    ComposeUIViewController {
        val database by lazy { RecurringExpenseDatabase.getRecurringExpenseDatabase(getDatabaseBuilder()) }
        val repository by lazy { ExpenseRepository(database.recurringExpenseDao()) }
        val recurringExpenseViewModel = RecurringExpenseViewModel(repository)
        val upcomingPaymentsViewModel = UpcomingPaymentsViewModel(repository)

        ExpenseTrackerTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                MainContent(
                    weeklyExpense = recurringExpenseViewModel.weeklyExpense,
                    monthlyExpense = recurringExpenseViewModel.monthlyExpense,
                    yearlyExpense = recurringExpenseViewModel.yearlyExpense,
                    recurringExpenseData = recurringExpenseViewModel.recurringExpenseData,
                    isGridMode = false,
                    biometricSecurity = false,
                    canUseBiometric = false,
                    toggleGridMode = {},
                    onBiometricSecurityChanged = {},
                    onRecurringExpenseAdded = recurringExpenseViewModel::addRecurringExpense,
                    onRecurringExpenseEdited = recurringExpenseViewModel::editRecurringExpense,
                    onRecurringExpenseDeleted = recurringExpenseViewModel::deleteRecurringExpense,
                    onBackupClicked = {},
                    onRestoreClicked = {},
                    upcomingPaymentsViewModel = upcomingPaymentsViewModel,
                )
            }
        }
    }
