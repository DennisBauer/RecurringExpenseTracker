import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import di.platformModule
import di.sharedModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.database.UserPreferencesRepository
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import ui.MainContent
import ui.ThemeMode
import ui.theme.ExpenseTrackerTheme

fun MainViewController() =
    ComposeUIViewController {
        val userPreferencesRepository = koinInject<UserPreferencesRepository>()
        val isGridMode by userPreferencesRepository.gridMode.collectAsState()

        val selectedTheme by userPreferencesRepository.themeMode.collectAsState()
        val useDarkTheme =
            when (selectedTheme) {
                ThemeMode.Dark.value -> true
                ThemeMode.Light.value -> false
                else -> isSystemInDarkTheme()
            }

        ExpenseTrackerTheme(darkTheme = useDarkTheme) {
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
