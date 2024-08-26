package ui.editexpense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
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
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.dialog_ok
import recurringexpensetracker.app.generated.resources.edit_expense_first_payment
import recurringexpensetracker.app.generated.resources.edit_expense_first_payment_placeholder
import toLocaleString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstPaymentOption(
    date: Instant?,
    onSelectDate: (Instant?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var datePickerOpen by rememberSaveable { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = date.orNowIfInvalid())

    val formattedDate =
        date?.toLocaleString()
            ?: stringResource(Res.string.edit_expense_first_payment_placeholder)

    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = stringResource(Res.string.edit_expense_first_payment),
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
                    ).clickable {
                        datePickerOpen = true
                    },
        ) {
            Icon(
                modifier = Modifier.padding(16.dp),
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
            )
            Text(
                text = formattedDate,
                modifier = Modifier.padding(16.dp).weight(1f),
            )
            if (date != null) {
                IconButton(onClick = {
                    onSelectDate(null)
                    datePickerState.selectedDateMillis = null.orNowIfInvalid()
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
                            onSelectDate(Instant.fromEpochMilliseconds(it))
                        }
                    },
                ) {
                    Text(text = stringResource(Res.string.dialog_ok))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun Instant?.orNowIfInvalid(): Long {
    return this?.toEpochMilliseconds()
        ?: Clock.System
            .now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .toInstant(TimeZone.UTC)
            .toEpochMilliseconds()
}
