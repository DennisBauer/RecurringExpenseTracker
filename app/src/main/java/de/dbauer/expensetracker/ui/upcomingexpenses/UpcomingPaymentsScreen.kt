package de.dbauer.expensetracker.ui.upcomingexpenses

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.data.UpcomingPaymentData
import de.dbauer.expensetracker.helper.UtcDateFormat
import de.dbauer.expensetracker.toCurrencyString
import de.dbauer.expensetracker.ui.customizations.ExpenseColor
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import de.dbauer.expensetracker.viewmodel.UpcomingPaymentsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.util.Date
import java.util.concurrent.TimeUnit

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
    upcomingPaymentsData: ImmutableList<UpcomingPaymentData>,
    onItemClicked: (Int) -> Unit,
    isGridMode: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val fadeDuration: Int = integerResource(id = R.integer.overview_list_grid_toggle_fade_anim_duration)

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
                    StaggeredGridCells.Adaptive(dimensionResource(id = R.dimen.overview_list_grid_cell_min_width))
                } else {
                    StaggeredGridCells.Fixed(1)
                },
            state = if (targetValue) gridState else listState,
            verticalItemSpacing = dimensionResource(id = R.dimen.overview_list_grid_vertical_item_spacing),
            horizontalArrangement =
                Arrangement.spacedBy(
                    dimensionResource(id = R.dimen.overview_list_grid_horizontal_arrangement_space_by),
                ),
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
                            .height(dimensionResource(id = R.dimen.overview_list_grid_spacer_height)),
                )
            }
        }
    }
}

@Composable
private fun getUpcomingPaymentTimeString(upcomingPaymentData: UpcomingPaymentData): String {
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
            text = stringResource(id = R.string.upcoming_placeholder_title),
            textAlign = TextAlign.Center,
        )
    }
}

@PreviewLightDark
@Composable
private fun UpcomingPaymentsOverviewPreview() {
    val dateFormat = UtcDateFormat.getDateInstance()

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

@PreviewLightDark
@Composable
private fun UpcomingPaymentsOverviewPlaceholderPreview() {
    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            UpcomingPaymentsOverviewPlaceholder()
        }
    }
}
