package de.dbauer.expensetracker.ui.upcomingexpenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.List
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.UpcomingPaymentData
import de.dbauer.expensetracker.toCurrencyString
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import de.dbauer.expensetracker.viewmodel.UpcomingPaymentsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.text.DateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Composable
fun UpcomingPaymentsScreen(
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel,
    onItemClicked: (RecurringExpenseData) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    if (upcomingPaymentsViewModel.upcomingPaymentsData.size > 0) {
        UpcomingPaymentsOverview(
            upcomingPaymentsData = upcomingPaymentsViewModel.upcomingPaymentsData,
            onItemClicked = {
                upcomingPaymentsViewModel.onExpenseWithIdClicked(it, onItemClicked)
            },
            modifier = modifier,
            contentPadding = contentPadding,
        )
    } else {
        UpcomingPaymentsOverviewPlaceholder(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
        )
    }
}

@Composable
private fun UpcomingPaymentsOverview(
    upcomingPaymentsData: ImmutableList<UpcomingPaymentData>,
    onItemClicked: (Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        items(items = upcomingPaymentsData) { upcomingPaymentData ->
            UpcomingPayment(
                upcomingPaymentData = upcomingPaymentData,
                onItemClicked = {
                    onItemClicked(upcomingPaymentData.id)
                },
            )
        }
    }
}

@Composable
private fun UpcomingPayment(
    upcomingPaymentData: UpcomingPaymentData,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inDaysString =
        when (upcomingPaymentData.nextPaymentRemainingDays) {
            0 -> stringResource(id = R.string.upcoming_time_remaining_today)
            1 -> stringResource(id = R.string.upcoming_time_remaining_tomorrow)
            else ->
                stringResource(
                    id = R.string.upcoming_time_remaining_days,
                    upcomingPaymentData.nextPaymentRemainingDays,
                )
        }

    Card(
        modifier = modifier.clickable { onItemClicked() },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(end = 16.dp)
                        .weight(1f),
            ) {
                Text(
                    text = upcomingPaymentData.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = upcomingPaymentData.nextPaymentDate,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = upcomingPaymentData.price.toCurrencyString(),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = inDaysString,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
fun UpcomingPaymentsOverviewPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.List,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
        )
        Text(
            text = stringResource(id = R.string.upcoming_placeholder_title),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun UpcomingPaymentsOverviewPreview() {
    val dateFormat = DateFormat.getDateInstance()

    val nextPaymentDays1 = 0
    val nextPaymentDate1String =
        dateFormat.format(
            Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(nextPaymentDays1.toLong())),
        )
    val nextPaymentDays2 = 1
    val nextPaymentDate2String =
        dateFormat.format(
            Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(nextPaymentDays2.toLong())),
        )
    val nextPaymentDays3 = 2
    val nextPaymentDate3String =
        dateFormat.format(
            Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(nextPaymentDays3.toLong())),
        )

    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            UpcomingPaymentsOverview(
                upcomingPaymentsData =
                    persistentListOf(
                        UpcomingPaymentData(
                            id = 0,
                            name = "Netflix",
                            price = 9.99f,
                            nextPaymentRemainingDays = nextPaymentDays1,
                            nextPaymentDate = nextPaymentDate1String,
                        ),
                        UpcomingPaymentData(
                            id = 1,
                            name = "Disney Plus",
                            price = 5f,
                            nextPaymentRemainingDays = nextPaymentDays2,
                            nextPaymentDate = nextPaymentDate2String,
                        ),
                        UpcomingPaymentData(
                            id = 2,
                            name = "Amazon Prime with a long name",
                            price = 7.95f,
                            nextPaymentRemainingDays = nextPaymentDays3,
                            nextPaymentDate = nextPaymentDate3String,
                        ),
                    ),
                onItemClicked = {},
                contentPadding = PaddingValues(8.dp),
            )
        }
    }
}

@Preview
@Composable
private fun UpcomingPaymentsOverviewPlaceholderPreview() {
    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            UpcomingPaymentsOverviewPlaceholder()
        }
    }
}
