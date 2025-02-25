package ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import data.CurrencyValue
import data.EditExpensePane
import data.Recurrence
import data.RecurringExpenseData
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.edit_expense_button_add
import recurringexpensetracker.app.generated.resources.home_summary_monthly
import recurringexpensetracker.app.generated.resources.home_summary_weekly
import recurringexpensetracker.app.generated.resources.home_summary_yearly
import recurringexpensetracker.app.generated.resources.home_title
import ui.customizations.ExpenseColor
import ui.theme.ExpenseTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpenseOverview(
    weeklyExpense: String,
    monthlyExpense: String,
    yearlyExpense: String,
    recurringExpenseData: List<RecurringExpenseData>,
    isGridMode: Boolean,
    onToggleGridMode: () -> Unit,
    navController: NavController,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val fadeDuration = 700

    val listState = rememberLazyStaggeredGridState()
    val gridState = rememberLazyStaggeredGridState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.home_title),
                    )
                },
                actions = {
                    ToggleGridModeButton(
                        onToggleGridMode = onToggleGridMode,
                        isGridMode = isGridMode,
                    )
                },
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(EditExpensePane().destination)
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription =
                        stringResource(Res.string.edit_expense_button_add),
                )
            }
        },
        content = { paddingValues ->
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(paddingValues),
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
                                onClickItem = {
                                    navController.navigate(EditExpensePane(recurringExpenseData.id).destination)
                                },
                            )
                        } else {
                            RecurringExpense(
                                recurringExpenseData = recurringExpenseData,
                                onClickItem = {
                                    navController.navigate(EditExpensePane(recurringExpenseData.id).destination)
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
        },
    )
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
                            stringResource(recurringExpenseData.recurrence.shortStringRes),
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
    ExpenseTrackerTheme {
        Surface(modifier = Modifier.fillMaxWidth()) {
            RecurringExpenseOverview(
                weeklyExpense = "4,00 €",
                monthlyExpense = "16,00 €",
                yearlyExpense = "192,00 €",
                recurringExpenseData =
                    listOf(
                        RecurringExpenseData(
                            id = 0,
                            name = "Netflix",
                            description = "My Netflix description",
                            price = CurrencyValue(9.99f, "USD"),
                            monthlyPrice = CurrencyValue(9.99f, "USD"),
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            firstPayment = Clock.System.now(),
                            color = ExpenseColor.Dynamic,
                            notifyForExpense = true,
                            notifyXDaysBefore = null,
                            lastNotificationDate = null,
                        ),
                        RecurringExpenseData(
                            id = 1,
                            name = "Disney Plus",
                            description =
                                "My Disney Plus very very very very very " +
                                    "very very very very long description",
                            price = CurrencyValue(5f, "USD"),
                            monthlyPrice = CurrencyValue(5f, "USD"),
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            firstPayment = Clock.System.now(),
                            color = ExpenseColor.Orange,
                            notifyForExpense = true,
                            notifyXDaysBefore = null,
                            lastNotificationDate = null,
                        ),
                        RecurringExpenseData(
                            id = 2,
                            name = "Amazon Prime with a long name",
                            description = "",
                            price = CurrencyValue(7.95f, "USD"),
                            monthlyPrice = CurrencyValue(7.95f, "USD"),
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Monthly,
                            firstPayment = Clock.System.now(),
                            color = ExpenseColor.Turquoise,
                            notifyForExpense = true,
                            notifyXDaysBefore = null,
                            lastNotificationDate = null,
                        ),
                        RecurringExpenseData(
                            id = 3,
                            name = "Yearly Test Subscription",
                            description = "Test Description with another very long name",
                            price = CurrencyValue(72f, "USD"),
                            monthlyPrice = CurrencyValue(6f, "USD"),
                            everyXRecurrence = 1,
                            recurrence = Recurrence.Yearly,
                            firstPayment = Clock.System.now(),
                            color = ExpenseColor.Dynamic,
                            notifyForExpense = true,
                            notifyXDaysBefore = null,
                            lastNotificationDate = null,
                        ),
                    ),
                isGridMode = isGridMode,
                onToggleGridMode = { },
                navController = rememberNavController(),
            )
        }
    }
}
