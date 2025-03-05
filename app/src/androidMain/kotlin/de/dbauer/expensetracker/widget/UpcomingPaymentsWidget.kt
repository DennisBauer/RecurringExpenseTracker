package de.dbauer.expensetracker.widget

import android.content.Context
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
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
import asString
import data.UpcomingPaymentData
import de.dbauer.expensetracker.MainActivity
import de.dbauer.expensetracker.R
import model.database.UserPreferencesRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.upcoming_title
import ui.theme.widget.ExpenseTrackerWidgetTheme

class UpcomingPaymentsWidget : GlanceAppWidget(), KoinComponent {
    private val upcomingPayment by inject<UpcomingPaymentsWidgetModel>()
    private val preferences by inject<UserPreferencesRepository>()

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val upcomingPaymentsTitle = Res.string.upcoming_title.asString()

        provideContent {
            LaunchedEffect(Unit) {
                upcomingPayment.init()
            }
            ExpenseTrackerWidgetTheme {
                Scaffold(
                    modifier =
                        GlanceModifier.fillMaxSize().clickable(
                            onClick = actionStartActivity<MainActivity>(),
                        ),
                    titleBar = {
                        TitleBar(
                            startIcon = ImageProvider(R.mipmap.ic_launcher_monochrome),
                            title = upcomingPaymentsTitle,
                        )
                    },
                ) {
                    StaggeredLazyVerticalGrid(
                        items = upcomingPayment.upcomingPaymentsData,
                        gridMode = preferences.gridMode.collectAsState().value,
                    )
                }
            }
        }
    }
}

@Composable
private fun StaggeredLazyVerticalGrid(
    items: List<UpcomingPaymentData>,
    gridMode: Boolean,
    modifier: GlanceModifier = GlanceModifier,
) {
    LazyVerticalGrid(
        gridCells =
            if (gridMode && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                GridCells.Adaptive(160.dp)
            } else {
                GridCells.Fixed(1)
            },
        modifier = modifier.fillMaxSize(),
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
