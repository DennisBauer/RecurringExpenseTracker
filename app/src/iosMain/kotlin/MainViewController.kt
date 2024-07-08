import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

        var isGridMode by remember { mutableStateOf(false) }

        ExpenseTrackerTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
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
    }
