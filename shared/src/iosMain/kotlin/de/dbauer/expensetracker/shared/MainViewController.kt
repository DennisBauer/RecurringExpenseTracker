package de.dbauer.expensetracker.shared

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import de.dbauer.expensetracker.shared.di.platformModule
import de.dbauer.expensetracker.shared.di.sharedModule
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.ui.MainContent
import de.dbauer.expensetracker.shared.ui.ThemeMode
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerTheme
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
