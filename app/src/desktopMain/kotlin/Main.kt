
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.platformModule
import di.sharedModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import ui.MainContent
import viewmodel.database.UserPreferencesRepository

fun main() =
    application {
        startKoin {
            modules(sharedModule, platformModule)
        }

        val userPreferencesRepository = koinInject<UserPreferencesRepository>()
        val isGridMode by userPreferencesRepository.gridMode.collectAsState()

        Window(
            onCloseRequest = ::exitApplication,
            title = "RecurringExpenseTracker",
        ) {
            MainContent(
                isGridMode = isGridMode,
                biometricSecurity = false,
                canUseBiometric = false,
                toggleGridMode = {
                    CoroutineScope(Dispatchers.Main).launch {
                        userPreferencesRepository.gridMode.save(!isGridMode)
                    }
                },
                onBiometricSecurityChange = {},
                onClickBackup = {},
                onClickRestore = {},
            )
        }
    }
