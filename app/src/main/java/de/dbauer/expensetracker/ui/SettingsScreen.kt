package de.dbauer.expensetracker.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun SettingsScreen(
    onLicensesClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState()),
    ) {
        SettingsClickableElement(name = R.string.settings_licenses, onClick = onLicensesClicked)
    }
}

@Composable
private fun SettingsClickableElement(
    @StringRes name: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
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

@Preview
@Composable
private fun SettingsScreenPreview() {
    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SettingsScreen(onLicensesClicked = {})
        }
    }
}
