package de.dbauer.expensetracker.shared.ui.upcomingexpenses

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.EditExpensePane
import de.dbauer.expensetracker.shared.data.NavRoute
import de.dbauer.expensetracker.shared.data.Tag
import de.dbauer.expensetracker.shared.data.UpcomingPaymentData
import de.dbauer.expensetracker.shared.toLocaleString
import de.dbauer.expensetracker.shared.ui.home.HorizontalAssignedTagColorsList
import de.dbauer.expensetracker.shared.ui.home.HorizontalAssignedTagList
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.shared.viewmodel.UpcomingPayment
import de.dbauer.expensetracker.shared.viewmodel.UpcomingPaymentsViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.upcoming_placeholder_title
import recurringexpensetracker.shared.generated.resources.upcoming_time_remaining_days
import recurringexpensetracker.shared.generated.resources.upcoming_time_remaining_today
import recurringexpensetracker.shared.generated.resources.upcoming_time_remaining_tomorrow
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun UpcomingPaymentsScreen(
    isGridMode: Boolean,
    navigateTo: (NavRoute) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel = koinViewModel<UpcomingPaymentsViewModel>(),
) {
    if (upcomingPaymentsViewModel.upcomingPaymentsData.isNotEmpty()) {
        UpcomingPaymentsOverview(
            upcomingPaymentsData = upcomingPaymentsViewModel.upcomingPaymentsData,
            onClickItem = { expenseId ->
                upcomingPaymentsViewModel.onExpenseWithIdClicked(expenseId) {
                    navigateTo(NavRoute.EditExpensePane(expenseId))
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
    upcomingPaymentsData: List<UpcomingPayment>,
    onClickItem: (Int) -> Unit,
    isGridMode: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val listState = rememberLazyStaggeredGridState()

    val firstVisibleItem by remember {
        derivedStateOf {
            upcomingPaymentsData[listState.firstVisibleItemIndex]
        }
    }

    Column(modifier = modifier) {
        UpcomingPaymentsSummary(
            month = firstVisibleItem.month,
            remainingExpenseThisMonth = firstVisibleItem.paymentsSum,
            scrolledDown = listState.canScrollBackward,
        )
        LazyVerticalStaggeredGrid(
            columns =
                if (isGridMode) {
                    StaggeredGridCells.Adaptive(160.dp)
                } else {
                    StaggeredGridCells.Fixed(1)
                },
            state = listState,
            verticalItemSpacing = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(
                items = upcomingPaymentsData,
                key = { entry ->
                    "expense_${entry.month}_${entry.payment?.id}_${entry.payment?.nextPaymentDate}"
                },
                span = { entry ->
                    if (entry.payment == null) {
                        StaggeredGridItemSpan.FullLine
                    } else {
                        StaggeredGridItemSpan.SingleLane
                    }
                },
            ) { entry ->
                if (entry.payment == null) {
                    Text(
                        text = entry.month,
                        style = MaterialTheme.typography.titleMedium,
                    )
                } else {
                    if (isGridMode) {
                        GridUpcomingPayment(
                            upcomingPaymentData = entry.payment,
                            onClickItem = {
                                onClickItem(entry.payment.id)
                            },
                        )
                    } else {
                        UpcomingPayment(
                            upcomingPaymentData = entry.payment,
                            onClickItem = {
                                onClickItem(entry.payment.id)
                            },
                        )
                    }
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
}

@Composable
private fun getUpcomingPaymentTimeString(upcomingPaymentData: UpcomingPaymentData): String {
    val inDaysString =
        when (upcomingPaymentData.nextPaymentRemainingDays) {
            0 -> {
                stringResource(Res.string.upcoming_time_remaining_today)
            }

            1 -> {
                stringResource(Res.string.upcoming_time_remaining_tomorrow)
            }

            else -> {
                stringResource(
                    Res.string.upcoming_time_remaining_days,
                    upcomingPaymentData.nextPaymentRemainingDays,
                )
            }
        }
    return inDaysString
}

@Composable
private fun UpcomingPaymentsSummary(
    month: String,
    remainingExpenseThisMonth: String,
    scrolledDown: Boolean,
    modifier: Modifier = Modifier,
) {
    val shadowElevation = if (scrolledDown) 4.dp else 0.dp
    Surface(
        shadowElevation = shadowElevation,
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
        ) {
            Text(
                text = month,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = remainingExpenseThisMonth,
                style = MaterialTheme.typography.titleMedium,
            )
        }
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
    ) {
        Column(
            modifier =
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = inDaysString,
                    style = MaterialTheme.typography.bodyMedium,
                )
                HorizontalAssignedTagColorsList(
                    tags = upcomingPaymentData.tags,
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    modifier =
                        Modifier
                            .padding(start = 4.dp)
                            .weight(1f),
                )
            }
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
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .padding(horizontal = 16.dp)
                    // The conditional padding needed to work around the inner padding of the FilterChip of the tag
                    .padding(top = 16.dp, bottom = if (upcomingPaymentData.tags.isEmpty()) 16.dp else 8.dp),
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
                HorizontalAssignedTagList(
                    tags = upcomingPaymentData.tags,
                    onTagClick = { onClickItem() },
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier =
                    Modifier
                        // The conditional padding needed to work around the inner padding of the FilterChip of the tag
                        .padding(bottom = if (upcomingPaymentData.tags.isEmpty()) 0.dp else 8.dp),
            ) {
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

@OptIn(ExperimentalUuidApi::class)
@Preview
@Composable
private fun UpcomingPaymentsOverviewPreview() {
    val nextPaymentDays1 = 0
    val nextPaymentDate1String = Clock.System.now().toLocaleString()
    val nextPaymentDays2 = 1
    val nextPaymentDate2String = Clock.System.now().toLocaleString()
    val nextPaymentDays3 = 2
    val nextPaymentDate3String = Clock.System.now().toLocaleString()

    ExpenseTrackerThemePreview {
        Surface(modifier = Modifier.fillMaxSize()) {
            UpcomingPaymentsOverview(
                upcomingPaymentsData =
                    listOf(
                        UpcomingPayment(
                            month = "January",
                            paymentsSum = "22,94 €",
                            payment = null,
                        ),
                        UpcomingPayment(
                            month = "January",
                            paymentsSum = "22,94 €",
                            payment =
                                UpcomingPaymentData(
                                    id = 0,
                                    name = "Netflix",
                                    price = CurrencyValue(9.99f, "USD"),
                                    nextPaymentRemainingDays = nextPaymentDays1,
                                    nextPaymentDate = nextPaymentDate1String,
                                    tags =
                                        listOf(
                                            Tag("Tag 1", 0xFFFF00FF, id = Uuid.random().hashCode()),
                                            Tag("Tag 2", 0xFFFF00F0, id = Uuid.random().hashCode()),
                                            Tag("Tag 3", 0xFF80FF80, id = Uuid.random().hashCode()),
                                        ),
                                ),
                        ),
                        UpcomingPayment(
                            month = "January",
                            paymentsSum = "22,94 €",
                            payment =
                                UpcomingPaymentData(
                                    id = 1,
                                    name = "Disney Plus",
                                    price = CurrencyValue(5f, "USD"),
                                    nextPaymentRemainingDays = nextPaymentDays2,
                                    nextPaymentDate = nextPaymentDate2String,
                                    tags =
                                        listOf(
                                            Tag("Tag 2", 0xFFFF00F0, id = Uuid.random().hashCode()),
                                        ),
                                ),
                        ),
                        UpcomingPayment(
                            month = "January",
                            paymentsSum = "22,94 €",
                            payment =
                                UpcomingPaymentData(
                                    id = 2,
                                    name = "Amazon Prime with a long name",
                                    price = CurrencyValue(7.95f, "USD"),
                                    nextPaymentRemainingDays = nextPaymentDays3,
                                    nextPaymentDate = nextPaymentDate3String,
                                    tags =
                                        listOf(
                                            Tag("Tag 3", 0xFF80FF80, id = Uuid.random().hashCode()),
                                        ),
                                ),
                        ),
                    ),
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
    ExpenseTrackerThemePreview {
        Surface(modifier = Modifier.fillMaxSize()) {
            UpcomingPaymentsOverviewPlaceholder()
        }
    }
}
