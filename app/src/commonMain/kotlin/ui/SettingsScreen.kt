package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import data.SettingsPane
import data.SettingsPaneAbout
import data.SettingsPaneLibraries
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.dialog_ok
import recurringexpensetracker.app.generated.resources.settings_about
import recurringexpensetracker.app.generated.resources.settings_about_app
import recurringexpensetracker.app.generated.resources.settings_backup
import recurringexpensetracker.app.generated.resources.settings_backup_create
import recurringexpensetracker.app.generated.resources.settings_backup_restore
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_info
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_last_update
import recurringexpensetracker.app.generated.resources.settings_default_currency
import recurringexpensetracker.app.generated.resources.settings_general
import recurringexpensetracker.app.generated.resources.settings_notifications
import recurringexpensetracker.app.generated.resources.settings_notifications_missing_permission_subtitle
import recurringexpensetracker.app.generated.resources.settings_notifications_missing_permission_title
import recurringexpensetracker.app.generated.resources.settings_notifications_schedule_days
import recurringexpensetracker.app.generated.resources.settings_notifications_schedule_days_subtitle
import recurringexpensetracker.app.generated.resources.settings_notifications_schedule_time
import recurringexpensetracker.app.generated.resources.settings_notifications_subtitle
import recurringexpensetracker.app.generated.resources.settings_notifications_upcoming
import recurringexpensetracker.app.generated.resources.settings_security_biometric_lock
import recurringexpensetracker.app.generated.resources.settings_system_default
import recurringexpensetracker.app.generated.resources.settings_title
import recurringexpensetracker.app.generated.resources.settings_title_security
import ui.about.AboutLibrariesScreen
import ui.about.AboutScreen
import ui.elements.TimePickerDialog
import ui.theme.ExpenseTrackerTheme
import viewmodel.SettingsViewModel

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
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val settingsNavController = rememberNavController()

    NavHost(
        navController = settingsNavController,
        startDestination = SettingsPane.ROUTE,
        modifier = modifier,
    ) {
        composable(SettingsPane.ROUTE) {
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
                navigateToAbout = { settingsNavController.navigate(SettingsPaneAbout.ROUTE) },
                navController = navController,
            )
        }

        composable(SettingsPaneAbout.ROUTE) {
            AboutScreen(
                onNavigateBack = { settingsNavController.navigateUp() },
                onLibrariesClick = { settingsNavController.navigate(SettingsPaneLibraries.ROUTE) },
            )
        }

        composable(SettingsPaneLibraries.ROUTE) {
            AboutLibrariesScreen(
                onNavigateBack = { settingsNavController.navigateUp() },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsMainScreen(
    biometricsChecked: Boolean,
    canUseBiometric: Boolean,
    canUseNotifications: Boolean,
    hasNotificationPermission: Boolean,
    onClickBackup: () -> Unit,
    onClickRestore: () -> Unit,
    onBiometricCheckedChange: (Boolean) -> Unit,
    requestNotificationPermission: () -> Unit,
    navigateToPermissionsSettings: () -> Unit,
    navigateToAbout: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.settings_title)) },
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        content = { paddingValues ->
            Column(
                modifier =
                    Modifier
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize(),
            ) {
                SettingsHeaderElement(
                    header = Res.string.settings_general,
                )
                SettingsClickableElement(
                    title = stringResource(Res.string.settings_default_currency),
                    subtitle =
                        viewModel.selectedCurrencyName.ifEmpty {
                            stringResource(Res.string.settings_system_default)
                        },
                    onClick = viewModel::onSelectCurrency,
                    icon = Icons.Rounded.CurrencyExchange,
                    infoActionClick = viewModel::onCurrencyInfo,
                )

                if (canUseBiometric) {
                    SettingsHeaderElement(header = Res.string.settings_title_security)
                    SettingsClickableElementWithToggle(
                        title = stringResource(Res.string.settings_security_biometric_lock),
                        checked = biometricsChecked,
                        onCheckedChange = onBiometricCheckedChange,
                        icon = Icons.Rounded.Fingerprint,
                    )
                }

                if (canUseNotifications) {
                    val notificationsEnabled by viewModel.upcomingPaymentNotification.collectAsState()

                    SettingsHeaderElement(header = Res.string.settings_notifications)
                    SettingsClickableElementWithToggle(
                        title = stringResource(Res.string.settings_notifications_upcoming),
                        subtitle = stringResource(Res.string.settings_notifications_subtitle),
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            viewModel.onUpcomingPaymentNotification(it)
                            if (it && !hasNotificationPermission) {
                                requestNotificationPermission()
                            }
                        },
                        icon = Icons.Rounded.Notifications,
                    )
                    AnimatedVisibility(notificationsEnabled) {
                        Column {
                            val upcomingPaymentNotificationTime by viewModel.upcomingPaymentNotificationTime
                                .collectAsState()
                            val upcomingPaymentNotificationTimeString =
                                LocalTime(
                                    upcomingPaymentNotificationTime / 60,
                                    upcomingPaymentNotificationTime % 60,
                                ).toString()
                            val upcomingPaymentNotificationDaysAdvance by
                                viewModel.upcomingPaymentNotificationDaysAdvance.collectAsState()
                            val upcomingPaymentNotificationDaysAdvanceString =
                                stringResource(
                                    Res.string.settings_notifications_schedule_days_subtitle,
                                    upcomingPaymentNotificationDaysAdvance,
                                )

                            if (!hasNotificationPermission) {
                                SettingsMissingPermissionElement(
                                    title =
                                        stringResource(
                                            Res.string.settings_notifications_missing_permission_title,
                                        ),
                                    subtitle =
                                        stringResource(
                                            Res.string.settings_notifications_missing_permission_subtitle,
                                        ),
                                    onClick = navigateToPermissionsSettings,
                                )
                            }
                            SettingsClickableElement(
                                title = stringResource(Res.string.settings_notifications_schedule_time),
                                subtitle = upcomingPaymentNotificationTimeString,
                                onClick = viewModel::onUpcomingPaymentNotificationTimeSelection,
                                icon = Icons.Rounded.Schedule,
                            )
                            SettingsClickableElement(
                                title = stringResource(Res.string.settings_notifications_schedule_days),
                                subtitle = upcomingPaymentNotificationDaysAdvanceString,
                                onClick = viewModel::onUpcomingPaymentNotificationDaysAdvanceSelection,
                                icon = Icons.Rounded.DateRange,
                            )
                        }
                    }
                }

                SettingsHeaderElement(
                    header = Res.string.settings_backup,
                )
                SettingsClickableElement(
                    title = stringResource(Res.string.settings_backup_create),
                    onClick = onClickBackup,
                    icon = Icons.Rounded.Backup,
                )
                SettingsClickableElement(
                    title = stringResource(Res.string.settings_backup_restore),
                    onClick = onClickRestore,
                    icon = Icons.Rounded.Restore,
                )
                SettingsHeaderElement(
                    header = Res.string.settings_about,
                )
                SettingsClickableElement(
                    title = stringResource(Res.string.settings_about_app),
                    onClick = navigateToAbout,
                    icon = Icons.Rounded.Info,
                )
            }
            if (viewModel.showCurrencySelectionDialog) {
                AlertDialog(
                    onDismissRequest = viewModel::onDismissCurrencySelectionDialog,
                    text = {
                        LazyColumn {
                            items(viewModel.availableCurrencies.value) { currency ->
                                TextButton(
                                    onClick = {
                                        viewModel.onCurrencySelected(currency)
                                    },
                                ) {
                                    Text(
                                        text = "${currency.name} (${currency.symbol})",
                                        textAlign = TextAlign.Start,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {},
                )
            } else if (viewModel.showCurrencyInfoDialog) {
                AlertDialog(
                    onDismissRequest = viewModel::onDismissCurrencyInfoDialog,
                    text = {
                        Column {
                            Text(
                                text = stringResource(Res.string.settings_currency_exchange_info),
                                modifier = Modifier.padding(bottom = 16.dp),
                            )
                            Text(
                                text =
                                    stringResource(
                                        Res.string.settings_currency_exchange_last_update,
                                        viewModel.exchangeRateLastUpdate,
                                    ),
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = viewModel::onDismissCurrencyInfoDialog,
                        ) {
                            Text(text = stringResource(Res.string.dialog_ok))
                        }
                    },
                )
            } else if (viewModel.upcomingPaymentNotificationTimePickerDialog) {
                val timePickerState =
                    rememberTimePickerState(
                        initialHour = 8,
                        initialMinute = 0,
                    )
                val upcomingPaymentNotificationTime by viewModel.upcomingPaymentNotificationTime.collectAsState()
                upcomingPaymentNotificationTime.takeIf { it >= 0 }?.let { initialTime ->
                    timePickerState.hour = initialTime / 60
                    timePickerState.minute = initialTime % 60
                }
                TimePickerDialog(
                    onDismiss = viewModel::onDismissUpcomingPaymentNotificationTimePickerDialog,
                    onConfirm = viewModel::onConfirmUpcomingPaymentNotificationTimePickerDialog,
                    timePickerState = timePickerState,
                )
            } else if (viewModel.upcomingPaymentNotificationDaysAdvanceDialog) {
                val upcomingPaymentNotificationDaysAdvance by viewModel.upcomingPaymentNotificationDaysAdvance
                    .collectAsState()
                var days by rememberSaveable { mutableStateOf("") }
                upcomingPaymentNotificationDaysAdvance.takeIf { it >= 0 }?.let { daysAdvance ->
                    days = daysAdvance.toString()
                }
                var inputError by rememberSaveable { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = viewModel::onDismissUpcomingPaymentNotificationDaysAdvanceDialog,
                    text = {
                        TextField(
                            value = days,
                            onValueChange = {
                                if (it.matches(Regex("\\d{0,2}"))) {
                                    days = it
                                    inputError = false
                                }
                            },
                            label = {
                                Text(
                                    text = stringResource(Res.string.settings_notifications_schedule_days),
                                )
                            },
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Next,
                                ),
                            singleLine = true,
                            isError = inputError,
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val daysOrNull = days.toIntOrNull()
                                if (daysOrNull != null && daysOrNull >= 0) {
                                    viewModel.onConfirmUpcomingPaymentNotificationDaysAdvanceDialog(daysOrNull)
                                } else {
                                    inputError = true
                                }
                            },
                        ) {
                            Text(text = stringResource(Res.string.dialog_ok))
                        }
                    },
                )
            }
        },
    )
}

@Composable
private fun SettingsHeaderElement(
    header: StringResource,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(header),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier =
            modifier
                .padding(16.dp)
                .fillMaxWidth(),
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SettingsClickableElement(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    infoActionClick: (() -> Unit)? = null,
) {
    Surface(
        color = Color.Transparent,
        modifier =
            modifier
                .fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
            Column(
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (infoActionClick != null) {
                IconButton(onClick = infoActionClick) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsClickableElementWithToggle(
    title: String,
    checked: Boolean,
    icon: ImageVector,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.padding(start = 16.dp),
        )
        Column(
            modifier =
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
private fun SettingsMissingPermissionElement(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier =
            modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .clickable { onClick() },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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

    ExpenseTrackerTheme {
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
                navController = rememberNavController(),
            )
        }
    }
}
