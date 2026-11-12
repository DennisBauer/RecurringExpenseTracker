package de.dbauer.expensetracker.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.dbauer.expensetracker.shared.di.platformModule
import de.dbauer.expensetracker.shared.di.sharedModule
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.ui.MainContent
import de.dbauer.expensetracker.shared.ui.ThemeMode
import de.dbauer.expensetracker.shared.ui.getDefaultTabRoute
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

fun main() {
    val koin =
        startKoin {
            modules(sharedModule, platformModule)
        }.koin
    val startRoute = runBlocking { koin.get<IUserPreferencesRepository>().getDefaultTabRoute() }

    application {
        val userPreferencesRepository = koinInject<IUserPreferencesRepository>()
        val isGridMode by userPreferencesRepository.gridMode.collectAsState()

        Window(
            onCloseRequest = ::exitApplication,
            title = "RecurringExpenseTracker",
        ) {
            // Use theme based on user setting
            val selectedTheme by userPreferencesRepository.themeMode.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            ExpenseTrackerTheme(themeMode = ThemeMode.fromInt(selectedTheme)) {
                MainContent(
                    startRoute = startRoute,
                    isGridMode = isGridMode,
                    biometricSecurity = false,
                    canUseBiometric = false,
                    canUseNotifications = false,
                    hasNotificationPermission = true,
                    toggleGridMode = {
                        coroutineScope.launch {
                            userPreferencesRepository.gridMode.save(!isGridMode)
                        }
                    },
                    onBiometricSecurityChange = {},
                    requestNotificationPermission = {},
                    navigateToPermissionsSettings = {},
                    canUseBackup = false,
                    onClickBackup = {},
                    onClickRestore = {},
                    updateWidget = {},
                )
            }
        }
    }
}
