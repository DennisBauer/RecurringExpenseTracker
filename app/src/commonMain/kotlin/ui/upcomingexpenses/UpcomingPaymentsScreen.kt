package ui.upcomingexpenses

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import data.RecurringExpenseData
import data.UpcomingPaymentData
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.upcoming_placeholder_title
import recurringexpensetracker.app.generated.resources.upcoming_time_remaining_days
import recurringexpensetracker.app.generated.resources.upcoming_time_remaining_today
import recurringexpensetracker.app.generated.resources.upcoming_time_remaining_tomorrow
import toCurrencyString
import toLocaleString
import ui.customizations.ExpenseColor
import ui.theme.ExpenseTrackerTheme
import viewmodel.UpcomingPaymentsViewModel

@Composable
fun UpcomingPaymentsScreen(
    upcomingPaymentsViewModel: UpcomingPaymentsViewModel,
    onItemClicked: (RecurringExpenseData) -> Unit,
    isGridMode: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    if (upcomingPaymentsViewModel.upcomingPaymentsData.size > 0) {
        UpcomingPaymentsOverview(
            upcomingPaymentsData = upcomingPaymentsViewModel.upcomingPaymentsData,
            onItemClicked = {
                upcomingPaymentsViewModel.onExpenseWithIdClicked(it, onItemClicked)
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
    onItemClicked: (Int) -> Unit,
    isGridMode: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val fadeDuration: Int = 700

    val listState = rememberLazyStaggeredGridState()
    val gridState = rememberLazyStaggeredGridState()

    AnimatedContent(
        targetState = isGridMode,
        transitionSpec = {
            ContentTransform(
                fadeIn(
                    animationSpec = tween(durationMillis = fadeDuration),
                    initialAlpha = 0.0f,
                ),
                fadeOut(
                    animationSpec = tween(durationMillis = fadeDuration),
                    targetAlpha = 0.0f,
                ),
                sizeTransform = null,
            )
        },
        label = "Animates between row mode and grid mode",
    ) { targetValue ->
        LazyVerticalStaggeredGrid(
            columns =
                if (targetValue) {
                    StaggeredGridCells.Adaptive(160.dp)
                } else {
                    StaggeredGridCells.Fixed(1)
                },
            state = if (targetValue) gridState else listState,
            verticalItemSpacing = 8.dp,
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            contentPadding = contentPadding,
            modifier = modifier.fillMaxWidth(),
        ) {
            items(items = upcomingPaymentsData) { upcomingPaymentData ->
                if (targetValue) {
                    GridUpcomingPayment(
                        upcomingPaymentData = upcomingPaymentData,
                        onItemClicked = {
                            onItemClicked(upcomingPaymentData.id)
                        },
                    )
                } else {
                    UpcomingPayment(
                        upcomingPaymentData = upcomingPaymentData,
                        onItemClicked = {
                            onItemClicked(upcomingPaymentData.id)
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
private fun GridUpcomingPayment(
    upcomingPaymentData: UpcomingPaymentData,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inDaysString = getUpcomingPaymentTimeString(upcomingPaymentData)
    Card(
        modifier = modifier.clickable { onItemClicked() },
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
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = upcomingPaymentData.price.toCurrencyString(),
                style = MaterialTheme.typography.headlineSmall,
            )
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
    }
}

@Composable
private fun UpcomingPayment(
    upcomingPaymentData: UpcomingPaymentData,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val inDaysString = getUpcomingPaymentTimeString(upcomingPaymentData)
    Card(
        modifier = modifier.clickable { onItemClicked() },
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
                            price = 9.99f,
                            nextPaymentRemainingDays = nextPaymentDays1,
                            nextPaymentDate = nextPaymentDate1String,
                            color = ExpenseColor.Dynamic,
                        ),
                        UpcomingPaymentData(
                            id = 1,
                            name = "Disney Plus",
                            price = 5f,
                            nextPaymentRemainingDays = nextPaymentDays2,
                            nextPaymentDate = nextPaymentDate2String,
                            color = ExpenseColor.Green,
                        ),
                        UpcomingPaymentData(
                            id = 2,
                            name = "Amazon Prime with a long name",
                            price = 7.95f,
                            nextPaymentRemainingDays = nextPaymentDays3,
                            nextPaymentDate = nextPaymentDate3String,
                            color = ExpenseColor.Pink,
                        ),
                    ),
                onItemClicked = {},
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