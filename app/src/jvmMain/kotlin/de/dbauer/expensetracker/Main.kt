package de.dbauer.expensetracker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import de.dbauer.expensetracker.di.platformModule
import de.dbauer.expensetracker.di.sharedModule
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.ui.MainContent
import de.dbauer.expensetracker.ui.ThemeMode
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

fun main() =
    application {
        startKoin {
            modules(sharedModule, platformModule)
        }

        val userPreferencesRepository = koinInject<IUserPreferencesRepository>()
        val isGridMode by userPreferencesRepository.gridMode.collectAsState()

        Window(
            onCloseRequest = ::exitApplication,
            title = "RecurringExpenseTracker",
        ) {
            // Use theme based on user setting
            val selectedTheme by userPreferencesRepository.themeMode.collectAsState()
            val useDarkTheme =
                when (selectedTheme) {
                    ThemeMode.Dark.value -> true
                    ThemeMode.Light.value -> false
                    else -> isSystemInDarkTheme()
                }

            ExpenseTrackerTheme(darkTheme = useDarkTheme) {
                MainContent(
                    isGridMode = isGridMode,
                    biometricSecurity = false,
                    canUseBiometric = false,
                    canUseNotifications = false,
                    hasNotificationPermission = true,
                    toggleGridMode = {
                        CoroutineScope(Dispatchers.Main).launch {
                            userPreferencesRepository.gridMode.save(!isGridMode)
                        }
                    },
                    onBiometricSecurityChange = {},
                    requestNotificationPermission = {},
                    navigateToPermissionsSettings = {},
                    onClickBackup = {},
                    onClickRestore = {},
                    updateWidget = {},
                )
            }
        }
    }
