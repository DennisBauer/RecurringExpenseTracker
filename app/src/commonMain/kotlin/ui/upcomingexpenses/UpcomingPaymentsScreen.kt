package ui.upcomingexpenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import data.CurrencyValue
import data.EditExpensePane
import data.UpcomingPaymentData
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.upcoming_expenses_remaining_this_month
import recurringexpensetracker.app.generated.resources.upcoming_placeholder_title
import recurringexpensetracker.app.generated.resources.upcoming_time_remaining_days
import recurringexpensetracker.app.generated.resources.upcoming_time_remaining_today
import recurringexpensetracker.app.generated.resources.upcoming_time_remaining_tomorrow
import toLocaleString
import ui.customizations.ExpenseColor
import ui.theme.ExpenseTrackerTheme
import viewmodel.UpcomingPaymentsViewModel

@Composable
fun UpcomingPaymentsScreen(
    isGridMode: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel = koinViewModel<UpcomingPaymentsViewModel>(),
) {
    if (upcomingPaymentsViewModel.upcomingPaymentsData.isNotEmpty()) {
        UpcomingPaymentsOverview(
            upcomingPaymentsData = upcomingPaymentsViewModel.upcomingPaymentsData,
            remainingExpenseThisMonth = upcomingPaymentsViewModel.remainingExpenseThisMonth,
            onClickItem = { expenseId ->
                upcomingPaymentsViewModel.onExpenseWithIdClicked(expenseId) {
                    navController.navigate(EditExpensePane(expenseId).destination)
                }
            },
            isGridMode = isGridMode,
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
    upcomingPaymentsData: List<UpcomingPaymentData>,
    remainingExpenseThisMonth: String,
    onClickItem: (Int) -> Unit,
    isGridMode: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val listState = rememberLazyStaggeredGridState()
    val gridState = rememberLazyStaggeredGridState()

    LazyVerticalStaggeredGrid(
        columns =
            if (isGridMode) {
                StaggeredGridCells.Adaptive(160.dp)
            } else {
                StaggeredGridCells.Fixed(1)
            },
        state = if (isGridMode) gridState else listState,
        verticalItemSpacing = 8.dp,
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
        contentPadding = contentPadding,
        modifier = modifier.fillMaxWidth(),
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            UpcomingPaymentsSummary(
                remainingExpenseThisMonth = remainingExpenseThisMonth,
            )
        }
        items(items = upcomingPaymentsData) { upcomingPaymentData ->
            if (isGridMode) {
                GridUpcomingPayment(
                    upcomingPaymentData = upcomingPaymentData,
                    onClickItem = {
                        onClickItem(upcomingPaymentData.id)
                    },
                )
            } else {
                UpcomingPayment(
                    upcomingPaymentData = upcomingPaymentData,
                    onClickItem = {
                        onClickItem(upcomingPaymentData.id)
                    },
                )
            }
        }

        item(span = StaggeredGridItemSpan.FullLine) {
            Spacer(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(80.dp),
            )
        }
    }
}

@Composable
private fun getUpcomingPaymentTimeString(upcomingPaymentData: UpcomingPaymentData): String {
    val inDaysString =
        when (upcomingPaymentData.nextPaymentRemainingDays) {
            0 -> stringResource(Res.string.upcoming_time_remaining_today)
            1 -> stringResource(Res.string.upcoming_time_remaining_tomorrow)
            else ->
                stringResource(
                    Res.string.upcoming_time_remaining_days,
                    upcomingPaymentData.nextPaymentRemainingDays,
                )
        }
    return inDaysString
}

@Composable
private fun UpcomingPaymentsSummary(
    remainingExpenseThisMonth: String,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(8.dp),
    ) {
        Text(
            text = stringResource(Res.string.upcoming_expenses_remaining_this_month),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = remainingExpenseThisMonth,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun GridUpcomingPayment(
    upcomingPaymentData: UpcomingPaymentData,
    onClickItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inDaysString = getUpcomingPaymentTimeString(upcomingPaymentData)
    Card(
        modifier = modifier.clickable { onClickItem() },
        colors = CardDefaults.cardColors(containerColor = upcomingPaymentData.color.getColor()),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = inDaysString,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = upcomingPaymentData.price.toCurrencyString(),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = upcomingPaymentData.name,
                style = MaterialTheme.typography.titleMedium,
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
    }
}

@Composable
private fun UpcomingPayment(
    upcomingPaymentData: UpcomingPaymentData,
    onClickItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inDaysString = getUpcomingPaymentTimeString(upcomingPaymentData)
    Card(
        modifier = modifier.clickable { onClickItem() },
        colors = CardDefaults.cardColors(containerColor = upcomingPaymentData.color.getColor()),
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
                    style = MaterialTheme.typography.titleMedium,
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
                    style = MaterialTheme.typography.titleLarge,
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
            imageVector = Icons.AutoMirrored.Rounded.List,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
        )
        Text(
            text = stringResource(Res.string.upcoming_placeholder_title),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview
@Composable
private fun UpcomingPaymentsOverviewPreview() {
    val nextPaymentDays1 = 0
    val nextPaymentDate1String = Clock.System.now().toLocaleString()
    val nextPaymentDays2 = 1
    val nextPaymentDate2String = Clock.System.now().toLocaleString()
    val nextPaymentDays3 = 2
    val nextPaymentDate3String = Clock.System.now().toLocaleString()

    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            UpcomingPaymentsOverview(
                upcomingPaymentsData =
                    listOf(
                        UpcomingPaymentData(
                            id = 0,
                            name = "Netflix",
                            price = CurrencyValue(9.99f, "USD"),
                            nextPaymentRemainingDays = nextPaymentDays1,
                            nextPaymentDate = nextPaymentDate1String,
                            color = ExpenseColor.Dynamic,
                        ),
                        UpcomingPaymentData(
                            id = 1,
                            name = "Disney Plus",
                            price = CurrencyValue(5f, "USD"),
                            nextPaymentRemainingDays = nextPaymentDays2,
                            nextPaymentDate = nextPaymentDate2String,
                            color = ExpenseColor.Green,
                        ),
                        UpcomingPaymentData(
                            id = 2,
                            name = "Amazon Prime with a long name",
                            price = CurrencyValue(7.95f, "USD"),
                            nextPaymentRemainingDays = nextPaymentDays3,
                            nextPaymentDate = nextPaymentDate3String,
                            color = ExpenseColor.Pink,
                        ),
                    ),
                remainingExpenseThisMonth = "$100.00",
                onClickItem = {},
                contentPadding = PaddingValues(8.dp),
                isGridMode = false,
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
