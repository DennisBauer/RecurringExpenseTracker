package de.dbauer.expensetracker.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.lazy.GridCells
import androidx.glance.appwidget.lazy.LazyVerticalGrid
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import asString
import data.CurrencyValue
import data.UpcomingPaymentData
import de.dbauer.expensetracker.MainActivity
import de.dbauer.expensetracker.R
import model.datastore.IUserPreferencesRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.upcoming_title
import ui.customizations.ExpenseColor
import ui.theme.widget.ExpenseTrackerWidgetTheme

class UpcomingPaymentsWidget : GlanceAppWidget(), KoinComponent {
    private val upcomingPayment by inject<UpcomingPaymentsWidgetModel>()
    private val preferences by inject<IUserPreferencesRepository>()

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val upcomingPaymentsTitle = Res.string.upcoming_title.asString()
        provideContent {
            LaunchedEffect(Unit) {
                upcomingPayment.init()
            }
            Content(
                items = upcomingPayment.upcomingPaymentsData,
                gridMode = preferences.gridMode.collectAsState().value,
                upcomingPaymentsTitle = upcomingPaymentsTitle,
                isBackgroundTransparent = preferences.widgetBackgroundTransparent.collectAsState().value,
            )
        }
    }

    override suspend fun providePreview(
        context: Context,
        widgetCategory: Int,
    ) {
        val previewItems =
            listOf<UpcomingPaymentData>(
                UpcomingPaymentData(
                    id = 0,
                    name = "Expense",
                    price =
                        CurrencyValue(
                            value = 10f,
                            currencyCode = "USD",
                            isExchanged = false,
                        ),
                    nextPaymentRemainingDays = 20,
                    nextPaymentDate = "2025-06-06",
                    color = ExpenseColor.Red,
                ),
                UpcomingPaymentData(
                    id = 1,
                    name = "Second Expense",
                    price =
                        CurrencyValue(
                            value = 10.9f,
                            currencyCode = "USD",
                            isExchanged = true,
                        ),
                    nextPaymentRemainingDays = 20,
                    nextPaymentDate = "2025-06-06",
                    color = ExpenseColor.Red,
                ),
            )
        val gridMode = false
        val upcomingPaymentsTitle = Res.string.upcoming_title.asString()

        provideContent {
            Content(
                items = previewItems,
                gridMode = gridMode,
                upcomingPaymentsTitle = upcomingPaymentsTitle,
                isBackgroundTransparent = false,
            )
        }
    }
}

private val transparentColorProvider =
    object : ColorProvider {
        override fun getColor(context: Context): Color {
            return Color.Transparent
        }
    }

@OptIn(ExperimentalGlanceApi::class)
@Composable
private fun Content(
    items: List<UpcomingPaymentData>,
    gridMode: Boolean,
    upcomingPaymentsTitle: String,
    isBackgroundTransparent: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    ExpenseTrackerWidgetTheme {
        Scaffold(
            backgroundColor =
                if (isBackgroundTransparent) {
                    transparentColorProvider
                } else {
                    GlanceTheme.colors.widgetBackground
                },
            modifier =
                modifier.fillMaxSize().clickable(
                    onClick = actionStartActivity<MainActivity>(),
                ),
            titleBar = {
                TitleBar(
                    startIcon = ImageProvider(R.mipmap.ic_launcher_monochrome),
                    title = upcomingPaymentsTitle,
                )
            },
        ) {
            LazyVerticalGrid(
                gridCells =
                    if (gridMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        GridCells.Adaptive(160.dp)
                    } else {
                        GridCells.Fixed(1)
                    },
                modifier = GlanceModifier.fillMaxSize(),
            ) {
                items(items = items, itemId = { it.id.toLong() }) { item ->
                    Column(
                        modifier = GlanceModifier.padding(8.dp),
                        verticalAlignment = Alignment.Vertical.CenterVertically,
                    ) {
                        Text(
                            text = item.name,
                            maxLines = 1,
                            style = TextStyle(GlanceTheme.colors.onSurface, textAlign = TextAlign.Center),
                        )
                        Text(
                            text = item.price.toCurrencyString(),
                            style = TextStyle(GlanceTheme.colors.onSurface, textAlign = TextAlign.Center),
                        )
                        Text(
                            text = item.nextPaymentDate,
                            maxLines = 2,
                            style = TextStyle(GlanceTheme.colors.onSurface, textAlign = TextAlign.Center),
                        )
                    }
                }
            }
        }
    }
}
