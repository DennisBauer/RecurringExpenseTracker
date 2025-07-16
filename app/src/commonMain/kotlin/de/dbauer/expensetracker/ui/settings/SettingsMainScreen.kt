package de.dbauer.expensetracker.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Rocket
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.ui.DefaultTab
import de.dbauer.expensetracker.ui.ThemeMode
import de.dbauer.expensetracker.ui.elements.TimePickerDialog
import de.dbauer.expensetracker.viewmodel.SettingsViewModel
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.bottom_nav_home
import recurringexpensetracker.app.generated.resources.bottom_nav_upcoming
import recurringexpensetracker.app.generated.resources.dialog_ok
import recurringexpensetracker.app.generated.resources.settings_about
import recurringexpensetracker.app.generated.resources.settings_about_app
import recurringexpensetracker.app.generated.resources.settings_backup
import recurringexpensetracker.app.generated.resources.settings_backup_create
import recurringexpensetracker.app.generated.resources.settings_backup_restore
import recurringexpensetracker.app.generated.resources.settings_currency
import recurringexpensetracker.app.generated.resources.settings_default_currency
import recurringexpensetracker.app.generated.resources.settings_default_tab
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
import recurringexpensetracker.app.generated.resources.settings_show_converted_currency
import recurringexpensetracker.app.generated.resources.settings_system_default
import recurringexpensetracker.app.generated.resources.settings_theme_mode
import recurringexpensetracker.app.generated.resources.settings_theme_mode_dark
import recurringexpensetracker.app.generated.resources.settings_theme_mode_follow_system
import recurringexpensetracker.app.generated.resources.settings_theme_mode_light
import recurringexpensetracker.app.generated.resources.settings_title_security

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMainScreen(
    biometricsChecked: Boolean,
    canUseBiometric: Boolean,
    canUseNotifications: Boolean,
    hasNotificationPermission: Boolean,
    onClickBackup: () -> Unit,
    onClickRestore: () -> Unit,
    onBiometricCheckedChange: (Boolean) -> Unit,
    requestNotificationPermission: () -> Unit,
    navigateToPermissionsSettings: () -> Unit,
    onClickDefaultCurrency: () -> Unit,
    onClickAbout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModel>(),
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
    ) {
        SettingsHeaderElement(
            header = Res.string.settings_general,
        )
        val selectedTheme by viewModel.selectedThemeMode.collectAsState(ThemeMode.FollowSystem)
        SettingsClickableElement(
            title = stringResource(Res.string.settings_theme_mode),
            subtitle =
                when (selectedTheme) {
                    ThemeMode.FollowSystem -> stringResource(Res.string.settings_theme_mode_follow_system)
                    ThemeMode.Dark -> stringResource(Res.string.settings_theme_mode_dark)
                    ThemeMode.Light -> stringResource(Res.string.settings_theme_mode_light)
                },
            onClick = viewModel::onClickThemeSelection,
            icon = Icons.Rounded.DarkMode,
        )
        val defaultTab by viewModel.selectedDefaultTab.collectAsState(DefaultTab.Home)
        SettingsClickableElement(
            title = stringResource(Res.string.settings_default_tab),
            subtitle =
                when (defaultTab) {
                    DefaultTab.Home -> stringResource(Res.string.bottom_nav_home)
                    DefaultTab.Upcoming -> stringResource(Res.string.bottom_nav_upcoming)
                },
            onClick = viewModel::onClickDefaultTabSelection,
            icon = Icons.Rounded.Rocket,
        )
        SettingsHeaderElement(
            header = Res.string.settings_currency,
        )
        SettingsClickableElement(
            title = stringResource(Res.string.settings_default_currency),
            subtitle =
                viewModel.selectedCurrencyName.ifEmpty {
                    stringResource(Res.string.settings_system_default)
                },
            onClick = onClickDefaultCurrency,
            icon = Icons.Rounded.AttachMoney,
        )
        SettingsClickableElementWithToggle(
            title = stringResource(Res.string.settings_show_converted_currency),
            checked = viewModel.showConvertedCurrency.collectAsState().value,
            icon = Icons.Rounded.CurrencyExchange,
            onCheckedChange = viewModel::onShowConvertedCurrency,
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
            onClick = onClickAbout,
            icon = Icons.Rounded.Info,
        )
    }
    if (viewModel.showThemeSelectionDialog) {
        val selectedTheme by viewModel.selectedThemeMode.collectAsState(initial = ThemeMode.FollowSystem)

        AlertDialog(
            onDismissRequest = viewModel::onDismissThemeSelectionDialog,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_theme_mode),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    DialogCheckbox(
                        text = stringResource(Res.string.settings_theme_mode_follow_system),
                        checked = selectedTheme == ThemeMode.FollowSystem,
                        onClick = { viewModel.onSelectTheme(ThemeMode.FollowSystem) },
                    )
                    DialogCheckbox(
                        text = stringResource(Res.string.settings_theme_mode_dark),
                        checked = selectedTheme == ThemeMode.Dark,
                        onClick = { viewModel.onSelectTheme(ThemeMode.Dark) },
                    )
                    DialogCheckbox(
                        text = stringResource(Res.string.settings_theme_mode_light),
                        checked = selectedTheme == ThemeMode.Light,
                        onClick = { viewModel.onSelectTheme(ThemeMode.Light) },
                    )
                }
            },
            confirmButton = {},
        )
    } else if (viewModel.showDefaultTabSelectionDialog) {
        val defaultTab by viewModel.selectedDefaultTab.collectAsState(DefaultTab.Home)

        AlertDialog(
            onDismissRequest = viewModel::onDismissDefaultTabSelectionDialog,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_default_tab),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                    DialogCheckbox(
                        text = stringResource(Res.string.bottom_nav_home),
                        checked = defaultTab == DefaultTab.Home,
                        onClick = { viewModel.onSelectDefaultTab(DefaultTab.Home) },
                    )
                    DialogCheckbox(
                        text = stringResource(Res.string.bottom_nav_upcoming),
                        checked = defaultTab == DefaultTab.Upcoming,
                        onClick = { viewModel.onSelectDefaultTab(DefaultTab.Upcoming) },
                    )
                }
            },
            confirmButton = {},
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
}

@Composable
fun DialogCheckbox(
    text: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 48.dp)
                .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
