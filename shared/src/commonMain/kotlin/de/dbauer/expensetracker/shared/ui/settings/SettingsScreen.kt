package de.dbauer.expensetracker.shared.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.dbauer.expensetracker.shared.data.SettingsPane
import de.dbauer.expensetracker.shared.data.SettingsPaneAbout
import de.dbauer.expensetracker.shared.data.SettingsPaneDefaultCurrency
import de.dbauer.expensetracker.shared.data.SettingsPaneLibraries
import de.dbauer.expensetracker.shared.ui.about.AboutLibrariesScreen
import de.dbauer.expensetracker.shared.ui.about.AboutScreen
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.shared.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.settings_about_app
import recurringexpensetracker.shared.generated.resources.settings_about_libraries
import recurringexpensetracker.shared.generated.resources.settings_currency_clear_search_content_desc
import recurringexpensetracker.shared.generated.resources.settings_currency_close_search_content_desc
import recurringexpensetracker.shared.generated.resources.settings_currency_search_content_desc
import recurringexpensetracker.shared.generated.resources.settings_currency_search_placeholder
import recurringexpensetracker.shared.generated.resources.settings_default_currency
import recurringexpensetracker.shared.generated.resources.settings_title
import recurringexpensetracker.shared.generated.resources.top_app_bar_navigate_up_content_desc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onClickTags: () -> Unit,
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
                onClickTags = onClickTags,
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
                                contentDescription =
                                    stringResource(Res.string.top_app_bar_navigate_up_content_desc),
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
            val viewModel = koinViewModel<SettingsViewModel>()
            var searchActive by rememberSaveable { mutableStateOf(false) }
            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(searchActive) {
                if (searchActive) {
                    focusRequester.requestFocus()
                }
            }

            setTopAppBar {
                TopAppBar(
                    title = {
                        if (searchActive) {
                            TextField(
                                value = viewModel.currencySearchQuery,
                                onValueChange = viewModel::onCurrencySearchQueryChange,
                                placeholder = {
                                    Text(
                                        stringResource(Res.string.settings_currency_search_placeholder),
                                    )
                                },
                                singleLine = true,
                                colors =
                                    TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    ),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                            )
                        } else {
                            Text(text = stringResource(Res.string.settings_default_currency))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (searchActive) {
                                searchActive = false
                                viewModel.clearCurrencySearch()
                            } else {
                                settingsNavController.navigateUp()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription =
                                    if (searchActive) {
                                        stringResource(Res.string.settings_currency_close_search_content_desc)
                                    } else {
                                        stringResource(Res.string.top_app_bar_navigate_up_content_desc)
                                    },
                            )
                        }
                    },
                    actions = {
                        if (!searchActive) {
                            IconButton(onClick = { searchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription =
                                        stringResource(
                                            Res.string.settings_currency_search_content_desc,
                                        ),
                                )
                            }
                        } else if (viewModel.currencySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearCurrencySearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription =
                                        stringResource(
                                            Res.string.settings_currency_clear_search_content_desc,
                                        ),
                                )
                            }
                        }
                    },
                )
            }

            SettingsDefaultCurrencyScreen(
                onNavigateBack = {
                    searchActive = false
                    viewModel.clearCurrencySearch()
                    settingsNavController.navigateUp()
                },
                viewModel = viewModel,
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
                onClickTags = {},
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
