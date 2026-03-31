package de.dbauer.expensetracker.shared.ui.upcomingexpenses

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
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.dbauer.expensetracker.shared.data.CurrencyValue
import de.dbauer.expensetracker.shared.data.EditExpensePane
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
import recurringexpensetracker.shared.generated.resources.upcoming_mark_as_paid
import recurringexpensetracker.shared.generated.resources.upcoming_mark_as_unpaid
import recurringexpensetracker.shared.generated.resources.upcoming_paid_section
import recurringexpensetracker.shared.generated.resources.upcoming_placeholder_title
import recurringexpensetracker.shared.generated.resources.upcoming_section_upcoming
import recurringexpensetracker.shared.generated.resources.upcoming_time_overdue_days
import recurringexpensetracker.shared.generated.resources.upcoming_time_overdue_today
import recurringexpensetracker.shared.generated.resources.upcoming_time_past_days
import recurringexpensetracker.shared.generated.resources.upcoming_time_remaining_days
import recurringexpensetracker.shared.generated.resources.upcoming_time_remaining_today
import recurringexpensetracker.shared.generated.resources.upcoming_time_remaining_tomorrow
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
            upcomingStartIndex = upcomingPaymentsViewModel.upcomingStartIndex,
            onClickItem = { expenseId ->
                upcomingPaymentsViewModel.onExpenseWithIdClicked(expenseId) {
                    navController.navigate(EditExpensePane(expenseId))
                }
            },
            onMarkAsPaid = { expenseId, paymentDateEpoch ->
                upcomingPaymentsViewModel.markAsPaid(expenseId, paymentDateEpoch)
            },
            onMarkAsUnpaid = { expenseId, paymentDateEpoch ->
                upcomingPaymentsViewModel.markAsUnpaid(expenseId, paymentDateEpoch)
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
    upcomingStartIndex: Int,
    onClickItem: (Int) -> Unit,
    onMarkAsPaid: (Int, Long) -> Unit,
    onMarkAsUnpaid: (Int, Long) -> Unit,
    isGridMode: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val listState =
        rememberLazyStaggeredGridState(
            initialFirstVisibleItemIndex = upcomingStartIndex,
        )

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
                    when (entry) {
                        is UpcomingPayment.MonthHeader -> {
                            val section = if (entry.isPastSection) "past_" else ""
                            "header_${section}${entry.month}"
                        }

                        is UpcomingPayment.PaymentItem -> {
                            "expense_${entry.month}_${entry.payment.id}_${entry.payment.nextPaymentDate}"
                        }

                        is UpcomingPayment.PaidDivider -> {
                            val section = if (entry.isPastSection) "past_" else ""
                            "paid_divider_${section}${entry.month}"
                        }

                        is UpcomingPayment.UpcomingDivider -> {
                            "upcoming_divider"
                        }
                    }
                },
                span = { entry ->
                    when (entry) {
                        is UpcomingPayment.MonthHeader -> StaggeredGridItemSpan.FullLine
                        is UpcomingPayment.PaidDivider -> StaggeredGridItemSpan.FullLine
                        is UpcomingPayment.UpcomingDivider -> StaggeredGridItemSpan.FullLine
                        is UpcomingPayment.PaymentItem -> StaggeredGridItemSpan.SingleLane
                    }
                },
            ) { entry ->
                when (entry) {
                    is UpcomingPayment.MonthHeader -> {
                        Text(
                            text = entry.month,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    is UpcomingPayment.PaidDivider -> {
                        PaidSectionDivider()
                    }

                    is UpcomingPayment.UpcomingDivider -> {
                        UpcomingSectionDivider()
                    }

                    is UpcomingPayment.PaymentItem -> {
                        if (isGridMode) {
                            GridUpcomingPayment(
                                upcomingPaymentData = entry.payment,
                                onClick = {
                                    onClickItem(entry.payment.id)
                                },
                                onMarkAsPaid = {
                                    onMarkAsPaid(entry.payment.id, entry.payment.paymentDateEpoch)
                                },
                                onMarkAsUnpaid = {
                                    onMarkAsUnpaid(entry.payment.id, entry.payment.paymentDateEpoch)
                                },
                            )
                        } else {
                            UpcomingPaymentItem(
                                upcomingPaymentData = entry.payment,
                                onClick = {
                                    onClickItem(entry.payment.id)
                                },
                                onMarkAsPaid = {
                                    onMarkAsPaid(entry.payment.id, entry.payment.paymentDateEpoch)
                                },
                                onMarkAsUnpaid = {
                                    onMarkAsUnpaid(entry.payment.id, entry.payment.paymentDateEpoch)
                                },
                            )
                        }
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
        when {
            upcomingPaymentData.isPaid -> {
                stringResource(Res.string.upcoming_paid_section)
            }

            upcomingPaymentData.nextPaymentRemainingDays < 0 && !upcomingPaymentData.requiresConfirmation -> {
                stringResource(
                    Res.string.upcoming_time_past_days,
                    -upcomingPaymentData.nextPaymentRemainingDays,
                )
            }

            upcomingPaymentData.nextPaymentRemainingDays < 0 -> {
                stringResource(
                    Res.string.upcoming_time_overdue_days,
                    -upcomingPaymentData.nextPaymentRemainingDays,
                )
            }

            upcomingPaymentData.nextPaymentRemainingDays == 0 -> {
                if (upcomingPaymentData.requiresConfirmation) {
                    stringResource(Res.string.upcoming_time_overdue_today)
                } else {
                    stringResource(Res.string.upcoming_time_remaining_today)
                }
            }

            upcomingPaymentData.nextPaymentRemainingDays == 1 -> {
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
private fun PaidSectionDivider(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(Res.string.upcoming_paid_section),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
}

@Composable
private fun UpcomingSectionDivider(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(Res.string.upcoming_section_upcoming),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
    }
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
    onClick: () -> Unit,
    onMarkAsPaid: () -> Unit,
    onMarkAsUnpaid: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inDaysString = getUpcomingPaymentTimeString(upcomingPaymentData)
    val cardColors =
        if (upcomingPaymentData.isOverdue) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            )
        } else {
            CardDefaults.cardColors()
        }
    Card(
        onClick = onClick,
        colors = cardColors,
        modifier =
            modifier.then(
                if (upcomingPaymentData.isPaid) Modifier.alpha(0.6f) else Modifier,
            ),
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
                    color =
                        if (upcomingPaymentData.isOverdue) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
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
                textDecoration =
                    if (upcomingPaymentData.isPaid) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
            )
            Text(
                text = upcomingPaymentData.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textDecoration =
                    if (upcomingPaymentData.isPaid) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = upcomingPaymentData.nextPaymentDate,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (upcomingPaymentData.requiresConfirmation) {
                    if (upcomingPaymentData.isPaid) {
                        IconButton(onClick = onMarkAsUnpaid) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Undo,
                                contentDescription = stringResource(Res.string.upcoming_mark_as_unpaid),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        IconButton(onClick = onMarkAsPaid) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircleOutline,
                                contentDescription = stringResource(Res.string.upcoming_mark_as_paid),
                                tint =
                                    if (upcomingPaymentData.isOverdue) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpcomingPaymentItem(
    upcomingPaymentData: UpcomingPaymentData,
    onClick: () -> Unit,
    onMarkAsPaid: () -> Unit,
    onMarkAsUnpaid: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inDaysString = getUpcomingPaymentTimeString(upcomingPaymentData)
    val cardColors =
        if (upcomingPaymentData.isOverdue) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            )
        } else {
            CardDefaults.cardColors()
        }
    Card(
        onClick = onClick,
        colors = cardColors,
        modifier =
            modifier.then(
                if (upcomingPaymentData.isPaid) Modifier.alpha(0.6f) else Modifier,
            ),
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
                    textDecoration =
                        if (upcomingPaymentData.isPaid) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                )
                Text(
                    text = upcomingPaymentData.nextPaymentDate,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                HorizontalAssignedTagList(
                    tags = upcomingPaymentData.tags,
                    onTagClick = { onClick() },
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
                    textDecoration =
                        if (upcomingPaymentData.isPaid) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        },
                )
                Text(
                    text = inDaysString,
                    style = MaterialTheme.typography.bodyLarge,
                    color =
                        if (upcomingPaymentData.isOverdue) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                )
                if (upcomingPaymentData.requiresConfirmation) {
                    if (upcomingPaymentData.isPaid) {
                        IconButton(onClick = onMarkAsUnpaid) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Undo,
                                contentDescription = stringResource(Res.string.upcoming_mark_as_unpaid),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    } else {
                        IconButton(onClick = onMarkAsPaid) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircleOutline,
                                contentDescription = stringResource(Res.string.upcoming_mark_as_paid),
                                tint =
                                    if (upcomingPaymentData.isOverdue) {
                                        MaterialTheme.colorScheme.onErrorContainer
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    },
                            )
                        }
                    }
                }
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
                        UpcomingPayment.UpcomingDivider(
                            month = "January",
                            paymentsSum = "22,94 €",
                        ),
                        UpcomingPayment.MonthHeader(
                            month = "January",
                            paymentsSum = "22,94 €",
                        ),
                        UpcomingPayment.PaymentItem(
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
                        UpcomingPayment.PaymentItem(
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
                        UpcomingPayment.PaymentItem(
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
                                    requiresConfirmation = true,
                                ),
                        ),
                        UpcomingPayment.PaidDivider(
                            month = "January",
                            paymentsSum = "22,94 €",
                        ),
                        UpcomingPayment.PaymentItem(
                            month = "January",
                            paymentsSum = "22,94 €",
                            payment =
                                UpcomingPaymentData(
                                    id = 3,
                                    name = "Spotify (Paid)",
                                    price = CurrencyValue(9.99f, "USD"),
                                    nextPaymentRemainingDays = -2,
                                    nextPaymentDate = nextPaymentDate1String,
                                    tags = emptyList(),
                                    requiresConfirmation = true,
                                    isPaid = true,
                                ),
                        ),
                    ),
                onClickItem = {},
                onMarkAsPaid = { _, _ -> },
                onMarkAsUnpaid = { _, _ -> },
                contentPadding = PaddingValues(8.dp),
                isGridMode = false,
                upcomingStartIndex = 0,
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
