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

        Window(
            onCloseRequest = ::exitApplication,
            title = "RecurringExpenseTracker",
        ) {
            MainContent(
                isGridMode = false,
                biometricSecurity = false,
                canUseBiometric = false,
                toggleGridMode = {},
                onBiometricSecurityChange = {},
                onClickBackup = {},
                onClickRestore = {},
            )
        }
    }
