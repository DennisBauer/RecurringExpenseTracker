import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import di.platformModule
import di.sharedModule
import org.koin.core.context.startKoin
import ui.MainContent

fun main() =
    application {
        startKoin {
            modules(sharedModule, platformModule)
        }

        var isGridMode by remember { mutableStateOf(false) }

        Window(
            onCloseRequest = ::exitApplication,
            title = "RecurringExpenseTracker",
        ) {
            MainContent(
                isGridMode = isGridMode,
                biometricSecurity = false,
                canUseBiometric = false,
                toggleGridMode = {
                    isGridMode = !isGridMode
                },
                onBiometricSecurityChange = {},
                onClickBackup = {},
                onClickRestore = {},
            )
        }
    }
