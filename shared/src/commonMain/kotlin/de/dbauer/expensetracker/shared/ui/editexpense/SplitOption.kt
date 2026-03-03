package de.dbauer.expensetracker.shared.ui.editexpense

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.edit_expense_split
import recurringexpensetracker.shared.generated.resources.edit_expense_split_between

@Composable
fun SplitOption(
    isSplit: Boolean,
    onIsSplitChange: (Boolean) -> Unit,
    splitBetweenPeople: Int,
    onSplitBetweenPeopleChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.edit_expense_split),
                style = MaterialTheme.typography.bodyLarge,
            )
            Switch(
                checked = isSplit,
                onCheckedChange = onIsSplitChange,
            )
        }
        if (isSplit) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(Res.string.edit_expense_split_between, splitBetweenPeople),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (splitBetweenPeople > 2) {
                                onSplitBetweenPeopleChange(splitBetweenPeople - 1)
                            }
                        },
                        enabled = splitBetweenPeople > 2,
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = null)
                    }
                    Text(
                        text = splitBetweenPeople.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                    IconButton(
                        onClick = { onSplitBetweenPeopleChange(splitBetweenPeople + 1) },
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    }
                }
            }
        }
    }
}
 
