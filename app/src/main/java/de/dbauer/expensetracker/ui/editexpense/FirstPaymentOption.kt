package de.dbauer.expensetracker.ui.editexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.helper.UtcDateFormat
import java.util.Date
import java.util.TimeZone

private const val INVALID_DATE = 0L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstPaymentOption(
    date: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var datePickerOpen by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.orNowIfInvalid())

    val formattedDate =
        if (date != INVALID_DATE) {
            UtcDateFormat.getDateInstance().format(Date(date))
        } else {
            stringResource(id = R.string.edit_expense_first_payment_placeholder)
        }

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(R.string.edit_expense_first_payment),
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                    )
                    .clickable {
                        datePickerOpen = true
                    },
        ) {
            Text(
                text = formattedDate,
                modifier = Modifier.padding(16.dp).weight(1f),
            )
            if (date != INVALID_DATE) {
                IconButton(onClick = {
                    onDateSelected(INVALID_DATE)
                    datePickerState.selectedDateMillis = INVALID_DATE.orNowIfInvalid()
                }) {
                    Icon(imageVector = Icons.Rounded.Cancel, contentDescription = null)
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (datePickerOpen) {
        DatePickerDialog(
            onDismissRequest = {
                datePickerOpen = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerOpen = false
                        datePickerState.selectedDateMillis?.let {
                            onDateSelected(it)
                        }
                    },
                ) {
                    Text(text = stringResource(id = R.string.dialog_ok))
                }
            },
            modifier = modifier,
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun Long.orNowIfInvalid(): Long {
    return if (this != INVALID_DATE) this else System.currentTimeMillis() + TimeZone.getDefault().rawOffset
}
