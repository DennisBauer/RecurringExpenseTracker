package ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import data.SettingsPane
import data.SettingsPaneAbout
import data.SettingsPaneDefaultCurrency
import data.SettingsPaneLibraries
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.settings_about_app
import recurringexpensetracker.app.generated.resources.settings_about_libraries
import recurringexpensetracker.app.generated.resources.settings_default_currency
import recurringexpensetracker.app.generated.resources.settings_title
import ui.about.AboutLibrariesScreen
import ui.about.AboutScreen
import ui.theme.ExpenseTrackerThemePreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    biometricsChecked: Boolean,
    canUseBiometric: Boolean,
    canUseNotifications: Boolean,
    hasNotificationPermission: Boolean,
    onClickBackup: () -> Unit,
    onClickRestore: () -> Unit,
    onBiometricCheckedChange: (Boolean) -> Unit,
    requestNotificationPermission: () -> Unit,
    navigateToPermissionsSettings: () -> Unit,
    setTopAppBar: (@Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val settingsNavController = rememberNavController()

    NavHost(
        navController = settingsNavController,
        startDestination = SettingsPane,
        modifier = modifier,
    ) {
        composable<SettingsPane> {
            setTopAppBar {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.settings_title),
                        )
                    },
                )
            }
            SettingsMainScreen(
                biometricsChecked = biometricsChecked,
                canUseBiometric = canUseBiometric,
                canUseNotifications = canUseNotifications,
                hasNotificationPermission = hasNotificationPermission,
                onClickBackup = onClickBackup,
                onClickRestore = onClickRestore,
                onBiometricCheckedChange = onBiometricCheckedChange,
                requestNotificationPermission = requestNotificationPermission,
                navigateToPermissionsSettings = navigateToPermissionsSettings,
                onClickDefaultCurrency = { settingsNavController.navigate(SettingsPaneDefaultCurrency) },
                onClickAbout = { settingsNavController.navigate(SettingsPaneAbout) },
            )
        }

        composable<SettingsPaneAbout> {
            setTopAppBar {
                TopAppBar(
                    title = { Text(text = stringResource(Res.string.settings_about_app)) },
                    navigationIcon = {
                        IconButton(onClick = { settingsNavController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate back",
                            )
                        }
                    },
                )
            }
            AboutScreen(
                onLibrariesClick = { settingsNavController.navigate(SettingsPaneLibraries) },
            )
        }

        composable<SettingsPaneLibraries> {
            setTopAppBar {
                TopAppBar(
                    title = { Text(text = stringResource(Res.string.settings_about_libraries)) },
                    navigationIcon = {
                        IconButton(onClick = { settingsNavController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            AboutLibrariesScreen()
        }

        composable<SettingsPaneDefaultCurrency> {
            setTopAppBar {
                TopAppBar(
                    title = { Text(text = stringResource(Res.string.settings_default_currency)) },
                    navigationIcon = {
                        IconButton(onClick = { settingsNavController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
            SettingsDefaultCurrencyScreen(
                onNavigateBack = { settingsNavController.navigateUp() },
            )
        }
    }
}

private class SettingsScreenPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(true, false)
}

@Preview
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(SettingsScreenPreviewProvider::class) canUseBiometric: Boolean,
) {
    var checked by remember { mutableStateOf(false) }

    ExpenseTrackerThemePreview {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsScreen(
                biometricsChecked = checked,
                canUseBiometric = canUseBiometric,
                canUseNotifications = true,
                hasNotificationPermission = true,
                onClickBackup = {},
                onClickRestore = {},
                onBiometricCheckedChange = { checked = it },
                requestNotificationPermission = {},
                navigateToPermissionsSettings = {},
                setTopAppBar = {},
            )
        }
    }
}
