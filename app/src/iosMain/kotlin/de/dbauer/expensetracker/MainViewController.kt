package de.dbauer.expensetracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
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

fun MainViewController() =
    ComposeUIViewController {
        val userPreferencesRepository = koinInject<IUserPreferencesRepository>()
        val isGridMode by userPreferencesRepository.gridMode.collectAsState()
        val selectedTheme by userPreferencesRepository.themeMode.collectAsState()

        ExpenseTrackerTheme(themeMode = ThemeMode.fromInt(selectedTheme)) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
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

fun initKoin() {
    startKoin {
        modules(listOf(sharedModule, platformModule))
    }
}
