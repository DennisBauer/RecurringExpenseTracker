package de.dbauer.expensetracker.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.glance.ExperimentalGlanceApi
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import de.dbauer.expensetracker.MainActivity
import de.dbauer.expensetracker.R
import de.dbauer.expensetracker.asString
import de.dbauer.expensetracker.data.CurrencyValue
import de.dbauer.expensetracker.data.Tag
import de.dbauer.expensetracker.data.UpcomingPaymentData
import de.dbauer.expensetracker.model.datastore.IUserPreferencesRepository
import de.dbauer.expensetracker.ui.theme.widget.ExpenseTrackerWidgetTheme
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import recurringexpensetracker.app.generated.resources.Res
import recurringexpensetracker.app.generated.resources.upcoming_title
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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

    @OptIn(ExperimentalUuidApi::class)
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
                    tags =
                        listOf(
                            Tag("Tag 1", 0x80990000, id = Uuid.random().hashCode()),
                            Tag("Tag 2", 0x80009999, id = Uuid.random().hashCode()),
                            Tag("Tag 3", 0x804c0099, id = Uuid.random().hashCode()),
                        ),
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
                    tags =
                        listOf(
                            Tag("Tag 1", 0x80990000, id = Uuid.random().hashCode()),
                        ),
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
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            Text(
                                text = item.price.toCurrencyString(),
                                style = TextStyle(GlanceTheme.colors.onSurface, textAlign = TextAlign.Center),
                            )
                            Row(
                                horizontalAlignment = Alignment.Start,
                                modifier = GlanceModifier.fillMaxWidth(),
                            ) {
                                // There are only 10 elements allowed within a row in Glance
                                Box(modifier = GlanceModifier.defaultWeight()) { }
                                item.tags.take(9).forEach {
                                    val circleBitmap =
                                        createCircleBitmap(
                                            context = LocalContext.current,
                                            color = it.color.toInt(),
                                            sizeDp = 16,
                                        )
                                    Image(
                                        provider = ImageProvider(circleBitmap),
                                        contentDescription = null,
                                        modifier = GlanceModifier.padding(start = 2.dp).size(16.dp),
                                    )
                                }
                            }
                        }
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

fun createCircleBitmap(
    context: Context,
    color: Int,
    sizeDp: Int,
): Bitmap {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()

    val bitmap = createBitmap(sizePx, sizePx)
    val canvas = Canvas(bitmap)

    val paint =
        Paint().apply {
            isAntiAlias = true
            this.color = color
            style = Paint.Style.FILL
        }

    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)
    return bitmap
}
