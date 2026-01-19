package de.dbauer.expensetracker.shared.ui

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import de.dbauer.expensetracker.shared.data.EditExpensePane
import de.dbauer.expensetracker.shared.data.Recurrence
import de.dbauer.expensetracker.shared.data.RecurringExpenseData
import de.dbauer.expensetracker.shared.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.shared.toCurrencyString
import de.dbauer.expensetracker.shared.ui.home.HorizontalAssignedTagColorsList
import de.dbauer.expensetracker.shared.ui.home.HorizontalAssignedTagList
import de.dbauer.expensetracker.shared.ui.theme.ExpenseTrackerThemePreview
import de.dbauer.expensetracker.shared.viewmodel.RecurringExpenseViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.shared.generated.resources.Res
import recurringexpensetracker.shared.generated.resources.home_summary_monthly
import recurringexpensetracker.shared.generated.resources.home_summary_weekly
import recurringexpensetracker.shared.generated.resources.home_summary_yearly

@Composable
fun RecurringExpenseOverview(
    isGridMode: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    recurringExpenseViewModel: RecurringExpenseViewModel = koinViewModel<RecurringExpenseViewModel>(),
    userPreferencesRepository: IUserPreferencesRepository = koinInject(),
) {
    val listState = rememberLazyStaggeredGridState()
    val gridState = rememberLazyStaggeredGridState()

    val currencyCode by userPreferencesRepository.defaultCurrency.collectAsState()
    val weeklyExpense =
        recurringExpenseViewModel.currencyPrefix +
            recurringExpenseViewModel.weeklyExpense.toCurrencyString(currencyCode)
    val monthlyExpense =
        recurringExpenseViewModel.currencyPrefix +
            recurringExpenseViewModel.monthlyExpense.toCurrencyString(currencyCode)
    val yearlyExpense =
        recurringExpenseViewModel.currencyPrefix +
            recurringExpenseViewModel.yearlyExpense.toCurrencyString(currencyCode)

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
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            RecurringExpenseSummary(
                weeklyExpense = weeklyExpense,
                monthlyExpense = monthlyExpense,
                yearlyExpense = yearlyExpense,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        items(items = recurringExpenseViewModel.recurringExpenseData) { recurringExpenseData ->
            if (isGridMode) {
                GridRecurringExpense(
                    recurringExpenseData = recurringExpenseData,
                    onClick = {
                        navController.navigate(EditExpensePane(recurringExpenseData.id))
                    },
                )
            } else {
                RecurringExpense(
                    recurringExpenseData = recurringExpenseData,
                    onClick = {
                        navController.navigate(EditExpensePane(recurringExpenseData.id))
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
            text = stringResource(Res.string.home_summary_monthly),
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
                    text = stringResource(Res.string.home_summary_weekly),
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
                    text = stringResource(Res.string.home_summary_yearly),
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
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
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (recurringExpenseData.description.isNotBlank()) {
                Text(
                    text = recurringExpenseData.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End),
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(end = 4.dp),
                ) {
                    if (recurringExpenseData.recurrence != Recurrence.Monthly ||
                        recurringExpenseData.everyXRecurrence != 1
                    ) {
                        Text(
                            text =
                                "${recurringExpenseData.price.toCurrencyString()} / " +
                                    "${recurringExpenseData.everyXRecurrence} " +
                                    stringResource(recurringExpenseData.recurrence.shortStringRes),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                    }
                    HorizontalAssignedTagColorsList(
                        tags = recurringExpenseData.tags,
                    )
                }
                Text(
                    text = recurringExpenseData.monthlyPrice.toCurrencyString(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RecurringExpense(
    recurringExpenseData: RecurringExpenseData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .padding(horizontal = 16.dp)
                    // The conditional padding needed to work around the inner padding of the FilterChip of the tag
                    .padding(top = 16.dp, bottom = if (recurringExpenseData.tags.isEmpty()) 16.dp else 8.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(end = 16.dp)
                        .weight(1f),
            ) {
                Text(
                    text = recurringExpenseData.name,
                    style = MaterialTheme.typography.titleMedium,
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
                HorizontalAssignedTagList(
                    tags = recurringExpenseData.tags,
                    onTagClick = { onClick() },
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier =
                    Modifier
                        // The conditional padding needed to work around the inner padding of the FilterChip of the tag
                        .padding(bottom = if (recurringExpenseData.tags.isEmpty()) 0.dp else 8.dp),
            ) {
                Text(
                    text = recurringExpenseData.monthlyPrice.toCurrencyString(),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (recurringExpenseData.recurrence != Recurrence.Monthly ||
                    recurringExpenseData.everyXRecurrence != 1
                ) {
                    Text(
                        text =
                            "${recurringExpenseData.price.toCurrencyString()} / " +
                                "${recurringExpenseData.everyXRecurrence} " +
                                stringResource(recurringExpenseData.recurrence.shortStringRes),
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

@Preview
@Composable
private fun RecurringExpenseOverviewPreview(
    @PreviewParameter(GridLayoutParameterProvider::class) isGridMode: Boolean,
) {
    ExpenseTrackerThemePreview {
        Surface(modifier = Modifier.fillMaxWidth()) {
            RecurringExpenseOverview(
                isGridMode = isGridMode,
                navController = rememberNavController(),
            )
        }
    }
}
