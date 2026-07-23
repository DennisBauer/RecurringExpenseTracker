package de.dbauer.expensetracker.shared.ui.editexpense

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.edit_expense_include_in_summary

@Composable
fun IncludeInSummaryOption(
    includeInSummary: Boolean,
    onIncludeInSummaryChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .sizeIn(minHeight = 64.dp)
                .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(Res.string.edit_expense_include_in_summary),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = includeInSummary,
            onCheckedChange = onIncludeInSummaryChange,
        )
    }
}