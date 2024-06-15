import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.MainContent
import viewmodel.RecurringExpenseViewModel
import viewmodel.UpcomingPaymentsViewModel
import viewmodel.database.ExpenseRepository
import viewmodel.database.RecurringExpenseDatabase
import viewmodel.database.getDatabaseBuilder

fun main() =
    application {
        val database by lazy { RecurringExpenseDatabase.getRecurringExpenseDatabase(getDatabaseBuilder()) }
        val repository by lazy { ExpenseRepository(database.recurringExpenseDao()) }
        val recurringExpenseViewModel = RecurringExpenseViewModel(repository)
        val upcomingPaymentsViewModel = UpcomingPaymentsViewModel(repository)

        Window(
            onCloseRequest = ::exitApplication,
            title = "RecurringExpenseTracker",
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
