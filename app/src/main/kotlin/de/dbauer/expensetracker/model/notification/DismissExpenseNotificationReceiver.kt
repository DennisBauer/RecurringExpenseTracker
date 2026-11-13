package de.dbauer.expensetracker.model.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.dbauer.expensetracker.shared.model.notification.ExpenseNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.koin.java.KoinJavaComponent.inject

class DismissExpenseNotificationReceiver : BroadcastReceiver() {
    private val expenseNotificationManager: ExpenseNotificationManager by inject(
        ExpenseNotificationManager::class.java,
    )

    override fun onReceive(
        context: Context?,
        intent: Intent?,
    ) {
        val invalidExpenseId = -1
        val expenseId = intent?.getIntExtra(EXTRA_EXPENSE_ID, invalidExpenseId) ?: invalidExpenseId
        if (expenseId != invalidExpenseId) {
            runBlocking(Dispatchers.IO) {
                expenseNotificationManager.markNotificationAsShown(expenseId)
            }
        }
    }

    companion object {
        private const val EXTRA_EXPENSE_ID = "expense_id"
        private const val DISMISS_EXPENSE_NOTIFICATION_ACTION = "dismiss_expense_notification"

        fun createIntent(
            context: Context,
            expenseId: Int,
        ): Intent {
            return Intent(context, DismissExpenseNotificationReceiver::class.java).apply {
                setAction(DISMISS_EXPENSE_NOTIFICATION_ACTION)
                putExtra(EXTRA_EXPENSE_ID, expenseId)
            }
        }
    }
}
