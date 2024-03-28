package de.dbauer.expensetracker.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun SettingsScreen(
    checked: Boolean,
    canUseBiometric: Boolean,
    onBackupClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
    onCheckChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
    ) {
        SettingsHeaderElement(
            header = R.string.settings_backup,
        )
        SettingsClickableElement(
            name = R.string.settings_backup_create,
            onClick = onBackupClicked,
        )
        SettingsClickableElement(
            name = R.string.settings_backup_restore,
            onClick = onRestoreClicked,
        )
        if (canUseBiometric) {
            HorizontalDivider()
            SettingsHeaderElement(header = R.string.settings_title_security)
            SettingsClickableElementWithToggle(
                name = R.string.settings_security_biometric_lock,
                checked = checked,
                onCheckChanged = onCheckChanged,
            )
        }
    }
}

@Composable
private fun SettingsHeaderElement(
    @StringRes header: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(id = header),
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
    @StringRes name: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        color = Color.Transparent,
        modifier =
            modifier
                .fillMaxWidth(),
        onClick = onClick,
    ) {
        Text(
            text = stringResource(id = name),
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                Modifier
                    .padding(16.dp),
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SettingsClickableElementWithToggle(
    @StringRes name: Int,
    checked: Boolean,
    onCheckChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable { onCheckChanged(!checked) },
    ) {
        Text(
            text = stringResource(id = name),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp).weight(1f),
            overflow = TextOverflow.Ellipsis,
        )
        Switch(checked = checked, onCheckedChange = onCheckChanged)
        Spacer(modifier = Modifier.width(16.dp))
    }
}

private class SettingsScreenPreviewProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean>
        get() = sequenceOf(true, false)
}

@PreviewLightDark
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
                onBackupClicked = {},
                onRestoreClicked = {},
                onCheckChanged = { checked = it },
            )
        }
    }
}
