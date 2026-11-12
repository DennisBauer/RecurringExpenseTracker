package de.dbauer.expensetracker.web

import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.sqlite.SQLiteDriver
import de.dbauer.expensetracker.shared.data.HomePane
import de.dbauer.expensetracker.shared.data.MainNavRoute
import de.dbauer.expensetracker.shared.data.UpcomingPane
import de.dbauer.expensetracker.shared.di.platformModule
import de.dbauer.expensetracker.shared.di.sharedModule
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.model.initSystemCurrencyCode
import de.dbauer.expensetracker.shared.ui.AlreadyOpenInAnotherTabScreen
import de.dbauer.expensetracker.shared.ui.DefaultTab
import de.dbauer.expensetracker.shared.ui.MainContent
import de.dbauer.expensetracker.shared.ui.ThemeMode
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerTheme
import de.dbauer.expensetracker.websqlite.createWebSQLiteDriver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.koin.dsl.module

private val webDriverModule =
    module {
        single<SQLiteDriver> { createWebSQLiteDriver() }
    }

fun main() {
    requestPersistentStorage()
    // The OPFS database can only be opened by one tab; a second tab gets a notice instead.
    acquireSingleTabLock { acquired ->
        if (acquired) {
            MainScope().launch {
                try {
                    initSystemCurrencyCode()
                    startApp()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Throwable) {
                    showStartupError(STARTUP_ERROR_MESSAGE, e.stackTraceToString())
                }
            }
        } else {
            showAlreadyOpenInAnotherTab()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun showAlreadyOpenInAnotherTab() {
    ComposeViewport {
        ExpenseTrackerTheme(themeMode = ThemeMode.FollowSystem) {
            AlreadyOpenInAnotherTabScreen()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private suspend fun startApp() {
    val koin =
        startKoin {
            modules(sharedModule, platformModule, webDriverModule)
        }.koin
    val startRoute: MainNavRoute =
        when (
            koin
                .get<IUserPreferencesRepository>()
                .defaultTab
                .get()
                .first()
        ) {
            DefaultTab.Upcoming.value -> UpcomingPane
            else -> HomePane()
        }

    ComposeViewport {
        val userPreferencesRepository = koinInject<IUserPreferencesRepository>()
        val isGridMode by userPreferencesRepository.gridMode.collectAsState()
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
