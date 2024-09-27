package ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Restore
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.dialog_ok
import recurringexpensetracker.app.generated.resources.settings_backup
import recurringexpensetracker.app.generated.resources.settings_backup_create
import recurringexpensetracker.app.generated.resources.settings_backup_restore
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_info
import recurringexpensetracker.app.generated.resources.settings_currency_exchange_last_update
import recurringexpensetracker.app.generated.resources.settings_default_currency
import recurringexpensetracker.app.generated.resources.settings_general
import recurringexpensetracker.app.generated.resources.settings_security_biometric_lock
import recurringexpensetracker.app.generated.resources.settings_system_default
import recurringexpensetracker.app.generated.resources.settings_title
import recurringexpensetracker.app.generated.resources.settings_title_security
import ui.theme.ExpenseTrackerTheme
import viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    checked: Boolean,
    canUseBiometric: Boolean,
    onClickBackup: () -> Unit,
    onClickRestore: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
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
                        name = Res.string.settings_security_biometric_lock,
                        checked = checked,
                        onCheckedChange = onCheckedChange,
                        icon = Icons.Rounded.Fingerprint,
                    )
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
    modifier: Modifier = Modifier,
    subtitle: String = "",
    infoActionClick: (() -> Unit)? = null,
    onClick: () -> Unit,
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
    name: StringResource,
    checked: Boolean,
    icon: ImageVector,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
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
        Text(
            text = stringResource(name),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp).weight(1f),
            overflow = TextOverflow.Ellipsis,
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(16.dp))
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
                checked = checked,
                canUseBiometric = canUseBiometric,
                onClickBackup = {},
                onClickRestore = {},
                onCheckedChange = { checked = it },
                navController = rememberNavController(),
            )
        }
    }
}
