package ui

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import data.EditExpensePane
import data.Recurrence
import data.RecurringExpenseData
import model.database.UserPreferencesRepository
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.home_summary_monthly
import recurringexpensetracker.app.generated.resources.home_summary_weekly
import recurringexpensetracker.app.generated.resources.home_summary_yearly
import toCurrencyString
import viewmodel.RecurringExpenseViewModel

@Composable
fun RecurringExpenseOverview(
    isGridMode: Boolean,
    navController: NavController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    recurringExpenseViewModel: RecurringExpenseViewModel = koinViewModel<RecurringExpenseViewModel>(),
    userPreferencesRepository: UserPreferencesRepository = koinInject(),
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
                    onClickItem = {
                        navController.navigate(EditExpensePane(recurringExpenseData.id))
                    },
                )
            } else {
                RecurringExpense(
                    recurringExpenseData = recurringExpenseData,
                    onClickItem = {
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
    onClickItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onClickItem() },
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
                modifier = Modifier.align(Alignment.End),
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
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                    Spacer(modifier = Modifier.weight(1f))
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
    onClickItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onClickItem() },
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
            }
            Column(horizontalAlignment = Alignment.End) {
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

// private class GridLayoutParameterProvider : PreviewParameterProvider<Boolean> {
//    override val values = sequenceOf(false, true)
// }

// @Preview
// @Composable
// private fun RecurringExpenseOverviewPreview(
//    @PreviewParameter(GridLayoutParameterProvider::class) isGridMode: Boolean,
// ) {
//    ExpenseTrackerTheme {
//        Surface(modifier = Modifier.fillMaxWidth()) {
//            RecurringExpenseOverview(
//                weeklyExpense = "4,00 €",
//                monthlyExpense = "16,00 €",
//                yearlyExpense = "192,00 €",
//                recurringExpenseData =
//                    listOf(
//                        RecurringExpenseData(
//                            id = 0,
//                            name = "Netflix",
//                            description = "My Netflix description",
//                            price = CurrencyValue(9.99f, "USD"),
//                            monthlyPrice = CurrencyValue(9.99f, "USD"),
//                            everyXRecurrence = 1,
//                            recurrence = Recurrence.Monthly,
//                            firstPayment = Clock.System.now(),
//                            color = ExpenseColor.Dynamic,
//                            notifyForExpense = true,
//                            notifyXDaysBefore = null,
//                            lastNotificationDate = null,
//                        ),
//                        RecurringExpenseData(
//                            id = 1,
//                            name = "Disney Plus",
//                            description =
//                                "My Disney Plus very very very very very " +
//                                    "very very very very long description",
//                            price = CurrencyValue(5f, "USD"),
//                            monthlyPrice = CurrencyValue(5f, "USD"),
//                            everyXRecurrence = 1,
//                            recurrence = Recurrence.Monthly,
//                            firstPayment = Clock.System.now(),
//                            color = ExpenseColor.Orange,
//                            notifyForExpense = true,
//                            notifyXDaysBefore = null,
//                            lastNotificationDate = null,
//                        ),
//                        RecurringExpenseData(
//                            id = 2,
//                            name = "Amazon Prime with a long name",
//                            description = "",
//                            price = CurrencyValue(7.95f, "USD"),
//                            monthlyPrice = CurrencyValue(7.95f, "USD"),
//                            everyXRecurrence = 1,
//                            recurrence = Recurrence.Monthly,
//                            firstPayment = Clock.System.now(),
//                            color = ExpenseColor.Turquoise,
//                            notifyForExpense = true,
//                            notifyXDaysBefore = null,
//                            lastNotificationDate = null,
//                        ),
//                        RecurringExpenseData(
//                            id = 3,
//                            name = "Yearly Test Subscription",
//                            description = "Test Description with another very long name",
//                            price = CurrencyValue(72f, "USD"),
//                            monthlyPrice = CurrencyValue(6f, "USD"),
//                            everyXRecurrence = 1,
//                            recurrence = Recurrence.Yearly,
//                            firstPayment = Clock.System.now(),
//                            color = ExpenseColor.Dynamic,
//                            notifyForExpense = true,
//                            notifyXDaysBefore = null,
//                            lastNotificationDate = null,
//                        ),
//                    ),
//                isGridMode = isGridMode,
//                navController = rememberNavController(),
//            )
//        }
//    }
// }
