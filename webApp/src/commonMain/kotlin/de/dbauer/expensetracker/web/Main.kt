package de.dbauer.expensetracker.web

import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.sqlite.SQLiteDriver
import de.dbauer.expensetracker.shared.di.platformModule
import de.dbauer.expensetracker.shared.di.sharedModule
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.ui.MainContent
import de.dbauer.expensetracker.shared.ui.ThemeMode
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerTheme
import de.dbauer.expensetracker.websqlite.createWebSQLiteDriver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.dsl.module

private val webDriverModule =
    module {
        single<SQLiteDriver> { createWebSQLiteDriver() }
    }

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(sharedModule, platformModule, webDriverModule)
    }

    ComposeViewport {
        val userPreferencesRepository = koinInject<IUserPreferencesRepository>()
        val isGridMode by userPreferencesRepository.gridMode.collectAsState()
        val selectedTheme by userPreferencesRepository.themeMode.collectAsState()

        ExpenseTrackerTheme(themeMode = ThemeMode.fromInt(selectedTheme)) {
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
