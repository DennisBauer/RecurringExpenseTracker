import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import di.platformModule
import di.sharedModule
import org.koin.core.context.startKoin
import ui.MainContent
import ui.theme.ExpenseTrackerTheme

fun MainViewController() =
    ComposeUIViewController {
        startKoin {
            modules(sharedModule, platformModule)
        }

        ExpenseTrackerTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
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
    }
