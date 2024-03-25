package de.dbauer.expensetracker.ui

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.data.Recurrence
import de.dbauer.expensetracker.data.RecurringExpenseData
import de.dbauer.expensetracker.toCurrencyString
import de.dbauer.expensetracker.ui.customizations.ExpenseColor
import de.dbauer.expensetracker.ui.theme.ExpenseTrackerTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun RecurringExpenseOverview(
    weeklyExpense: String,
    monthlyExpense: String,
    yearlyExpense: String,
    recurringExpenseData: ImmutableList<RecurringExpenseData>,
    onItemClicked: (RecurringExpenseData) -> Unit,
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
            item(span = StaggeredGridItemSpan.FullLine) {
                RecurringExpenseSummary(
                    weeklyExpense = weeklyExpense,
                    monthlyExpense = monthlyExpense,
                    yearlyExpense = yearlyExpense,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            items(items = recurringExpenseData) { recurringExpenseData ->
                if (targetValue) {
                    GridRecurringExpense(
                        recurringExpenseData = recurringExpenseData,
                        onItemClicked = {
                            onItemClicked(recurringExpenseData)
                        },
                    )
                } else {
                    RecurringExpense(
                        recurringExpenseData = recurringExpenseData,
                        onItemClicked = {
                            onItemClicked(recurringExpenseData)
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
private fun RecurringExpenseSummary(
    weeklyExpense: String,
    monthlyExpense: String,
    yearlyExpense: String,
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
            text = stringResource(R.string.home_summary_monthly),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = monthlyExpense,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.home_summary_weekly),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = weeklyExpense,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.home_summary_yearly),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = yearlyExpense,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun GridRecurringExpense(
    recurringExpenseData: RecurringExpenseData,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onItemClicked() },
        colors = CardDefaults.cardColors(containerColor = recurringExpenseData.color.getColor()),
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
                text = recurringExpenseData.name,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .align(Alignment.CenterHorizontally),
            )
            Text(
                text = recurringExpenseData.monthlyPrice.toCurrencyString(),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .align(Alignment.End),
            )
            if (recurringExpenseData.recurrence != Recurrence.Monthly ||
                recurringExpenseData.everyXRecurrence != 1
            ) {
                Text(
                    text =
                        "${recurringExpenseData.price.toCurrencyString()} / " +
                            "${recurringExpenseData.everyXRecurrence} " +
                            stringResource(id = recurringExpenseData.recurrence.shortStringRes),
                    style = MaterialTheme.typography.bodySmall,
                    modifier =
                        Modifier
                            .align(Alignment.End),
                )
            }
            if (recurringExpenseData.description.isNotBlank()) {
                Text(
                    text = recurringExpenseData.description,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun RecurringExpense(
    recurringExpenseData: RecurringExpenseData,
    onItemClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onItemClicked() },
        colors = CardDefaults.cardColors(containerColor = recurringExpenseData.color.getColor()),
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
                    text = recurringExpenseData.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (recurringExpenseData.description.isNotBlank()) {
                    Text(
                        text = recurringExpenseData.description,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = recurringExpenseData.monthlyPrice.toCurrencyString(),
                    style = MaterialTheme.typography.headlineSmall,
                )
                if (recurringExpenseData.recurrence != Recurrence.Monthly ||
                    recurringExpenseData.everyXRecurrence != 1
                ) {
                    Text(
                        text =
                            "${recurringExpenseData.price.toCurrencyString()} / " +
                                "${recurringExpenseData.everyXRecurrence} " +
                                stringResource(id = recurringExpenseData.recurrence.shortStringRes),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

private class GridLayoutParameterProvider : PreviewParameterProvider<Boolean> {
    override val values = sequenceOf(false, true)
}

@PreviewLightDark
@Composable
private fun RecurringExpenseOverviewPreview(
    @PreviewParameter(GridLayoutParameterProvider::class) isGridMode: Boolean,
) {
    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            RecurringExpenseOverview(
                weeklyExpense = "4,00 €",
                monthlyExpense = "16,00 €",
                yearlyExpense = "192,00 €",
                recurringExpenseData =
                    persistentListOf(
                        RecurringExpenseData(
                            id = 0,
                            name = "Netflix",
                            description = "My Netflix description",
                            price = 9.99f,
                            monthlyPrice = 9.99f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            0L,
                            ExpenseColor.Dynamic,
                        ),
                        RecurringExpenseData(
                            id = 1,
                            name = "Disney Plus",
                            description =
                                "My Disney Plus very very very very very " +
                                    "very very very very long description",
                            price = 5f,
                            monthlyPrice = 5f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            1L,
                            ExpenseColor.Orange,
                        ),
                        RecurringExpenseData(
                            id = 2,
                            name = "Amazon Prime with a long name",
                            description = "",
                            price = 7.95f,
                            monthlyPrice = 7.95f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            2L,
                            ExpenseColor.Turquoise,
                        ),
                        RecurringExpenseData(
                            id = 3,
                            name = "Yearly Test Subscription",
                            description = "Test Description with another very long name",
                            price = 72f,
                            monthlyPrice = 6f,
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Yearly,
                            3L,
                            ExpenseColor.Dynamic,
                        ),
                    ),
                onItemClicked = {},
                isGridMode = isGridMode,
            )
        }
    }
}
